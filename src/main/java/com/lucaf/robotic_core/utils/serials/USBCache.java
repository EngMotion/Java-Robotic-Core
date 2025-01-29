package com.lucaf.robotic_core.utils.serials;

import com.lucaf.robotic_core.TRINAMIC.USB;
import jssc.SerialPortException;

import java.util.HashMap;
import java.util.Map;

public class USBCache {
    static Map<String, USB> usbHashMap = new HashMap<>();

    /**
     * Get the USB object from the cache or create a new one
     * @param portName the port name
     * @return the USB object
     */
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

    /**
     * Close all the USB objects
     */
    public static void closeALL() {
        for (Map.Entry<String, USB> entry : usbHashMap.entrySet()) {
            try {
                entry.getValue().close();
                usbHashMap.remove(entry.getKey());
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }

}
