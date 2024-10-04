package com.lucaf.robotic_core.TRINAMIC;

import com.lucaf.robotic_core.TRINAMIC.utils.TMCLCommand;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class USB implements SerialPortEventListener {

    private final String com;

    private final SerialPort serialPort;

    public USB(String com) throws SerialPortException {
        this.com = com;
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

    CountDownLatch latch = new CountDownLatch(1);

    byte[] expected = new byte[0];

    TMCLCommand lastResponse = null;

    public synchronized TMCLCommand write(TMCLCommand command) throws DeviceCommunicationException {
        latch = new CountDownLatch(1);
        try {
            serialPort.purgePort(SerialPort.PURGE_RXCLEAR);
            expected = command.getFrame();
            serialPort.writeBytes(expected);
            lastResponse = null;
            serialPort.writeBytes(command.getFrame());
            latch.await(1000, TimeUnit.MILLISECONDS);
            return lastResponse;
        } catch (SerialPortException | InterruptedException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.isRXCHAR()) {
            if (serialPortEvent.getEventValue() > 0) {
                try {
                    lastResponse = new TMCLCommand(serialPort.readBytes(serialPortEvent.getEventValue()));
                    latch.countDown();
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
