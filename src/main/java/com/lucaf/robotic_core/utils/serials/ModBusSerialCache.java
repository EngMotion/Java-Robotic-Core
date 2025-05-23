package com.lucaf.robotic_core.utils.serials;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.util.SerialParameters;

import java.util.HashMap;
import java.util.Map;

public class ModBusSerialCache {
    static Map<String, ModbusSerialMaster> modbusSerialMasterHashMap = new HashMap<>();

    /**
     * Get the ModBus master for the specified COM port
     * @param com the COM port
     * @param baudrate the baudrate
     * @return the ModBus master
     * @throws Exception if the connection fails
     */
    public static ModbusSerialMaster getModBusMasterCom(String com, int baudrate) throws Exception {
        if (modbusSerialMasterHashMap.containsKey(com)) return modbusSerialMasterHashMap.get(com);
        SerialParameters params = new SerialParameters();
        params.setPortName(com);
        params.setBaudRate(baudrate);
        params.setDatabits(8);
        params.setParity("None");
        params.setStopbits(1);
        params.setEncoding(Modbus.SERIAL_ENCODING_RTU);
        ModbusSerialMaster master = new ModbusSerialMaster(
                params,
                1000,
                0
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
            if (entry.getValue().isConnected()){
                entry.getValue().disconnect();
            }
            modbusSerialMasterHashMap.remove(entry.getKey());
        }
    }
}
