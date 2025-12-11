package com.lucaf.robotic_core;

import jssc.SerialPort;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SerialParams {
    String comPort = "COM1";
    int unitId = 1;
    int baudrate = SerialPort.BAUDRATE_9600;
    int databits = SerialPort.DATABITS_8;
    int stopbits = SerialPort.STOPBITS_1;
    int parity = SerialPort.PARITY_NONE;

    public SerialParams(int baudrate, int databits, int stopbits, int parity) {
        this.baudrate = baudrate;
        this.databits = databits;
        this.stopbits = stopbits;
        this.parity = parity;
    }
}
