package com.lucaf.robotic_core.KERN;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.concurrent.CompletableFuture.runAsync;

public class PCB_3 {
    private final SerialPort serialPort;
    private CountDownLatch latch = null;
    private String result = "";
    private final OutputStream outputStream;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    public PCB_3(SerialPort serialPort) {
        this.serialPort = serialPort;
        initReader();
        outputStream = serialPort.getOutputStream();
    }

    public PCB_3(String portName) {
        SerialPort[] ports = SerialPort.getCommPorts();
        SerialPort port = null;
        for (SerialPort p : ports) {
            if (p.getSystemPortName().equals(portName)) {
                port = p;
                break;
            }
        }
        if (port == null) {
            throw new RuntimeException("Port not found");
        }
        serialPort = port;
        serialPort.setComPortParameters(9600, 8, 1, SerialPort.NO_PARITY); // Baud rate, Data bits, Stop bits, Parità
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0); // Timeout lettura/scrittura
        if (!serialPort.openPort()) {
            throw new RuntimeException("Error opening port");
        }
        initReader();
        outputStream = serialPort.getOutputStream();
    }

    private String sendDataForResult(String data) throws IOException, InterruptedException {
        System.out.println("->" + data);
        latch = new CountDownLatch(1);
        outputStream.write(data.getBytes());
        outputStream.flush();
        latch.await(1000, TimeUnit.MILLISECONDS);
        latch = null;
        return result;
    }

    private String sendDataWithoutResult(String data) throws IOException {
        System.out.println("->" + data);
        outputStream.write(data.getBytes());
        outputStream.flush();
        return result;
    }

    public double getReading() throws IOException, InterruptedException {
        String result = sendDataForResult("w");
        Pattern pattern = Pattern.compile("[\\d.]+");
        Matcher matcher = pattern.matcher(result);
        if (!matcher.find()) {
            throw new RuntimeException("Invalid response");
        }
        result = matcher.group();
        return Double.parseDouble(result);
    }

    public Future<Boolean> tare(){
        return executor.submit(() -> {
            try {
                sendDataWithoutResult("t");
                while (true){
                    double getReading = getReading();
                    if (Math.abs(getReading) < 0.1) {
                        return true;
                    }
                    Thread.sleep(3000);
                }
            } catch (IOException e) {
                return false;
            }
        });

    }

    public void initReader() {
        Thread readThread = new Thread(() -> {
            try {
                InputStream in = serialPort.getInputStream();
                Scanner dataScanner = new Scanner(in);
                while (dataScanner.hasNextLine()) {
                    String line = dataScanner.nextLine();
                    result = line;
                    if (latch != null) {
                        latch.countDown();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        readThread.start();
    }
}