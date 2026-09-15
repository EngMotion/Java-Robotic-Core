package com.lucaf.robotic_core;

import de.exlll.configlib.Configuration;
import jssc.SerialPort;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Configuration
public class SerialParams {
    protected String comPort = "COM1";
    protected int unitId = 1;
    protected int baudrate = SerialPort.BAUDRATE_9600;
    protected int databits = SerialPort.DATABITS_8;
    protected int stopbits = SerialPort.STOPBITS_1;
    protected int parity = SerialPort.PARITY_NONE;

    public SerialParams(int baudrate, int databits, int stopbits, int parity) {
        this.baudrate = baudrate;
        this.databits = databits;
        this.stopbits = stopbits;
        this.parity = parity;
    }
}
