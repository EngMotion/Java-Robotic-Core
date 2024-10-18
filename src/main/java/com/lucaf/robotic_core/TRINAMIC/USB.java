package com.lucaf.robotic_core.TRINAMIC;

import com.lucaf.robotic_core.TRINAMIC.utils.TMCLCommand;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;


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
     * Constructor of the class
     * @param com the COM port of the USB. Will initialize the port
     * @throws SerialPortException if the port is not found
     */
    public USB(String com) throws SerialPortException {
        serialPort = new SerialPort(com);
        serialPort.openPort();
        serialPort.setParams(
                SerialPort.BAUDRATE_9600,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE
        );
        int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;
        serialPort.setEventsMask(mask);
        serialPort.addEventListener(this);
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

    /**
     * Method that writes a command to the USB
     * @param command the command to write
     * @return the response of the command
     * @throws DeviceCommunicationException if there is an error writing the command
     */
    public synchronized TMCLCommand write(TMCLCommand command) throws DeviceCommunicationException {
        latch = new CountDownLatch(1);
        try {
            serialPort.purgePort(SerialPort.PURGE_RXCLEAR);
            serialPort.purgePort(SerialPort.PURGE_TXCLEAR);
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
            return lastResponse;
        } catch (SerialPortException | InterruptedException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that hanles a serial frame event
     * @param serialPortEvent the event of the frame
     */
    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.isRXCHAR()) {
            if (serialPortEvent.getEventValue() > 0) {
                try {
                    byte[] frames = serialPort.readBytes(serialPortEvent.getEventValue());
                    lastResponse = new TMCLCommand(frames);
                    latch.countDown();
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
