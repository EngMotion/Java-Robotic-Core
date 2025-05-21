package com.lucaf.robotic_core.utils.serials;

import com.lucaf.robotic_core.MOXMEC.Serial;
import com.lucaf.robotic_core.SerialParams;
import jssc.SerialPortException;

import java.util.HashMap;
import java.util.Map;

public class MoxMecSerialCache {
    static Map<String, Serial> usbHashMap = new HashMap<>();

    /**
     * Get the USB object from the cache or create a new one
     * @param portName the port name
     * @return the USB object
     */
    public static Serial getSerial(String portName, SerialParams params) throws SerialPortException {
        if (usbHashMap.containsKey(portName)) {
            return usbHashMap.get(portName);
        }
        Serial usb = new Serial(portName, params);
        usbHashMap.put(portName, usb);
        return usb;
    }

    /**
     * Close all the USB objects
     */
    public static void closeALL() {
        for (Map.Entry<String, Serial> entry : usbHashMap.entrySet()) {
            try {
                entry.getValue().close();
                usbHashMap.remove(entry.getKey());
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }
}
