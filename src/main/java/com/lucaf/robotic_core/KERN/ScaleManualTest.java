package com.lucaf.robotic_core.KERN;

import com.lucaf.robotic_core.KERN.PCB.PCB_3;
import com.lucaf.robotic_core.KERN.PLJ.PLJ_1200;
import com.lucaf.robotic_core.SerialParams;
import com.lucaf.robotic_core.dataInterfaces.serial.SerialPortCache;
import com.lucaf.robotic_core.dataInterfaces.serial.SimpleSerialConnector;
import com.lucaf.robotic_core.impl.ScaleInterface;
import jssc.SerialPort;

import java.util.Scanner;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Manual console harness to exercise a KERN scale over a real serial connection.
 * <p>
 * Usage: {@code ScaleManualTest <COM port> [PCB|PLJ]}
 * <p>
 * For the PCB make sure the scale is set to the {@code "rE CR"} data-transmission mode
 * (menu → Pr → rE CR) so that it accepts the remote ASCII commands.
 */
public class ScaleManualTest {

    public static void main(String[] args) throws Exception {
        String com = args.length > 0 ? args[0] : "COM3";
        String type = args.length > 1 ? args[1].toUpperCase() : "PCB";

        SerialParams params = new SerialParams(); // 9600, 8, N, 1
        SerialPort port = SerialPortCache.getSerialPort(com, params);
        SimpleSerialConnector connector = new SimpleSerialConnector(port, type);

        Consumer<Double> printer = w -> System.out.println("[stream] weight = " + w);
        ScaleInterface scale;
        if ("PLJ".equals(type)) {
            scale = new PLJ_1200(connector, printer);
        } else {
            scale = new PCB_3(connector, printer);
        }

        boolean initialized = scale.initialize().get();
        System.out.println("Initialized: " + initialized + " (connected=" + scale.isConnected() + ")");

        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.println("\nCommands: r=read, s=stable read, t=tare, e=enable stream, d=disable stream, q=quit");
            String line = scanner.nextLine().trim().toLowerCase();
            switch (line) {
                case "r":
                    System.out.println("read = " + scale.read());
                    break;
                case "s":
                    System.out.println("readStable = " + scale.readStable());
                    break;
                case "t":
                    Future<Boolean> tare = scale.tare();
                    System.out.println("tare = " + tare.get());
                    break;
                case "e":
                    scale.enableEventReading();
                    System.out.println("event reading enabled");
                    break;
                case "d":
                    scale.disableEventReading();
                    System.out.println("event reading disabled");
                    break;
                case "q":
                    running = false;
                    break;
                default:
                    System.out.println("unknown command");
            }
        }

        scale.shutdown();
        scanner.close();
        System.out.println("Done.");
    }
}
