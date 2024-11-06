package com.lucaf.robotic_core.KERN;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PLJ_1200 {
    private final SerialPort serialPort;
    private CountDownLatch latch = null;
    private String result = "";
    private final OutputStream outputStream;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    public PLJ_1200(SerialPort serialPort) {
        this.serialPort = serialPort;
        initReader();
        outputStream = serialPort.getOutputStream();
    }

    public PLJ_1200(String portName) {
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
        latch = new CountDownLatch(1);
        outputStream.write(data.getBytes());
        outputStream.flush();
        latch.await(1000, TimeUnit.MILLISECONDS);
        latch = null;
        return result;
    }

    private String sendDataWithoutResult(String data) throws IOException {
        outputStream.write(data.getBytes());
        outputStream.flush();
        return result;
    }

    public String closePort() throws IOException {
        serialPort.closePort();
        System.out.println("Porta chiusa.");
        return result;
    }

    public double getReading() throws IOException, InterruptedException {
        //String result = sendDataForResult("w");
        Pattern pattern = Pattern.compile("[\\d.]+");
        Matcher matcher = pattern.matcher(result);
        if (!matcher.find()) {
            return -1;
        }
        result = matcher.group();
        return Double.parseDouble(result);
    }

    public Future<Boolean> tare(){
        return executor.submit(() -> {
            try {
                sendDataWithoutResult("T");
                int i = 0;
                while (true){
                    Thread.sleep(1000);
                    double getReading = getReading();
                    i++;
                    if (Math.abs(getReading) < 0.01) {
                        return true;
                    }
                    if (i == 10) {
                        System.out.println("Errore tara");
                        closePort();
                        System.exit(0);
                    }
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
