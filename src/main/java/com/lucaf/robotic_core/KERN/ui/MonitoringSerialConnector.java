package com.lucaf.robotic_core.KERN.ui;

import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.dataInterfaces.impl.SerialEvent;
import com.lucaf.robotic_core.dataInterfaces.serial.SimpleSerialConnector;
import jssc.SerialPort;

import java.io.IOException;
import java.util.function.BiConsumer;

/**
 * A {@link SimpleSerialConnector} that reports every byte crossing the serial line to a traffic
 * listener, so that a UI can display the raw conversation with the device in real time.
 * <p>
 * Outgoing traffic is trivial to intercept ({@link #send(byte[])}). Incoming traffic is not: the
 * {@link SerialEvent} handed to the data listeners reads straight from the port, so whichever
 * listener calls {@code readBytes()} first <em>consumes</em> the data and every other listener sees
 * an empty buffer. To avoid stealing the payload from the scale, this connector drains the port
 * once in {@link #emitDataEvent(SerialEvent)}, reports the bytes to the traffic listener and then
 * forwards a {@link BufferedSerialEvent} that replays the very same buffer to the downstream
 * listeners.
 */
public class MonitoringSerialConnector extends SimpleSerialConnector {

    /**
     * Direction of a chunk of traffic, from the point of view of this application.
     */
    public enum Direction {
        /**
         * Bytes written to the device.
         */
        TX,
        /**
         * Bytes received from the device.
         */
        RX
    }

    /**
     * Listener notified with every chunk of traffic. May be {@code null} while the superclass
     * constructor is still running, hence the null checks before every use.
     */
    private final BiConsumer<Direction, byte[]> trafficListener;

    /**
     * Constructs a monitoring connector on top of an already open serial port.
     *
     * @param serialPort      the open serial port to talk through
     * @param name            logical name used to prefix log messages
     * @param logger          logger receiving the library log messages (may be {@code null})
     * @param trafficListener listener notified with every TX/RX chunk (may be {@code null})
     */
    public MonitoringSerialConnector(SerialPort serialPort, String name, Logger logger,
                                     BiConsumer<Direction, byte[]> trafficListener) {
        super(serialPort, name, logger);
        this.trafficListener = trafficListener;
    }

    /**
     * Reports the outgoing bytes to the traffic listener and then writes them to the port.
     *
     * @param request the bytes to send
     * @return {@code true} if the write succeeded
     * @throws IOException if the bytes cannot be written
     */
    @Override
    public boolean send(byte[] request) throws IOException {
        if (trafficListener != null && request != null && request.length > 0) {
            trafficListener.accept(Direction.TX, request.clone());
        }
        return super.send(request);
    }

    /**
     * Drains the incoming payload once, reports it to the traffic listener and replays it to the
     * downstream data listeners through a {@link BufferedSerialEvent}.
     *
     * @param event the event emitted by the underlying serial port
     */
    @Override
    protected void emitDataEvent(SerialEvent event) {
        byte[] payload;
        try {
            payload = event.readBytes();
        } catch (IOException e) {
            logError("Failed to read incoming data: " + e.getMessage());
            return;
        }
        if (payload == null || payload.length == 0) {
            return;
        }
        if (trafficListener != null) {
            trafficListener.accept(Direction.RX, payload.clone());
        }
        super.emitDataEvent(new BufferedSerialEvent(event.getType(), event.getValue(), payload));
    }

    /**
     * A {@link SerialEvent} backed by an in-memory buffer instead of the serial port, so the same
     * payload can be handed to several listeners without any of them consuming it.
     */
    private static final class BufferedSerialEvent extends SerialEvent {

        /**
         * The payload already drained from the port.
         */
        private final byte[] buffer;

        /**
         * @param type   the original jssc event type
         * @param value  the original jssc event value (number of bytes announced)
         * @param buffer the payload to replay
         */
        private BufferedSerialEvent(int type, int value, byte[] buffer) {
            super(type, value);
            this.buffer = buffer;
        }

        /**
         * {@inheritDoc}
         * <p>
         * The timeout is irrelevant: the payload is already in memory.
         */
        @Override
        public String readString(int length, long timeoutMillis) {
            return new String(readBytes(length, timeoutMillis));
        }

        /**
         * {@inheritDoc}
         * <p>
         * Returns at most {@code length} bytes; the buffer is never refilled from the port.
         */
        @Override
        public byte[] readBytes(int length, long timeoutMillis) {
            int size = Math.min(Math.max(length, 0), buffer.length);
            byte[] slice = new byte[size];
            System.arraycopy(buffer, 0, slice, 0, size);
            return slice;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String readString() {
            return new String(buffer);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public byte[] readBytes() {
            return buffer.clone();
        }
    }
}
