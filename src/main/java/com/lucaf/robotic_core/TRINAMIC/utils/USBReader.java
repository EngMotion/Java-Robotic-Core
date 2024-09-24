package com.lucaf.robotic_core.TRINAMIC.utils;

import com.lucaf.robotic_core.TRINAMIC.TMCLCommand;
import jssc.SerialPortException;

public interface USBReader {
    void onData(TMCLCommand data);
    void onSerialEcxception(SerialPortException e);
}
