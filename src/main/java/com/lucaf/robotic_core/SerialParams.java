package com.lucaf.robotic_core;

import de.exlll.configlib.Configuration;
import jssc.SerialPort;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Configuration
public class SerialParams {
    protected String comPort = "COM1";
    protected int unitId = 1;
    protected int baudrate = SerialPort.BAUDRATE_9600;
    protected int databits = SerialPort.DATABITS_8;
    protected int stopbits = SerialPort.STOPBITS_1;
    protected int parity = SerialPort.PARITY_NONE;
    protected boolean linePowered = false;

    // Ohranjen star konstruktor s 6 parametri, da se ne pokvarijo obstoječi klici
    public SerialParams(String comPort, int unitId, int baudrate, int databits, int stopbits, int parity) {
        this.comPort = comPort;
        this.unitId = unitId;
        this.baudrate = baudrate;
        this.databits = databits;
        this.stopbits = stopbits;
        this.parity = parity;
    }

    public SerialParams(int baudrate, int databits, int stopbits, int parity) {
        this.baudrate = baudrate;
        this.databits = databits;
        this.stopbits = stopbits;
        this.parity = parity;
    }
}