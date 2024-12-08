package com.lucaf.robotic_core.utils.serials;

import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.lucaf.robotic_core.Pair;
import com.nanotec.nanolib.*;
import com.nanotec.nanolib.helper.NanolibHelper;

import java.util.HashMap;
import java.util.Map;

public class NanoLibCache {
    static Map<String, Pair<DeviceHandle, NanolibHelper>> nanoLibHashMap = new HashMap<>();
    static Map<String, BusHardwareId> busHardwareHashMap = new HashMap<>();

    public static BusHardwareId getNanoLibBus(String com, NanolibHelper nanolibHelper) throws Exception {
        if (busHardwareHashMap.containsKey(com)) return busHardwareHashMap.get(com);
        BusHWIdVector busHwIds = nanolibHelper.getBusHardware();
        if (busHwIds.isEmpty()) {
            throw new Exception("No bus hardware found");
        }
        BusHardwareId busHwId = null;
        for (BusHardwareId adapter : busHwIds) {
            if (adapter.getName().contains(com)) {
                busHwId = adapter;
            }
        }
        if (busHwId == null) {
            throw new Exception("No bus hardware found");
        }
        BusHardwareOptions busHwOptions = nanolibHelper.createBusHardwareOptions(busHwId);
        nanolibHelper.openBusHardware(busHwId, busHwOptions);
        busHardwareHashMap.put(com, busHwId);
        return busHwId;
    }

    public static Pair<DeviceHandle, NanolibHelper> getNanoLib(String com, int id, String name) throws Exception {
        String key = com + id + name;
        key = key.replaceAll("\\s", "");
        if (nanoLibHashMap.containsKey(key)) return nanoLibHashMap.get(key);
        NanolibHelper nanolibHelper;
        if (!nanoLibHashMap.isEmpty()){
            Pair<DeviceHandle, NanolibHelper> pair = nanoLibHashMap.values().iterator().next();
            nanolibHelper = pair.second;
        }else{
            nanolibHelper = new NanolibHelper();
            nanolibHelper.setup();
        }
        BusHardwareId axis_b_hardware = getNanoLibBus(com, nanolibHelper);
        DeviceId axis_b_device = new DeviceId(
                axis_b_hardware,
                id,
                name);
        DeviceHandle axis_b_device_handle = nanolibHelper.createDevice(axis_b_device);
        nanolibHelper.connectDevice(axis_b_device_handle);
        nanoLibHashMap.put(key, new Pair<>(axis_b_device_handle, nanolibHelper));
        return new Pair<>(axis_b_device_handle, nanolibHelper);
    }
}
