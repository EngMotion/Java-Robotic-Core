package com.lucaf.robotic_core.dataInterfaces.test;

import com.lucaf.robotic_core.dataInterfaces.impl.SerialEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scripted {@link com.lucaf.robotic_core.dataInterfaces.impl.SerialInterface} used to test scales.
 * <p>
 * It records every command sent through {@link #send(byte[])} and, when a command has a scripted
 * response, pushes that response back to the registered data listeners after a short delay (mimicking
 * a real scale replying over the wire). Tests can also push data manually via
 * {@link #simulateData(String)} to model a continuous stream.
 */
public class FakeScaleSerialInterface extends MockedSerialInterface {

    /**
     * Commands sent through {@link #send(byte[])} in order.
     */
    private final List<String> sentCommands = Collections.synchronizedList(new ArrayList<>());

    /**
     * Command -> reply data, e.g. {@code "w" -> "12.345 g"}.
     */
    private final Map<String, String> scriptedResponses = new HashMap<>();

    /**
     * Scheduler used to push scripted responses asynchronously.
     */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private boolean connected = true;
    private long responseDelayMs = 10;

    public FakeScaleSerialInterface(String name) {
        super(name);
    }

    /**
     * Scripts a reply that will be pushed back after the given command is sent.
     *
     * @param command  the command to match (e.g. {@code "w"}, {@code "s"}, {@code "t"})
     * @param response the data to simulate as the incoming reply (a line containing a weight)
     */
    public void scriptResponse(String command, String response) {
        scriptedResponses.put(command, response);
    }

    /**
     * Controls the result reported by {@link #isConnected()}.
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Controls the delay before a scripted response is pushed back.
     */
    public void setResponseDelayMs(long responseDelayMs) {
        this.responseDelayMs = responseDelayMs;
    }

    /**
     * @return a snapshot of the commands sent through {@link #send(byte[])}
     */
    public List<String> getSentCommands() {
        synchronized (sentCommands) {
            return new ArrayList<>(sentCommands);
        }
    }

    /**
     * Pushes incoming data to all registered data listeners, as if it were received from the scale.
     */
    public void simulateData(String data) {
        onData(data.getBytes());
    }

    @Override
    public boolean send(byte[] request) throws IOException {
        String command = new String(request);
        sentCommands.add(command);
        String response = scriptedResponses.get(command);
        if (response != null) {
            scheduler.schedule(() -> simulateData(response), responseDelayMs, TimeUnit.MILLISECONDS);
        }
        return true;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    /**
     * Shuts down the internal scheduler. Call in test tear-down to prevent thread leaks.
     */
    public void dispose() {
        scheduler.shutdownNow();
    }
}
