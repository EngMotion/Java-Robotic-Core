package com.lucaf.robotic_core.dataInterfaces.modbus;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import com.lucaf.robotic_core.SerialParams;

import java.util.HashMap;
import java.util.Map;

public class ModBusSerialCache {
    static Map<String, ModbusSerialMaster> modbusSerialMasterHashMap = new HashMap<>();

    static SerialParameters convertSerialParams(SerialParams parameters) {
        SerialParameters serialParameters = new SerialParameters();
        serialParameters.setPortName(parameters.getComPort());
        serialParameters.setBaudRate(parameters.getBaudrate());
        serialParameters.setDatabits(parameters.getDatabits());
        serialParameters.setStopbits(parameters.getStopbits());
        serialParameters.setParity(parameters.getParity());
        serialParameters.setEncoding(Modbus.SERIAL_ENCODING_RTU);
        serialParameters.setEcho(false);
        return serialParameters;
    }

    /**
     * Get the ModBus master for the specified COM port
     *
     * @param parameters the serial parameters
     * @return the ModBus master
     * @throws Exception if the connection fails
     */
    public static ModbusSerialMaster getModBusMasterCom(SerialParams parameters) throws Exception {
        if (modbusSerialMasterHashMap.containsKey(parameters.getComPort())) return modbusSerialMasterHashMap.get(parameters.getComPort());
        ModbusSerialMaster master = new ModbusSerialMaster(
                convertSerialParams(parameters),
                1000,
                2
        );
        master.connect();
        modbusSerialMasterHashMap.put(parameters.getComPort(), master);
        return master;
    }

    /**
     * Close all the connections
     */
    public static void closeAll() {
        for (Map.Entry<String, ModbusSerialMaster> entry : modbusSerialMasterHashMap.entrySet()) {
            if (entry.getValue().isConnected()) {
                entry.getValue().disconnect();
            }
            modbusSerialMasterHashMap.remove(entry.getKey());
        }
    }
}
