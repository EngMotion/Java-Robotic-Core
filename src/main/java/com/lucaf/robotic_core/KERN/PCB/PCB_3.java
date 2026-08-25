package com.lucaf.robotic_core.KERN.PCB;

import com.lucaf.robotic_core.dataInterfaces.impl.SerialEvent;
import com.lucaf.robotic_core.dataInterfaces.impl.SerialInterface;
import com.lucaf.robotic_core.impl.ScaleInterface;
import com.lucaf.robotic_core.impl.ScaleResponse;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * KERN PCB series weighing scale, adapted to the {@link ScaleInterface} abstraction.
 * <p>
 * The scale must be set to the {@code "rE CR"} data-transmission mode (menu → Pr → rE CR) to accept
 * remote ASCII commands, which are sent without a trailing CR/LF. It talks through a
 * {@link SerialInterface}: {@code "w"} requests a stable-or-unstable weight, {@code "s"} requests a
 * stable weight and {@code "t"} tares the scale.
 *
 * <h2>Response format</h2>
 * Every answer is a fixed-width, 16 character frame terminated by {@code CR LF}:
 * <pre>
 *   M S N1 N2 N3 N4 N5 N6 N7 N8 N9 N10 B U1 U2 U3 CR LF
 * </pre>
 * <ul>
 *     <li>{@code M} — a space or the letter {@code 'M'}</li>
 *     <li>{@code S} — a space or a minus sign for negative weights</li>
 *     <li>{@code N1..N10} — the weight, right aligned and padded with spaces, decimal point included</li>
 *     <li>{@code B} — a space</li>
 *     <li>{@code U1..U3} — the unit ({@code "g"}, {@code "kg"}, {@code "pcs"}, {@code "%"}), space padded</li>
 * </ul>
 * Three frames can be received:
 * <ul>
 *     <li>stable weight — the unit field is filled in, e.g. {@code "      11.203 g  "}</li>
 *     <li>unstable weight — the very same layout but with a blank unit field, which is what marks the
 *     reading as not settled</li>
 *     <li>system error — eleven spaces followed by {@code "Error"}</li>
 * </ul>
 *
 * <h2>Framing</h2>
 * The scale does not necessarily deliver a frame in a single serial event: a reply such as
 * {@code "      11.203 g  \r\n"} is regularly split into {@code "      11."} and {@code "203 g  \r\n"}.
 * Incoming bytes are therefore accumulated in {@link #buffer} and only decoded once the {@code CR LF}
 * terminator shows up, so that a partial fragment is never mistaken for a complete weight.
 */
public class PCB_3 extends ScaleInterface {

    /**
     * Command that requests a stable-or-unstable weight reading.
     */
    private static final String COMMAND_READ = "w";

    /**
     * Command that requests a stable weight reading. The scale stays silent while the weight is not
     * settled, so this command may legitimately produce no answer at all.
     */
    private static final String COMMAND_READ_STABLE = "s";

    /**
     * Command that tares the scale.
     */
    private static final String COMMAND_TARE = "t";

    /**
     * Number of characters of a frame, terminator excluded.
     */
    private static final int FRAME_LENGTH = 16;

    /**
     * Position of the {@code M} field, either a space or {@link #MODE_MARKER}.
     */
    private static final int MODE_INDEX = 0;

    /**
     * The only non-blank value the {@code M} field can take.
     */
    private static final char MODE_MARKER = 'M';

    /**
     * Position of the {@code S} (sign) field, either a space or {@link #SIGN_NEGATIVE}.
     */
    private static final int SIGN_INDEX = 1;

    /**
     * The character marking a negative weight in the {@code S} field.
     */
    private static final char SIGN_NEGATIVE = '-';

    /**
     * Position of the first character of the {@code N1..N10} weight field.
     */
    private static final int VALUE_INDEX = 2;

    /**
     * Number of characters of the {@code N1..N10} weight field.
     */
    private static final int VALUE_LENGTH = 10;

    /**
     * Position of the {@code B} field separating the weight from the unit; always a space.
     */
    private static final int SEPARATOR_INDEX = 12;

    /**
     * Position of the first character of the {@code U1..U3} unit field.
     */
    private static final int UNIT_INDEX = 13;

    /**
     * Number of characters of the {@code U1..U3} unit field.
     */
    private static final int UNIT_LENGTH = 3;

    /**
     * Payload of the frame the scale sends when it detects a system error.
     */
    private static final String ERROR_PAYLOAD = "Error";

    /**
     * How long {@link #read()} waits for the answer to a {@code "w"} command.
     */
    private static final long READ_TIMEOUT_MS = 1000;

    /**
     * How long {@link #readStable()} waits for the answer to a {@code "s"} command before concluding
     * that the weight is not stable. The scale simply does not reply while it is still settling, so
     * this timeout is the normal way of detecting an unstable weight rather than an error path.
     */
    private static final long STABLE_READ_TIMEOUT_MS = 500;

    /**
     * Idle time after which a partially received frame is considered abandoned and dropped, so that a
     * truncated reply cannot corrupt the frame that follows it. Sending sixteen characters takes a few
     * milliseconds even at the slowest supported baud rate, hence a gap this long means the rest of
     * the frame is never coming.
     */
    private static final long FRAGMENT_TIMEOUT_MS = 250;

    /**
     * Upper bound on the unterminated bytes kept in {@link #buffer}, so that a device stuck babbling
     * without ever sending a terminator cannot grow it without limit.
     */
    private static final int MAX_BUFFER_LENGTH = 256;

    /**
     * Value returned by the read methods when no weight could be obtained.
     */
    private static final double NO_READING = -1;

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
     * Incoming characters not yet forming a complete frame. Guarded by its own monitor.
     */
    private final StringBuilder buffer = new StringBuilder();

    /**
     * Timestamp of the last chunk appended to {@link #buffer}, used to detect abandoned frames.
     */
    private long lastChunkAt = 0;

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
    public PCB_3(SerialInterface serial) {
        super(serial);
        this.serial = serial;
        serial.addDataListener(this::onData);
    }

    /**
     * Constructs the scale on top of the given serial interface and registers a reading consumer.
     *
     * @param serial          low-level serial interface connected to the scale
     * @param readingConsumer consumer notified with every streamed weight reading (may be {@code null})
     */
    public PCB_3(SerialInterface serial, Consumer<Double> readingConsumer) {
        super(serial, readingConsumer);
        this.serial = serial;
        serial.addDataListener(this::onData);
    }
    /**
     * Decodes a single frame, terminator excluded.
     *
     * @param frame the frame received from the scale
     * @return the decoded response, or {@code null} if the frame does not follow the protocol
     */
    static ScaleResponse parse(String frame) {
        if (frame == null) {
            return null;
        }
        if (ERROR_PAYLOAD.equalsIgnoreCase(frame.trim())) {
            return ScaleResponse.error();
        }
        if (frame.length() != FRAME_LENGTH) {
            return null;
        }
        char mode = frame.charAt(MODE_INDEX);
        if (mode != ' ' && mode != MODE_MARKER) {
            return null;
        }
        char sign = frame.charAt(SIGN_INDEX);
        if (sign != ' ' && sign != SIGN_NEGATIVE) {
            return null;
        }
        if (frame.charAt(SEPARATOR_INDEX) != ' ') {
            return null;
        }
        String digits = frame.substring(VALUE_INDEX, VALUE_INDEX + VALUE_LENGTH).trim();
        if (!isDecimal(digits)) {
            return null;
        }
        double magnitude = Double.parseDouble(digits);
        String unit = frame.substring(UNIT_INDEX, UNIT_INDEX + UNIT_LENGTH).trim();
        return ScaleResponse.weight(sign == SIGN_NEGATIVE ? -magnitude : magnitude, unit);
    }

    /**
     * Checks that a string is a bare decimal number: digits and at most one decimal point, nothing
     * else. {@link Double#parseDouble} is far more tolerant than the protocol (it would happily accept
     * {@code "1e5"}, {@code "NaN"} or {@code "+1d"}), so the field is validated before being parsed.
     *
     * @param value the trimmed content of the weight field
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
        if (chunk == null || chunk.isEmpty()) {
            return;
        }
        for (String frame : assembleFrames(chunk)) {
            handleFrame(frame);
        }
    }

    /**
     * Appends a chunk of incoming characters to {@link #buffer} and extracts every complete frame it
     * now contains, leaving any unterminated tail in the buffer for the next chunk.
     *
     * @param chunk the characters just received
     * @return the frames completed by this chunk, terminators stripped, in arrival order
     */
    private List<String> assembleFrames(String chunk) {
        List<String> frames = new ArrayList<>();
        synchronized (buffer) {
            long now = System.currentTimeMillis();
            if (buffer.length() > 0 && now - lastChunkAt > FRAGMENT_TIMEOUT_MS) {
                serial.logWarning("Dropping abandoned partial frame \"" + buffer + "\"");
                buffer.setLength(0);
            }
            lastChunkAt = now;
            buffer.append(chunk);

            int end;
            while ((end = indexOfTerminator(buffer)) >= 0) {
                String frame = buffer.substring(0, end);
                // Consume the frame plus the whole terminator, however many CR/LF characters it uses.
                int consumed = end;
                while (consumed < buffer.length() && isTerminator(buffer.charAt(consumed))) {
                    consumed++;
                }
                buffer.delete(0, consumed);
                if (!frame.isEmpty()) {
                    frames.add(frame);
                }
            }

            if (buffer.length() > MAX_BUFFER_LENGTH) {
                serial.logWarning("Dropping " + buffer.length() + " unterminated characters from the scale");
                buffer.setLength(0);
            }
        }
        return frames;
    }

    /**
     * @param data the buffered characters
     * @return the position of the first terminator character, or {@code -1} if there is none
     */
    private static int indexOfTerminator(CharSequence data) {
        for (int i = 0; i < data.length(); i++) {
            if (isTerminator(data.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @param c a received character
     * @return {@code true} if the character is part of a frame terminator
     */
    private static boolean isTerminator(char c) {
        return c == '\r' || c == '\n';
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
            serial.logError("Scale reported a system error");
        } else {
            hasError.set(false);
            setStable(response.isStable());
            if (!response.getUnit().isEmpty()) {
                setUnit(response.getUnit());
            }
            lastReading.set(response.getWeight());
            if (isEventReadingEnabled()) {
                emitReading(response.getWeight());
            }
        }
        CountDownLatch latch = readLatch;
        if (latch != null) {
            pendingResponse.set(response);
            latch.countDown();
        }
    }

    /**
     * Actively reads the current weight (stable or unstable) by sending the {@code "w"} command and
     * waiting for the response.
     *
     * @return the current weight, or {@code -1} if no reading is available
     * @throws IOException if the read command cannot be sent or the wait is interrupted
     */
    @Override
    public double read() throws IOException {
        return readWithCommand(COMMAND_READ, READ_TIMEOUT_MS, true);
    }

    /**
     * Actively reads the current stable weight by sending the {@code "s"} command and waiting for the
     * response.
     * <p>
     * The scale answers this command only while the weight is settled and stays silent otherwise, so a
     * weight that is still moving is reported as {@code -1} once
     * {@link #STABLE_READ_TIMEOUT_MS} has elapsed. Callers that need a value regardless of stability
     * should use {@link #read()} instead.
     *
     * @return the current stable weight, or {@code -1} if the weight is not stable
     * @throws IOException if the read command cannot be sent or the wait is interrupted
     */
    @Override
    public double readStable() throws IOException {
        return readWithCommand(COMMAND_READ_STABLE, STABLE_READ_TIMEOUT_MS, false);
    }

    /**
     * Sends a read command and waits for the scale to reply with a frame.
     *
     * @param command        the read command to send ({@code "w"} or {@code "s"})
     * @param timeoutMillis  how long to wait for the answer
     * @param warnOnTimeout  whether a missing answer is a fault worth a warning, as opposed to the
     *                       expected way the scale reports an unstable weight
     * @return the weight carried by the answer, or {@code -1} if the scale did not answer or answered
     * with an error
     * @throws IOException if the command cannot be sent or the wait is interrupted
     */
    private double readWithCommand(String command, long timeoutMillis, boolean warnOnTimeout) throws IOException {
        CountDownLatch latch = new CountDownLatch(1);
        pendingResponse.set(null);
        readLatch = latch;
        boolean answered;
        try {
            serial.send(command.getBytes());
            answered = latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while reading from scale", e);
        } finally {
            readLatch = null;
        }
        ScaleResponse response = pendingResponse.getAndSet(null);
        if (!answered || response == null) {
            String message = "No answer to command \"" + command + "\" within " + timeoutMillis + " ms";
            if (warnOnTimeout) {
                serial.logWarning(message);
            } else {
                serial.logDebug(message + ", the weight is not stable");
            }
            return NO_READING;
        }
        if (response.isError()) {
            return NO_READING;
        }
        return response.getWeight();
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
                serial.send(COMMAND_TARE.getBytes());
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < TARE_TIMEOUT_MS) {
                    double weight = read();
                    if (weight != NO_READING && Math.abs(weight) < TARE_TOLERANCE) {
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
