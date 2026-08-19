package com.lucaf.robotic_core.dataInterfaces.serial;

import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.dataInterfaces.impl.SerialEvent;
import com.lucaf.robotic_core.dataInterfaces.impl.SerialInterface;
import jssc.*;

import java.io.IOException;

public class SimpleSerialConnector extends SerialInterface implements SerialPortEventListener {

    final SerialPort serialPort;

    public SimpleSerialConnector(SerialPort serialPort, String name, Logger logger) {
        super(name, logger);
        this.serialPort = serialPort;
        try {
            serialPort.addEventListener(this);
        } catch (SerialPortException e) {
            logError("Failed to add serial port event listener: " + e.getMessage());
        }
    }

    public SimpleSerialConnector(SerialPort serialPort, String name) {
        this(serialPort, name, null);
    }

    @Override
    public byte[] sendForResult(byte[] request) throws IOException {
        send(request);
        try {
            while (serialPort.getInputBufferBytesCount() == 0) {
                Thread.sleep(10);
            }
            return serialPort.readBytes();
        } catch (SerialPortException | InterruptedException e) {
            if (e instanceof SerialPortException) {
                closeOnDisconnect((SerialPortException) e);
            }
            throw new IOException("Failed to read response from serial port", e);
        }
    }

    @Override
    public void purge() throws IOException {
        try {
            serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
        } catch (SerialPortException e) {
            closeOnDisconnect(e);
            throw new IOException("Failed to purge serial port", e);
        }
    }

    @Override
    protected void onData(byte[] data) {
        // Not used in this implementation
    }

    @Override
    public boolean send(byte[] request) throws IOException {
        try {
            return serialPort.writeBytes(request);
        } catch (SerialPortException e) {
            closeOnDisconnect(e);
            throw new IOException("Failed to send data to serial port", e);
        }
    }

    @Override
    public boolean isConnected() {
        return serialPort != null && serialPort.isOpened();
    }

    /**
     * True if the given {@link SerialPortException} signals that the device was
     * physically disconnected. jssc does not expose errno directly: a failed
     * read()/write() is wrapped into a {@code SerialPortException} whose type carries
     * the native {@code strerror(errno)} message. ENXIO ("No such device or address"),
     * ENODEV ("No such device") and EIO ("Input/output error") all indicate an
     * unplugged device, so we detect them from that message.
     */
    private static boolean isDisconnectionError(SerialPortException e) {
        String type = e.getExceptionType();
        if (type == null) {
            return false;
        }
        return type.contains("No such device") || type.contains("Input/output error");
    }

    /**
     * Closes the underlying file descriptor when a read()/write() fails because the
     * device has been unplugged. Closing releases the handle so {@link #isConnected()}
     * reports the disconnection instead of keeping a stale open port.
     */
    private void closeOnDisconnect(SerialPortException e) {
        if (!isDisconnectionError(e)) {
            return;
        }
        logWarning("Serial device disconnected, closing port: " + e.getMessage());
        try {
            if (serialPort.isOpened()) {
                serialPort.closePort();
            }
        } catch (SerialPortException ex) {
            logError("Failed to close serial port after disconnection: " + ex.getMessage());
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        try {
            serialPort.removeEventListener();
        } catch (SerialPortException e) {
            logError("Failed to remove serial port event listener: " + e.getMessage());
        }
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.isRXCHAR()) {
            if (serialPortEvent.getEventValue() > 0) {
                SerialEvent event = new SerialEvent(serialPortEvent.getEventType(), serialPortEvent.getEventValue()) {
                    @Override
                    public String readString(int length, long timeoutMillis) throws IOException {
                        try {
                            return serialPort.readString(length, (int) timeoutMillis);
                        } catch (SerialPortException| SerialPortTimeoutException e) {
                            if (e instanceof SerialPortException) {
                                closeOnDisconnect((SerialPortException) e);
                            }
                            throw new IOException("Failed to read string from serial port", e);
                        }
                    }

                    @Override
                    public byte[] readBytes(int length, long timeoutMillis) throws IOException {
                        try {
                            return serialPort.readBytes(length, (int) timeoutMillis);
                        } catch (SerialPortException| SerialPortTimeoutException e) {
                            if (e instanceof SerialPortException) {
                                closeOnDisconnect((SerialPortException) e);
                            }
                            throw new IOException("Failed to read bytes from serial port", e);
                        }
                    }

                    @Override
                    public String readString() throws IOException {
                        try {
                            return serialPort.readString();
                        } catch (SerialPortException e) {
                            closeOnDisconnect(e);
                            throw new IOException("Failed to read string from serial port", e);
                        }
                    }

                    @Override
                    public byte[] readBytes() throws IOException {
                        try {
                            return serialPort.readBytes();
                        } catch (SerialPortException e) {
                            closeOnDisconnect(e);
                            throw new IOException("Failed to read bytes from serial port", e);
                        }
                    }
                };
                emitDataEvent(event);
            }
        }
    }
}
