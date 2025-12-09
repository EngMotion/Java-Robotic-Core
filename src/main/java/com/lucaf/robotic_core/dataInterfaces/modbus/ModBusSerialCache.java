package com.lucaf.robotic_core.dataInterfaces.modbus;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.util.SerialParameters;

import java.util.HashMap;
import java.util.Map;

public class ModBusSerialCache {
    static Map<String, ModbusSerialMaster> modbusSerialMasterHashMap = new HashMap<>();

    /**
     * Get the ModBus master for the specified COM port
     *
     * @param com        the COM port
     * @param parameters the serial parameters
     * @return the ModBus master
     * @throws Exception if the connection fails
     */
    public static ModbusSerialMaster getModBusMasterCom(String com, SerialParameters parameters) throws Exception {
        if (modbusSerialMasterHashMap.containsKey(com)) return modbusSerialMasterHashMap.get(com);
        ModbusSerialMaster master = new ModbusSerialMaster(
                parameters,
                1000,
                2
        );
        master.connect();
        modbusSerialMasterHashMap.put(com, master);
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
