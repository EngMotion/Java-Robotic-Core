package com.lucaf.robotic_core.KERN.PLJ;

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

/**
 * KERN PLJ precision balance, adapted to the {@link ScaleInterface} abstraction.
 * <p>
 * Unlike the KERN PCB, the PLJ transmits on its own once continuous data transmission is enabled on
 * the device, so {@link #read()} reports the most recent frame instead of polling for one. A settled
 * weight can still be requested explicitly with {@link #readStable()}.
 *
 * <h2>Response format</h2>
 * A weighing record is a fixed-width, 13 character frame terminated by {@code CR LF}:
 * <pre>
 *   S N1 N2 N3 N4 N5 N6 N7 N8 U1 U2 U3 T CR LF
 * </pre>
 * <ul>
 *     <li>{@code S} — a space or a minus sign for negative weights</li>
 *     <li>{@code N1..N8} — the mass, right aligned and padded with spaces, decimal point included</li>
 *     <li>{@code U1..U3} — the unit ({@code "g"}, {@code "kg"}, {@code "ct"}, …), space padded</li>
 *     <li>{@code T} — the stability index, {@link #STABILITY_STABLE} once the weight has settled</li>
 * </ul>
 * The manual describes the stability index as a single character but does not spell out the value it
 * takes while the weight is still moving, so anything other than {@code 'S'} counts as unstable.
 *
 * <h2>Remote commands</h2>
 * <table>
 *     <caption>Remote commands understood by the balance</caption>
 *     <tr><td>{@code "T"} (H54)</td><td>tare — {@link #tare()}</td></tr>
 *     <tr><td>{@code "C"} (H43)</td><td>record the current weight — {@link #record()}</td></tr>
 *     <tr><td>{@code "E"} (H45)</td><td>transmit the stable weight — {@link #readStable()}</td></tr>
 *     <tr><td>{@code "M"} (H4D)</td><td>open the menu — {@link #menu()}</td></tr>
 *     <tr><td>{@code "O"} (H4F)</td><td>switch the balance on or off — {@link #togglePower()}</td></tr>
 * </table>
 *
 * <h2>Framing</h2>
 * As on the PCB, the serial port does not necessarily deliver a frame in one piece, so incoming bytes
 * are fed to a {@link LineAssembler} and decoded only once their {@code CR LF} terminator arrives.
 */
public class PLJ_1200 extends ScaleInterface {

    /**
     * Command that tares the balance ({@code H54}).
     */
    private static final String COMMAND_TARE = "T";

    /**
     * Command that records the current weight ({@code H43}).
     */
    private static final String COMMAND_RECORD = "C";

    /**
     * Command that asks the balance to transmit the stable weight ({@code H45}).
     */
    private static final String COMMAND_READ_STABLE = "E";

    /**
     * Command that opens the balance menu ({@code H4D}).
     */
    private static final String COMMAND_MENU = "M";

    /**
     * Command that switches the balance on or off ({@code H4F}).
     */
    private static final String COMMAND_POWER = "O";

    /**
     * Number of characters of a weighing frame, terminator excluded.
     */
    private static final int FRAME_LENGTH = 13;

    /**
     * Position of the sign field, either a space or {@link #SIGN_NEGATIVE}.
     */
    private static final int SIGN_INDEX = 0;

    /**
     * The character marking a negative weight in the sign field.
     */
    private static final char SIGN_NEGATIVE = '-';

    /**
     * Position of the first character of the mass field.
     */
    private static final int VALUE_INDEX = 1;

    /**
     * Number of characters of the mass field.
     */
    private static final int VALUE_LENGTH = 8;

    /**
     * Position of the first character of the unit field.
     */
    private static final int UNIT_INDEX = 9;

    /**
     * Number of characters of the unit field.
     */
    private static final int UNIT_LENGTH = 3;

    /**
     * Position of the stability index.
     */
    private static final int STABILITY_INDEX = 12;

    /**
     * Value of the stability index once the weight has settled.
     */
    private static final char STABILITY_STABLE = 'S';

    /**
     * How long {@link #readStable()} waits for the answer to an {@code "E"} command. The balance only
     * transmits once the weight has settled, which can take a moment after the load changes.
     */
    private static final long STABLE_READ_TIMEOUT_MS = 2000;

    /**
     * Absolute weight below which the balance is considered tared.
     */
    private static final double TARE_TOLERANCE = 0.01;

    /**
     * How long {@link #tare()} waits for the weight to settle near zero.
     */
    private static final long TARE_TIMEOUT_MS = 10000;

    /**
     * Delay between two weight checks while waiting for {@link #tare()} to complete.
     */
    private static final long TARE_POLL_INTERVAL_MS = 1000;

    /**
     * The low-level serial interface used to communicate with the balance.
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
     * Latch used by {@link #readStable()} to wait for the answer to an {@code "E"} command.
     */
    private volatile CountDownLatch readLatch = null;

    /**
     * Frame that answered the read command currently in flight, if any.
     */
    private final AtomicReference<ScaleResponse> pendingResponse = new AtomicReference<>();

    /**
     * Constructs the balance on top of the given serial interface.
     *
     * @param serial low-level serial interface connected to the balance
     */
    public PLJ_1200(SerialInterface serial) {
        super(serial);
        this.serial = serial;
        this.frames = new LineAssembler(serial::logWarning);
        serial.addDataListener(this::onData);
    }

    /**
     * Constructs the balance on top of the given serial interface and registers a reading consumer.
     *
     * @param serial          low-level serial interface connected to the balance
     * @param readingConsumer consumer notified with every streamed weight reading (may be {@code null})
     */
    public PLJ_1200(SerialInterface serial, Consumer<ScaleResponse> readingConsumer) {
        super(serial, readingConsumer);
        this.serial = serial;
        this.frames = new LineAssembler(serial::logWarning);
        serial.addDataListener(this::onData);
    }

    /**
     * Decodes a single weighing frame, terminator excluded.
     *
     * @param frame the frame received from the balance
     * @return the decoded response, or {@code null} if the frame does not follow the protocol
     */
    static ScaleResponse parse(String frame) {
        if (frame == null || frame.length() != FRAME_LENGTH) {
            return null;
        }
        char sign = frame.charAt(SIGN_INDEX);
        if (sign != ' ' && sign != SIGN_NEGATIVE) {
            return null;
        }
        String digits = frame.substring(VALUE_INDEX, VALUE_INDEX + VALUE_LENGTH).trim();
        if (!isDecimal(digits)) {
            return null;
        }
        double magnitude = Double.parseDouble(digits);
        String unit = frame.substring(UNIT_INDEX, UNIT_INDEX + UNIT_LENGTH).trim();
        boolean stable = frame.charAt(STABILITY_INDEX) == STABILITY_STABLE;
        return ScaleResponse.weight(sign == SIGN_NEGATIVE ? -magnitude : magnitude, unit, stable);
    }

    /**
     * Checks that a string is a bare decimal number: digits and at most one decimal point, nothing
     * else. {@link Double#parseDouble} is far more tolerant than the protocol (it would happily accept
     * {@code "1e5"}, {@code "NaN"} or {@code "+1d"}), so the field is validated before being parsed.
     *
     * @param value the trimmed content of the mass field
     * @return {@code true} if the value is a plain decimal number
     */
    private static boolean isDecimal(String value) {
        boolean digitSeen = false;
        boolean pointSeen = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c >= '0' && c <= '9') {
                digitSeen = true;
            } else if (c == '.' && !pointSeen) {
                pointSeen = true;
            } else {
                return false;
            }
        }
        return digitSeen;
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
        setStable(response.isStable());
        if (!response.getUnit().isEmpty()) {
            setUnit(response.getUnit());
        }
        lastReading.set(response);
        if (isEventReadingEnabled()) {
            emitReading(response);
        }
        CountDownLatch latch = readLatch;
        if (latch != null) {
            pendingResponse.set(response);
            latch.countDown();
        }
    }

    /**
     * Reports the most recent weight transmitted by the balance.
     * <p>
     * The PLJ transmits on its own, so this method sends nothing and never blocks: it simply returns
     * the last frame received. Use {@link #readStable()} to actively request a settled weight.
     *
     * @return the latest streamed response, or an error response if nothing has arrived yet
     */
    @Override
    public ScaleResponse read() {
        ScaleResponse last = lastReading.get();
        return last == null ? ScaleResponse.error() : last;
    }

    /**
     * Requests the stable weight by sending the {@code "E"} command and waiting for the answer.
     *
     * @return the stable weight, or an error response if the balance did not answer in time
     * @throws IOException if the command cannot be sent or the wait is interrupted
     */
    @Override
    public ScaleResponse readStable() throws IOException {
        CountDownLatch latch = new CountDownLatch(1);
        pendingResponse.set(null);
        readLatch = latch;
        boolean answered;
        try {
            serial.send(COMMAND_READ_STABLE.getBytes());
            answered = latch.await(STABLE_READ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while reading from scale", e);
        } finally {
            readLatch = null;
        }
        ScaleResponse response = pendingResponse.getAndSet(null);
        if (!answered || response == null) {
            serial.logWarning("No answer to command \"" + COMMAND_READ_STABLE + "\" within "
                    + STABLE_READ_TIMEOUT_MS + " ms");
            return ScaleResponse.error();
        }
        return response;
    }

    /**
     * Tares the balance and waits until the transmitted weight settles near zero.
     *
     * @return a Future resolving to {@code true} if the tare succeeded, {@code false} otherwise
     */
    @Override
    public Future<Boolean> tare() {
        return executor.submit(() -> {
            try {
                serial.send(COMMAND_TARE.getBytes());
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < TARE_TIMEOUT_MS) {
                    ScaleResponse reading = read();
                    if (!reading.isError() && Math.abs(reading.getWeight()) < TARE_TOLERANCE) {
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
     * Sends the {@code "C"} command, asking the balance to record the current weight.
     *
     * @throws IOException if the command cannot be sent
     */
    public void record() throws IOException {
        serial.send(COMMAND_RECORD.getBytes());
    }

    /**
     * Sends the {@code "M"} command, opening the balance menu.
     *
     * @throws IOException if the command cannot be sent
     */
    public void menu() throws IOException {
        serial.send(COMMAND_MENU.getBytes());
    }

    /**
     * Sends the {@code "O"} command, switching the balance on or off.
     *
     * @throws IOException if the command cannot be sent
     */
    public void togglePower() throws IOException {
        serial.send(COMMAND_POWER.getBytes());
    }

    /**
     * Enables the continuous reading stream. Incoming weights will be forwarded to consumers.
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
     * Shuts down the balance and its executor.
     *
     * @throws IOException if the reading stream cannot be stopped
     */
    @Override
    public void shutdown() throws IOException {
        super.shutdown();
        executor.shutdownNow();
    }
}
