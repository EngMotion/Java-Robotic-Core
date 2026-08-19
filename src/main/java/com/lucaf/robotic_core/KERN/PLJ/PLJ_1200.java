package com.lucaf.robotic_core.KERN.PLJ;

import com.lucaf.robotic_core.dataInterfaces.impl.SerialEvent;
import com.lucaf.robotic_core.dataInterfaces.impl.SerialInterface;
import com.lucaf.robotic_core.impl.ScaleInterface;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * KERN PLJ precision balance, adapted to the {@link ScaleInterface} abstraction.
 * <p>
 * The PLJ streams weight readings continuously over a {@link SerialInterface}; the tare command is
 * {@code "T"}. Incoming lines are parsed for a numeric weight and forwarded through
 * {@link #emitReading(double)} when the reading stream is enabled.
 */
public class PLJ_1200 extends ScaleInterface {

    /**
     * Pattern used to extract the numeric weight from a serial line.
     */
    private static final Pattern WEIGHT_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    /**
     * Command that tares the scale.
     */
    private static final String COMMAND_TARE = "T";

    /**
     * Absolute weight below which the scale is considered tared.
     */
    private static final double TARE_TOLERANCE = 0.01;

    /**
     * The low-level serial interface used to communicate with the scale.
     */
    private final SerialInterface serial;

    /**
     * Executor used for asynchronous operations such as {@link #tare()}.
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Constructs the scale on top of the given serial interface.
     *
     * @param serial low-level serial interface connected to the scale
     */
    public PLJ_1200(SerialInterface serial) {
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
    public PLJ_1200(SerialInterface serial, Consumer<Double> readingConsumer) {
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
        } catch (IOException e) {
            serial.logError("Error reading from scale: " + e.getMessage());
        }
    }

    /**
     * Reads the most recent weight streamed by the scale.
     *
     * @return the latest streamed weight, or {@code -1} if no reading is available
     */
    @Override
    public double read() {
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
                while (System.currentTimeMillis() - start < 10000) {
                    if (Math.abs(read()) < TARE_TOLERANCE) {
                        return true;
                    }
                    Thread.sleep(1000);
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
