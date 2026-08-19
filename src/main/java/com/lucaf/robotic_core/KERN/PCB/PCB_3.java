package com.lucaf.robotic_core.KERN.PCB;

import com.lucaf.robotic_core.dataInterfaces.impl.SerialEvent;
import com.lucaf.robotic_core.dataInterfaces.impl.SerialInterface;
import com.lucaf.robotic_core.impl.ScaleInterface;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * KERN PCB series weighing scale, adapted to the {@link ScaleInterface} abstraction.
 * <p>
 * The scale must be set to the {@code "rE CR"} data-transmission mode (menu → Pr → rE CR) to accept
 * remote ASCII commands, which are sent without a trailing CR/LF. It talks through a
 * {@link SerialInterface}: {@code "w"} requests a stable-or-unstable weight, {@code "s"} requests a
 * stable weight and {@code "t"} tares the scale. Incoming lines are parsed for a numeric weight and
 * forwarded through {@link #emitReading(double)} when the reading stream is enabled.
 */
public class PCB_3 extends ScaleInterface {

    /**
     * Pattern used to extract the numeric weight from a serial line.
     */
    private static final Pattern WEIGHT_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    /**
     * Command that requests a stable-or-unstable weight reading.
     */
    private static final String COMMAND_READ = "w";

    /**
     * Command that requests a stable weight reading.
     */
    private static final String COMMAND_READ_STABLE = "s";

    /**
     * Command that tares the scale.
     */
    private static final String COMMAND_TARE = "t";

    /**
     * Absolute weight below which the scale is considered tared.
     */
    private static final double TARE_TOLERANCE = 0.1;

    /**
     * The low-level serial interface used to communicate with the scale.
     */
    private final SerialInterface serial;

    /**
     * Executor used for asynchronous operations such as {@link #tare()}.
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Latch used by {@link #read()} to wait for the response to a read command.
     */
    private volatile CountDownLatch readLatch = null;

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
     * Parses the numeric weight out of a raw serial line.
     *
     * @param data the raw line received from the scale
     * @return the parsed weight, or {@code null} if no number is present
     */
    private Double parseWeight(String data) {
        Matcher matcher = WEIGHT_PATTERN.matcher(data);
        if (!matcher.find()) {
            return null;
        }
        return Double.parseDouble(matcher.group());
    }

    /**
     * Called for every {@link SerialEvent} emitted by the serial interface. Updates the last reading
     * and, when event reading is enabled, forwards the weight to the registered consumers.
     *
     * @param event the serial event carrying the incoming data
     */
    private void onData(SerialEvent event) {
        try {
            Double weight = parseWeight(event.readString());
            if (weight == null) {
                return;
            }
            lastReading.set(weight);
            if (isEventReadingEnabled()) {
                emitReading(weight);
            }
            CountDownLatch latch = readLatch;
            if (latch != null) {
                latch.countDown();
            }
        } catch (IOException e) {
            serial.logError("Error reading from scale: " + e.getMessage());
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
        return readWithCommand(COMMAND_READ);
    }

    /**
     * Actively reads the current stable weight by sending the {@code "s"} command and waiting for the
     * response.
     *
     * @return the current stable weight, or {@code -1} if no reading is available
     * @throws IOException if the read command cannot be sent or the wait is interrupted
     */
    @Override
    public double readStable() throws IOException {
        return readWithCommand(COMMAND_READ_STABLE);
    }

    /**
     * Sends a read command and waits for the scale to reply with a weight value.
     *
     * @param command the read command to send ({@code "w"} or {@code "s"})
     * @return the parsed weight, or {@code -1} if no reading is available
     * @throws IOException if the command cannot be sent or the wait is interrupted
     */
    private double readWithCommand(String command) throws IOException {
        CountDownLatch latch = new CountDownLatch(1);
        readLatch = latch;
        try {
            serial.send(command.getBytes());
            if (!latch.await(1000, TimeUnit.MILLISECONDS)) {
                serial.logWarning("Read timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while reading from scale", e);
        } finally {
            readLatch = null;
        }
        Double value = lastReading.get();
        return value == null ? -1 : value;
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
                while (System.currentTimeMillis() - start < 5000) {
                    if (Math.abs(read()) < TARE_TOLERANCE) {
                        return true;
                    }
                    Thread.sleep(500);
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
