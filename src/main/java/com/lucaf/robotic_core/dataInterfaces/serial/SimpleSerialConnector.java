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
        return new byte[0];
    }

    @Override
    public void purge() throws IOException {
        try {
            serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
        } catch (SerialPortException e) {
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
            throw new IOException("Failed to send data to serial port", e);
        }
    }

    @Override
    public boolean isConnected() {
        return serialPort != null && serialPort.isOpened();
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
                            throw new IOException("Failed to read string from serial port", e);
                        }
                    }

                    @Override
                    public byte[] readBytes(int length, long timeoutMillis) throws IOException {
                        try {
                            return serialPort.readBytes(length, (int) timeoutMillis);
                        } catch (SerialPortException| SerialPortTimeoutException e) {
                            throw new IOException("Failed to read bytes from serial port", e);
                        }
                    }

                    @Override
                    public String readString() throws IOException {
                        try {
                            return serialPort.readString();
                        } catch (SerialPortException e) {
                            throw new IOException("Failed to read string from serial port", e);
                        }
                    }

                    @Override
                    public byte[] readBytes() throws IOException {
                        try {
                            return serialPort.readBytes();
                        } catch (SerialPortException e) {
                            throw new IOException("Failed to read bytes from serial port", e);
                        }
                    }
                };
                emitDataEvent(event);
            }
        }
    }
}
