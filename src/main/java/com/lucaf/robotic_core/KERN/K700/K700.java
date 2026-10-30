package com.lucaf.robotic_core.KERN.K700;

import com.lucaf.robotic_core.dataInterfaces.impl.SerialEvent;
import com.lucaf.robotic_core.dataInterfaces.impl.SerialInterface;
import com.lucaf.robotic_core.dataInterfaces.serial.LineAssembler;
import com.lucaf.robotic_core.impl.ScaleInterface;
import com.lucaf.robotic_core.impl.ScaleResponse;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * KERN 770/GS/GJ series weighing scale (internally code-named "K700"), adapted to the
 * {@link ScaleInterface} abstraction.
 * <p>
 * Unlike the KERN PCB, which accepts bare single-letter commands, the 770 series expects every
 * control command to be an {@code ESC}-prefixed sequence terminated by {@code CR LF}:
 * <pre>
 *   ESC P CR LF   — print/output the current result (used as the read command)
 *   ESC T CR LF   — tare
 * </pre>
 * where {@code ESC} is ASCII 27. The scale's serial port must be configured for 7-bit ASCII
 * characters with a parity bit (factory default: odd parity, 1 stop bit, 1200 baud) — this is set
 * on the {@code SerialParams} the connection is opened with, not by this class.
 *
 * <h2>Response format</h2>
 * Every answer is a fixed-width, 14 character frame (terminator excluded), followed by {@code CR LF}:
 * <pre>
 *   S N1 N2 N3 N4 N5 N6 N7 N8 B U1 U2 U3 (with padding)
 * </pre>
 * <ul>
 *     <li>{@code S} — a space or a minus sign for negative weights</li>
 *     <li>{@code N1..N8} — the weight, right aligned and padded with spaces, decimal point included</li>
 *     <li>{@code B} — a space (separator)</li>
 *     <li>{@code U1..U3} — the unit ({@code "g"}, {@code "kg"}, ...), space padded</li>
 * </ul>
 * As with the PCB, the unit field is blanked out while the weight is still settling, which is what
 * marks a reading as unstable rather than stable.
 * <p>
 * Instead of a plain {@code "Error"} payload, the 770 series reports faults as a special frame
 * embedded in the same 14-character layout, e.g. {@code "   ERR X YZ  "} (system error) or a
 * status letter pair such as {@code "H*"} (overload) / {@code "L*"} (underload) / {@code "C*"}
 * (adjusting in progress) at the position the weight digits would otherwise occupy. Any frame that
 * doesn't carry a valid decimal weight is treated as an error/unusable reading.
 *
 * <h2>Framing</h2>
 * As with the PCB, the scale does not necessarily deliver a full frame in a single serial event, so
 * incoming bytes are fed to a {@link LineAssembler} and only decoded once the {@code CR LF}
 * terminator has been seen.
 */
public class K700 extends ScaleInterface {

    /**
     * ASCII Escape character prefixing every control command.
     */
    private static final byte ESC = 27;

    /**
     * Command that requests the current result (read). Sent as {@code ESC P CR LF}.
     */
    private static final byte[] COMMAND_READ = {ESC, 'P', '\r', '\n'};

    /**
     * Command that tares the scale. Sent as {@code ESC T CR LF}.
     */
    private static final byte[] COMMAND_TARE = {ESC, 'T', '\r', '\n'};

    /**
     * Number of characters of a frame, terminator excluded.
     */
    private static final int FRAME_LENGTH = 14;

    /**
     * Payload marking a system-error frame.
     */
    private static final String ERROR_MARKER = "ERR";

    /**
     * How long {@link #read()} waits for the answer to a read command.
     */
    private static final long READ_TIMEOUT_MS = 1000;

    /**
     * Absolute weight below which the scale is considered tared.
     */
    private static final double TARE_TOLERANCE = 0.1;

    /**
     * How long {@link #tare()} waits for the weight to settle near zero.
     */
    private static final long TARE_TIMEOUT_MS = 5000;

    /**
     * Delay between two weight polls while waiting for {@link #tare()} to complete.
     */
    private static final long TARE_POLL_INTERVAL_MS = 500;

    /**
     * The low-level serial interface used to communicate with the scale.
     */
    private final SerialInterface serial;

    /**
     * Executor used for asynchronous operations such as {@link #tare()}.
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Reassembles the frames out of the chunks in which the serial port delivers them.
     */
    private final LineAssembler frames;

    /**
     * Latch used by {@link #readWithCommand} to wait for the answer to a read command.
     */
    private volatile CountDownLatch readLatch = null;

    /**
     * Frame that answered the read command currently in flight, if any.
     */
    private final AtomicReference<ScaleResponse> pendingResponse = new AtomicReference<>();

    /**
     * Constructs the scale on top of the given serial interface.
     *
     * @param serial low-level serial interface connected to the scale
     */
    public K700(SerialInterface serial) {
        super(serial);
        this.serial = serial;
        this.frames = new LineAssembler(serial::logWarning);
        serial.addDataListener(this::onData);
    }

    /**
     * Constructs the scale on top of the given serial interface and registers a reading consumer.
     *
     * @param serial          low-level serial interface connected to the scale
     * @param readingConsumer consumer notified with every streamed weight reading (may be {@code null})
     */
    public K700(SerialInterface serial, Consumer<ScaleResponse> readingConsumer) {
        super(serial, readingConsumer);
        this.serial = serial;
        this.frames = new LineAssembler(serial::logWarning);
        serial.addDataListener(this::onData);
    }

    /**
     * Weight frame: optional sign, the number (digits with at most one decimal point), then an
     * optional unit. Whitespace between the parts is free, so the frame is read by its structure and
     * not by fixed character positions: a weight such as {@code "12.34567"} (2 integer + 5 decimal
     * digits) and {@code "156.1111"} (3 + 4) are both 8 characters, but the scale may pad them
     * differently depending on the range and the number of decimals.
     */
    private static final Pattern WEIGHT_FRAME = Pattern.compile(
            "^\\s*([+-])?\\s*(\\d+(?:\\.\\d*)?|\\.\\d+)(?:\\s*([A-Za-z%]{1,3}))?\\s*$");

    /**
     * Decodes a single frame, terminator excluded.
     * <p>
     * The frame is matched against {@link #WEIGHT_FRAME} instead of being cut at fixed offsets, so it
     * does not matter how the scale aligns the number inside the 8 character weight field.
     *
     * @param frame the frame received from the scale
     * @return the decoded response, or {@code null} if the frame does not follow the protocol
     */
    static ScaleResponse parse(String frame) {
        if (frame == null) {
            return null;
        }
        Matcher matcher = WEIGHT_FRAME.matcher(frame);
        if (matcher.matches()) {
            double magnitude = Double.parseDouble(matcher.group(2));
            String unit = matcher.group(3) == null ? "" : matcher.group(3);
            boolean negative = "-".equals(matcher.group(1));
            // The 770 blanks out the unit field while the weight is still moving, so a unit
            // means the reading has settled.
            return ScaleResponse.weight(negative ? -magnitude : magnitude, unit, !unit.isEmpty());
        }
        // Not a weight frame: documented system-error layout ("...ERR X YZ...").
        if (frame.contains(ERROR_MARKER)) {
            return ScaleResponse.error();
        }
        // Wrong length and not a weight: line noise / partial frame, ignored by the caller.
        if (frame.length() != FRAME_LENGTH) {
            return null;
        }
        // Overload "H*", underload "L*", adjusting "C*" or an unrecognised frame of the right
        // length: not a usable weight, reported as an error.
        return ScaleResponse.error();
    }

    /**
     * Called for every {@link SerialEvent} emitted by the serial interface. Feeds the incoming bytes
     * to the frame assembler and handles every frame it completes.
     *
     * @param event the serial event carrying the incoming data
     */
    private void onData(SerialEvent event) {
        String chunk;
        try {
            chunk = event.readString();
        } catch (IOException e) {
            serial.logError("Error reading from scale: " + e.getMessage());
            return;
        }
        for (String frame : frames.append(chunk)) {
            handleFrame(frame);
        }
    }

    /**
     * Decodes a complete frame, updates the driver state and hands the result to the read command
     * currently waiting for an answer, if any.
     *
     * @param frame a complete frame, terminator excluded
     */
    private void handleFrame(String frame) {
        ScaleResponse response = parse(frame);
        if (response == null) {
            serial.logWarning("Ignoring malformed frame \"" + frame + "\"");
            return;
        }
        if (response.isError()) {
            hasError.set(true);
            setStable(false);
            serial.logError("Scale reported a system error or non-weight status frame");
        } else {
            hasError.set(false);
            setStable(response.isStable());
            if (!response.getUnit().isEmpty()) {
                setUnit(response.getUnit());
            }
            lastReading.set(response);
            if (isEventReadingEnabled()) {
                emitReading(response);
            }
        }
        CountDownLatch latch = readLatch;
        if (latch != null) {
            pendingResponse.set(response);
            latch.countDown();
        }
    }

    /**
     * Actively reads the current weight (stable or unstable) by sending {@code ESC P CR LF} and
     * waiting for the response.
     * <p>
     * The 770 series has no dedicated "stable only" command like the PCB's {@code "s"}, so
     * {@link #readStable()} is not overridden and falls back to the base class's default of simply
     * calling this method.
     *
     * @return the current weight, or an error response if the scale did not answer
     * @throws IOException if the read command cannot be sent or the wait is interrupted
     */
    @Override
    public ScaleResponse read() throws IOException {
        return readWithCommand(COMMAND_READ);
    }

    /**
     * Sends a command and waits for the scale to reply with a frame.
     *
     * @param command the command bytes to send (already including the ESC prefix and CR LF terminator)
     * @return the answer, or an error response if the scale stayed silent or reported a fault
     * @throws IOException if the command cannot be sent or the wait is interrupted
     */
    private ScaleResponse readWithCommand(byte[] command) throws IOException {
        CountDownLatch latch = new CountDownLatch(1);
        pendingResponse.set(null);
        readLatch = latch;
        boolean answered;
        try {
            serial.send(command);
            answered = latch.await(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while reading from scale", e);
        } finally {
            readLatch = null;
        }
        ScaleResponse response = pendingResponse.getAndSet(null);
        if (!answered || response == null) {
            serial.logWarning("No answer to read command within " + READ_TIMEOUT_MS + " ms");
            return ScaleResponse.error();
        }
        return response;
    }

    /**
     * Tares the scale and waits until the reading settles near zero.
     *
     * @return a Future resolving to {@code true} if the tare succeeded, {@code false} otherwise
     */
    @Override
    public Future<Boolean> tare() {
        return executor.submit(() -> {
            try {
                serial.send(COMMAND_TARE);
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < TARE_TIMEOUT_MS) {
                    ScaleResponse weight = read();
                    if (!weight.isError() && Math.abs(weight.getWeight()) < TARE_TOLERANCE) {
                        return true;
                    }
                    Thread.sleep(TARE_POLL_INTERVAL_MS);
                }
                serial.logWarning("Tare timeout");
                return false;
            } catch (Exception e) {
                serial.logError("Error taring scale: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Enables the continuous reading stream. Incoming weights will be forwarded to consumers.
     * <p>
     * Note: the 770 series only pushes data on its own if it has been put into "Auto print" mode via
     * its own operating menu (codes 6-1-4 / 6-1-5). This class does not poll on a timer — it only
     * forwards whatever the scale sends, whether that's in response to {@link #read()} or, if the
     * scale is in Auto print mode, unsolicited frames pushed continuously.
     */
    @Override
    public void enableEventReading() {
        isStreaming.set(true);
    }

    /**
     * Disables the continuous reading stream.
     */
    @Override
    public void disableEventReading() {
        isStreaming.set(false);
    }

    /**
     * Shuts down the scale and its executor.
     *
     * @throws IOException if the reading stream cannot be stopped
     */
    @Override
    public void shutdown() throws IOException {
        super.shutdown();
        executor.shutdownNow();
    }
}