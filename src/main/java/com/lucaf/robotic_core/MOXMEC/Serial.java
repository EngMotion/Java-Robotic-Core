package com.lucaf.robotic_core.MOXMEC;

import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.SerialParams;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import jssc.*;
import lombok.Setter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Serial implements SerialPortEventListener {
    /**
     * The serial port of the USB
     */
    private final SerialPort serialPort;

    /**
     * Global Logger
     */
    @Setter
    Logger logger;

    /**
     * Constructor of the class
     *
     * @param com the COM port of the USB. Will initialize the port
     * @throws SerialPortException if the port is not found
     */
    public Serial(String com, SerialParams params) throws SerialPortException {
        serialPort = new SerialPort(com);
        if (serialPort.isOpened()) serialPort.closePort();
        serialPort.openPort();
        serialPort.setParams(
                params.getBaudrate(),
                params.getDatabits(),
                params.getStopbits(),
                params.getParity()
        );
        int mask = SerialPort.MASK_RXCHAR;
        serialPort.setEventsMask(mask);
        serialPort.addEventListener(this);
    }

    /**
     * The default constructor
     */
    public Serial(String com) throws SerialPortException {
        this(com, new SerialParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE));
    }

    /**
     * The latch for the communication
     */
    CountDownLatch latch = new CountDownLatch(1);

    /**
     * The expected response
     */
    char[] expected = new char[0];

    /**
     * The last response
     */
    MoxMecCommand lastResponse = null;

    /**
     * Method that writes a command to the USB
     *
     * @param command the command to write
     * @return the response of the command
     * @throws DeviceCommunicationException if there is an error writing the command
     */
    public synchronized MoxMecCommand write(MoxMecCommand command) throws DeviceCommunicationException {

        latch = new CountDownLatch(1);
        try {
            serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
            expected = command.getRequestFrame();
            if (logger != null) {
                logger.debug("[Serial] Writing command: " + new String(expected));
            }
            lastResponse = null;
            serialPort.writeString(new String(expected));
            latch.await(1000, TimeUnit.MILLISECONDS);
            if (lastResponse == null) {
                return null;
            }
            if (logger != null) {
                logger.debug("[Serial] Response: " + lastResponse.toString());
            }
            return lastResponse;
        } catch (SerialPortException | InterruptedException e ) {
            if (logger != null) {
                logger.error("[Serial] Error writing command: " + e.getMessage());
            }
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    byte[] extractResponse(byte[] data){
        char[] response = new char[data.length];
        for (int i = 0; i < data.length; i++) {
            response[i] = (char) data[i];
        }
        logger.log(new String(response));
        return null;
    }

    byte[] delay = new byte[0];
    /**
     * Method that hanles a serial frame event
     *
     * @param serialPortEvent the event of the frame
     */
    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {

        if (serialPortEvent.isRXCHAR()) {
            if (serialPortEvent.getEventValue() > 0) {
                if (logger != null) {
                    logger.debug("[Serial] Serial response: " + serialPortEvent.getEventValue());
                }
                try {
                    String data = serialPort.readString(12, 1000);
                    MoxMecCommand response = new MoxMecCommand(data);
                    lastResponse = response;
                    latch.countDown();
                } catch (SerialPortException|SerialPortTimeoutException e) {
                    logger.error("[Serial] Error reading serial port: " + e.toString());
                }
            }
        }
    }

    /**
     * Method that closes the port
     *
     * @throws SerialPortException
     */
    public void close() throws SerialPortException {
        if (serialPort.isOpened()) {
            serialPort.closePort();
        }
    }
}
