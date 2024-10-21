package com.kern.scale;

import com.fazecast.jSerialComm.SerialPort;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class SerialCommunication {

    public static void main(String[] args) {
        // Elenco delle porte disponibili
        SerialPort[] ports = SerialPort.getCommPorts();
        System.out.println("Porte seriali disponibili:");
        for (int i = 0; i < ports.length; i++) {
            System.out.println(i + ": " + ports[i].getSystemPortName());
        }

        /*// Scegli la porta seriale
        Scanner scanner = new Scanner(System.in);
        System.out.print("Seleziona la porta (numero): ");
        int portIndex = scanner.nextInt();
        SerialPort serialPort = ports[portIndex];*/
        SerialPort serialPort = ports[0];

        // Impostazioni della porta seriale
        serialPort.setComPortParameters(9600, 8, 1, SerialPort.NO_PARITY); // Baud rate, Data bits, Stop bits, Parità
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0); // Timeout lettura/scrittura

        if (serialPort.openPort()) {
            System.out.println("Porta aperta correttamente.");
        } else {
            System.out.println("Errore nell'aprire la porta.");
            return;
        }

        // Creazione di thread per gestire lettura e scrittura contemporaneamente
        Thread readThread = new Thread(() -> {
            try {
                InputStream in = serialPort.getInputStream();
                System.out.println("In attesa di dati dalla porta seriale...");
                Scanner dataScanner = new Scanner(in);
                while (dataScanner.hasNextLine()) {
                    System.out.println("Ricevuto: " + dataScanner.nextLine());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread writeThread = new Thread(() -> {
            try {
                OutputStream out = serialPort.getOutputStream();
                Scanner inputScanner = new Scanner(System.in);
                while (true) {
                    System.out.print("Inserisci una stringa da inviare via seriale (digita 'exit' per uscire): ");
                    String dataToSend = inputScanner.nextLine();
                    if (dataToSend.equalsIgnoreCase("exit")) {
                        break;
                    }
                    out.write(dataToSend.getBytes());
                    out.flush();
                    System.out.println("Dati inviati: " + dataToSend);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Avvio dei thread di lettura e scrittura
        readThread.start();
        writeThread.start();

        try {
            // Attendi che il thread di scrittura termini prima di chiudere la porta
            writeThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Chiudi la porta seriale
        serialPort.closePort();
        System.out.println("Porta chiusa.");
    }
}
