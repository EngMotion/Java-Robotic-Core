package com.lucaf.robotic_core.TRINAMIC;

import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.SerialParams;
import com.lucaf.robotic_core.TRINAMIC.utils.TMCLCommand;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import lombok.Setter;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Class that represents the USB communication class for the Trinamic Drivers
 */
public class USB implements SerialPortEventListener {

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
    public USB(String com, SerialParams params) throws SerialPortException {
        serialPort = new SerialPort(com);
        if (serialPort.isOpened()) serialPort.closePort();
        serialPort.openPort();
        serialPort.setParams(
                params.getBaudrate(),
                params.getDatabits(),
                params.getStopbits(),
                params.getParity()
        );
        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        //int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;
        //int mask = SerialPort.MASK_RXCHAR;
        //serialPort.setEventsMask(mask);
        serialPort.addEventListener(this);
    }

    /**
     * The default constructor
     */
    public USB(String com) throws SerialPortException {
        this(com, new SerialParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE));
    }

    /**
     * The latch for the communication
     */
    CountDownLatch latch = new CountDownLatch(1);

    /**
     * The expected response
     */
    byte[] expected = new byte[0];

    /**
     * The last response
     */
    TMCLCommand lastResponse = null;

    public synchronized void writeAsync(TMCLCommand command) throws DeviceCommunicationException {
        if (logger != null) {
            logger.debug("[USB] Writing command async: " + command.toString());
        }
        try {
            serialPort.writeBytes(command.getFrame());
        } catch (SerialPortException e) {
            if (logger != null) {
                logger.error("[USB] Error writing command async: " + e.getMessage());
            }
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that writes a command to the USB
     *
     * @param command the command to write
     * @return the response of the command
     * @throws DeviceCommunicationException if there is an error writing the command
     */
    public synchronized TMCLCommand write(TMCLCommand command) throws DeviceCommunicationException {
        if (logger != null) {
            logger.debug("[USB] Writing command: " + command.toString());
        }
        latch = new CountDownLatch(1);
        try {
            //serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR | SerialPort.PURGE_RXABORT | SerialPort.PURGE_TXABORT);
            //serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
            expected = command.getFrame();
            lastResponse = null;
            serialPort.writeBytes(expected);
            latch.await(1000, TimeUnit.MILLISECONDS);
            if (lastResponse == null) {
                //throw new DeviceCommunicationException("No response");
                return null;
            }
            if (!lastResponse.isOk()) {
                throw new DeviceCommunicationException("Response is not OK: " + lastResponse.toString());
            }
            if (logger != null) {
                logger.debug("[USB] Response: " + lastResponse.toString());
            }
            return lastResponse;
        } catch (SerialPortException | InterruptedException e) {
            if (logger != null) {
                logger.error("[USB] Error writing command: " + e.getMessage());
            }
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

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
                    logger.debug("[USB] Serial response: " + serialPortEvent.getEventValue());
                }
                try {
                    byte[] frames = serialPort.readBytes(9);
                    lastResponse = new TMCLCommand(frames);
                    latch.countDown();
                } catch (SerialPortException e) {
                    e.printStackTrace();
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
