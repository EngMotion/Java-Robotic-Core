package com.lucaf.robotic_core.mock.dataInterface;

import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.dataInterfaces.impl.SerialEvent;
import com.lucaf.robotic_core.dataInterfaces.impl.SerialInterface;
import lombok.Setter;

import java.io.IOException;

public class MockedSerialInterface extends SerialInterface {

    public MockedSerialInterface(String name, Logger logger) {
        super(name, logger);
    }

    public MockedSerialInterface(String name) {
        super(name);
    }

    @Setter
    byte[] nextResponse = new byte[0];

    @Override
    public byte[] sendForResult(byte[] request) throws IOException {
        send(request);
        return nextResponse;
    }

    @Override
    public void purge() throws IOException { }

    @Override
    protected void onData(byte[] data) {
        SerialEvent event = new SerialEvent(1, data.length) {
            @Override
            public String readString(int length, long timeoutMillis) throws IOException {
                if (length > data.length) {
                    throw new IOException("Not enough data available");
                }
                return new String(data, 0, length);
            }

            @Override
            public byte[] readBytes(int length, long timeoutMillis) throws IOException {
                if (length > data.length) {
                    throw new IOException("Not enough data available");
                }
                byte[] result = new byte[length];
                System.arraycopy(data, 0, result, 0, length);
                return result;
            }

            @Override
            public String readString() throws IOException {
                return new String(data);
            }

            @Override
            public byte[] readBytes() throws IOException {
                return data;
            }
        };
        emitDataEvent(event);
    }

    @Override
    public boolean send(byte[] request) throws IOException {
        return true;
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
