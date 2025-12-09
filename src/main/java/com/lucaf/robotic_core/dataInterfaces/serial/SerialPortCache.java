package com.lucaf.robotic_core.dataInterfaces.serial;

import com.lucaf.robotic_core.SerialParams;
import jssc.SerialPort;

import java.util.HashMap;
import java.util.Map;

public class SerialPortCache {
    static Map<String, SerialPort> serialPortHashMap = new HashMap<>();

    /**
     * Get the SerialPort master for the specified COM port
     *
     * @param com        the COM port
     * @param parameters the serial parameters
     * @return the SerialPort master
     * @throws Exception if the connection fails
     */
    public static SerialPort getSerialPort(String com, SerialParams parameters) throws Exception {
        if (serialPortHashMap.containsKey(com)) return serialPortHashMap.get(com);
        SerialPort port = new SerialPort(com);
        if (port.isOpened()) port.closePort();
        port.openPort();
        port.setParams(
                parameters.getBaudrate(),
                parameters.getDatabits(),
                parameters.getStopbits(),
                parameters.getParity()
        );
        port.setEventsMask(SerialPort.MASK_RXCHAR);
        serialPortHashMap.put(com, port);
        return port;
    }

    /**
     * Close all the connections
     */
    public static void closeAll() {
        for (Map.Entry<String, SerialPort> entry : serialPortHashMap.entrySet()) {
            try {
                if (entry.getValue().isOpened()) {
                    entry.getValue().closePort();
                }
            } catch (Exception ignored) {}
            serialPortHashMap.remove(entry.getKey());
        }
    }
}
