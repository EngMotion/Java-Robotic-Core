package com.lucaf.robotic_core.impl;

import com.lucaf.robotic_core.dataInterfaces.impl.IOInterface;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Abstract base class for weighing scales (bilance).
 * <p>
 * A scale exposes a fixed set of operations:
 * <ul>
 *     <li>{@link #initialize()} — checks the connection</li>
 *     <li>{@link #tare()} — tares (zeroes) the scale</li>
 *     <li>{@link #read()} — actively reads the current weight</li>
 *     <li>{@link #enableEventReading()} / {@link #disableEventReading()} — toggles the continuous
 *     stream of readings pushed by the scale to the {@link Consumer}s registered as reading listeners</li>
 * </ul>
 * <p>
 * Concrete scales implement the actual I/O by talking through the {@link IOInterface} passed in the
 * constructor. Subclasses must report the readings they receive (from polling or from the stream) by
 * calling {@link #emitReading(double)} so that registered listeners and {@link #getLastReading()} stay
 * up to date.
 */
public abstract class ScaleInterface extends SensorInterface {

    /**
     * The low-level I/O interface used to talk with the scale.
     */
    protected final IOInterface connection;

    /**
     * Whether the continuous reading stream is currently enabled.
     */
    protected final AtomicBoolean isStreaming = new AtomicBoolean(false);

    /**
     * Whether the last reported reading is stable (settled) rather than still oscillating.
     */
    protected final AtomicBoolean isStable = new AtomicBoolean(false);

    /**
     * The most recent reading reported through {@link #emitReading(double)}.
     * {@code null} until the first reading is received.
     */
    protected final AtomicReference<Double> lastReading = new AtomicReference<>();

    /**
     * Consumers notified with every reading reported through {@link #emitReading(double)}.
     */
    protected final List<Consumer<Double>> readingListeners = new ArrayList<>();

    /**
     * The unit of the readings reported by this scale (e.g. "g", "kg", "lb").
     */
    @Getter
    @Setter
    protected String unit = "g";

    /**
     * Constructs a scale using the given I/O interface.
     *
     * @param connection low-level I/O interface used to communicate with the scale
     */
    public ScaleInterface(IOInterface connection) {
        this.connection = connection;
    }

    /**
     * Constructs a scale using the given I/O interface and immediately registers a reading consumer.
     *
     * @param connection      low-level I/O interface used to communicate with the scale
     * @param readingConsumer consumer that will receive every weight reading (may be {@code null})
     */
    public ScaleInterface(IOInterface connection, Consumer<Double> readingConsumer) {
        this(connection);
        if (readingConsumer != null) {
            addReadingListener(readingConsumer);
        }
    }

    /**
     * Initializes the scale by checking the connection.
     * <p>
     * Subclasses may override this to perform additional device setup (e.g. sending init commands),
     * typically invoking {@code super.initialize()} as part of their own logic.
     *
     * @return a Future resolving to {@code true} if the scale is connected, {@code false} otherwise
     */
    @Override
    public Future<Boolean> initialize() {
        boolean connected = connection.isConnected();
        isInitialized.set(connected);
        hasError.set(!connected);
        return CompletableFuture.completedFuture(connected);
    }

    /**
     * Gracefully shuts the scale down: stops the reading stream and clears the registered listeners.
     *
     * @throws IOException if an I/O error occurs while disabling the reading stream
     */
    @Override
    public void shutdown() throws IOException {
        disableEventReading();
        readingListeners.clear();
        isStreaming.set(false);
        isShutdown.set(true);
    }

    /**
     * Returns whether the scale is currently connected.
     *
     * @return {@code true} if the underlying I/O interface reports a live connection
     */
    public boolean isConnected() {
        return connection.isConnected();
    }

    /**
     * Actively reads the current weight from the scale.
     *
     * @return the current weight expressed in {@link #getUnit()}
     * @throws IOException if the reading cannot be retrieved
     */
    public abstract double read() throws IOException;

    /**
     * Actively reads the current <em>stable</em> (settled) weight from the scale.
     * <p>
     * By default this is equivalent to {@link #read()}, since not every scale distinguishes stable
     * from unstable readings. Scales that expose a dedicated stable-reading command should override
     * this method (e.g. the KERN PCB uses {@code "s"} for stable versus {@code "w"} for
     * stable-or-unstable).
     *
     * @return the current stable weight expressed in {@link #getUnit()}
     * @throws IOException if the reading cannot be retrieved
     */
    public double readStable() throws IOException {
        return read();
    }

    /**
     * Tares (zeroes) the scale.
     *
     * @return a Future resolving to {@code true} if the tare succeeded, {@code false} otherwise
     */
    public abstract Future<Boolean> tare();

    /**
     * Enables the continuous reading stream: the scale starts pushing readings, which subclasses
     * forward through {@link #emitReading(double)}.
     *
     * @throws IOException if the stream cannot be started
     */
    public abstract void enableEventReading() throws IOException;

    /**
     * Disables the continuous reading stream.
     *
     * @throws IOException if the stream cannot be stopped
     */
    public abstract void disableEventReading() throws IOException;

    /**
     * Returns whether the continuous reading stream is currently enabled.
     *
     * @return {@code true} if event reading is enabled
     */
    public boolean isEventReadingEnabled() {
        return isStreaming.get();
    }

    /**
     * Returns whether the most recent reading is stable (settled).
     *
     * @return {@code true} if the last reading is stable
     */
    public boolean isStable() {
        return isStable.get();
    }

    /**
     * Marks the stability of the current reading. Called by subclasses when they can determine
     * stability from the device output.
     *
     * @param stable whether the current reading is stable
     */
    protected void setStable(boolean stable) {
        isStable.set(stable);
    }

    /**
     * Returns the most recent reading reported through {@link #emitReading(double)}.
     *
     * @return the last reported weight, or {@code null} if no reading has been received yet
     */
    public Double getLastReading() {
        return lastReading.get();
    }

    /**
     * Registers a consumer that will be notified with every reading reported via
     * {@link #emitReading(double)}.
     *
     * @param listener the consumer to notify
     */
    public void addReadingListener(Consumer<Double> listener) {
        readingListeners.add(listener);
    }

    /**
     * Removes a previously registered reading consumer.
     *
     * @param listener the consumer to remove
     */
    public void removeReadingListener(Consumer<Double> listener) {
        readingListeners.remove(listener);
    }

    /**
     * Forwards a reading received from the scale to all registered listeners and updates
     * {@link #getLastReading()}. Subclasses must call this whenever a new weight value is available.
     *
     * @param weight the weight value to propagate
     */
    protected void emitReading(double weight) {
        lastReading.set(weight);
        for (Consumer<Double> listener : readingListeners) {
            listener.accept(weight);
        }
    }
}
