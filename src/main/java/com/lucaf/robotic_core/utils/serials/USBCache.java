package com.lucaf.robotic_core.utils.serials;

import com.lucaf.robotic_core.TRINAMIC.USB;
import jssc.SerialPortException;

import java.util.HashMap;
import java.util.Map;

public class USBCache {
    static Map<String, USB> usbHashMap = new HashMap<>();

    public static USB getUSB(String portName) {
        if (usbHashMap.containsKey(portName)) {
            return usbHashMap.get(portName);
        }
        try {
            USB usb =new USB(portName);
            usbHashMap.put(portName, usb);
            return usb;
        } catch (SerialPortException e) {
            e.printStackTrace();
            return null;
        }
    }

}
