package com.lucaf.robotic_core.moxMec;

import com.lucaf.robotic_core.dataInterfaces.impl.SerialEvent;
import com.lucaf.robotic_core.dataInterfaces.impl.SerialInterface;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MoxMecSerialWrapper implements Consumer<SerialEvent> {

    /**
     * The slave id address
     */
    @Getter
    @Setter
    int address;

    /**
     * The serial interface
     */
    final SerialInterface serialInterface;

    /**
     * The latch for the communication
     */
    CountDownLatch latch = new CountDownLatch(1);

    /**
     * The last response
     */
    MoxMecCommand lastResponse = null;

    /**
     * The expected response
     */
    char[] expected = new char[0];

    /**
     * Constructor of the class
     * @param serialInterface the serial interface to use
     */
    public MoxMecSerialWrapper(SerialInterface serialInterface, int address) {
        this.serialInterface = serialInterface;
        this.address = address;
        serialInterface.addDataListener(this);
    }

    byte[] fromString(char[] chars) {
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            bytes[i] = (byte) chars[i];
        }
        return bytes;
    }

    boolean equals(char[] a, char[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    public synchronized MoxMecCommand sendForResult(MoxMecCommand request) throws IOException {
        latch = new CountDownLatch(1);
        try {
            serialInterface.purge();
            expected = request.getRequestFrame();
            serialInterface.logDebug(String.format("Sending command: %s", new String(expected)));
            lastResponse = null;
            serialInterface.send(fromString(expected));
            latch.await(1000, TimeUnit.MILLISECONDS);
            if (lastResponse == null || !equals(request.getRequestFrame(), expected)) {
                serialInterface.logWarning(String.format("Unexpected response for command: %s", new String(expected)));
                throw new IOException("No response received");
            }
            expected = new char[0];
            serialInterface.logDebug(String.format("Received response: %s", lastResponse.toString()));
            return lastResponse;
        } catch (IOException | InterruptedException e ) {
            serialInterface.logError(String.format("Error sending command: %s - %s", new String(expected), e.getMessage()));
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e.getMessage());
        }
    }

    protected synchronized boolean send(MoxMecCommand request) throws IOException {
        try {
            serialInterface.purge();
            char[] expected = request.getRequestFrame();
            serialInterface.logDebug(String.format("Sending command: %s", new String(expected)));
            serialInterface.send(fromString(expected));
            return true;
        } catch (IOException e ) {
            serialInterface.logError(String.format("Error sending command: %s - %s", new String(expected), e.getMessage()));
            throw e;
        }
    }

    /**
     * The onData method that will be called when data is received
     * @param event the received data
     */
    @Override
    public void accept(SerialEvent event) {
        try {
            String data = event.readString(12, 1000);
            lastResponse = new MoxMecCommand(data);
            latch.countDown();
        } catch (IOException e) {
            serialInterface.logError(String.format("Error reading data: %s", e.getMessage()));
        }
    }
}
