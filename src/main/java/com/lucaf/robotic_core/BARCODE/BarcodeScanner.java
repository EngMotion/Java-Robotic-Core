package com.lucaf.robotic_core.BARCODE;

import com.lucaf.robotic_core.Logger;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class BarcodeScanner implements SerialPortEventListener{
    /**
     * The serial port of the Barcode Reader
     */
    private final SerialPort serialPort;

    /**
     * Global Logger
     */
    private final Logger logger;

    /**
     * The list of barcode scan events
     */
    List<BarcodeScanEvent> barcodeScanEvents = new ArrayList<>();

    /**
     * The connected state of the barcode scanner
     */
    boolean connected = false;

    /**
     * Method that returns the connected state of the barcode scanner
     * @return true if the barcode scanner is connected, false otherwise
     */
    boolean isConnected(){
        if (serialPort != null){
            return serialPort.isOpened();
        }
        return connected;
    }

    /**
     * Constructor of the class
     * @param config the configuration of the barcode scanner
     * @param logger the logger of the application
     */
    public BarcodeScanner(BarcodeScannerConfig config, Logger logger) {
        this.logger = logger;
        switch (config.getType()) {
            case "SERIAL":
                serialPort = new SerialPort(config.getSerial());
                setupSerial();
                break;
            case "USB":
                serialPort = null;
                setupKeyListener();
                break;
            default:
                serialPort = null;
        }
    }

    /**
     * Method that adds a barcode scan event
     * @param barcodeScanEvent the barcode scan event
     */
    public void addBarcodeScanEvent(BarcodeScanEvent barcodeScanEvent){
        barcodeScanEvents.add(barcodeScanEvent);
    }

    /**
     * Method that removes a barcode scan event
     * @param barcodeScanEvent the barcode scan event
     */
    public void removeBarcodeScanEvent(BarcodeScanEvent barcodeScanEvent){
        barcodeScanEvents.remove(barcodeScanEvent);
    }

    /**
     * Method that sets up the serial port
     */
    void setupSerial(){
        try {
            if (serialPort.isOpened()){
                serialPort.closePort();
            }
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
            connected = true;
        }catch (Exception e){
            logger.error("Error while setting up serial port for barcode scanner, device not connected?: " + e.getMessage());
        }
    }

    /**
     * Method that closes the serial port
     */
    public void close(){
        if (serialPort != null){
            try {
                serialPort.removeEventListener();
                serialPort.closePort();
            } catch (SerialPortException e) {
                logger.error("Error while closing serial port: " + e.getMessage());
            }
        }
    }

    /**
     * Method that ensures the connection to the barcode scanner
     * @return true if the connection is established, false otherwise
     */
    public boolean ensureConnection(){
        if (serialPort == null){
            setupKeyListener();
            return true;
        }else{
            if (!serialPort.isOpened()){
                setupSerial();
            }
            return serialPort.isOpened();
        }
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.isRXCHAR()) {
            if (serialPortEvent.getEventValue() > 0) {
                try {
                    byte[] frames = serialPort.readBytes(serialPortEvent.getEventValue());
                    String barcode = new String(frames);
                    for (BarcodeScanEvent barcodeScanEvent : barcodeScanEvents){
                        barcodeScanEvent.onBarcodeScan(barcode);
                    }
                    serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR | SerialPort.PURGE_RXABORT | SerialPort.PURGE_TXABORT);
                } catch (SerialPortException e) {
                    logger.warn("Error while reading barcode scanner: " + e.getMessage());
                }
            }
        }
    }

    /**
     * The barcode string
     */
    StringBuilder barcode = new StringBuilder();
    KeyboardFocusManager keyboardFocusManager;
    /**
     * Method that sets up the key listener
     */
    void setupKeyListener(){
        KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        if (keyboardFocusManager != this.keyboardFocusManager){
            this.keyboardFocusManager = keyboardFocusManager;
            this.keyboardFocusManager.addKeyEventDispatcher(new KeyEventDispatcher() {
                @Override
                public boolean dispatchKeyEvent(KeyEvent keyEvent) {
                    if (keyEvent.getID() == KeyEvent.KEY_TYPED){
                        if (keyEvent.getKeyChar() == '\n'){
                            String barcodeString = barcode.toString();
                            barcode = new StringBuilder();
                            for (BarcodeScanEvent barcodeScanEvent : barcodeScanEvents){
                                barcodeScanEvent.onBarcodeScan(barcodeString);
                            }
                        }else{
                            barcode.append(keyEvent.getKeyChar());
                        }
                    }
                    return false;
                }
            });
        }
        connected = true;
    }
}
