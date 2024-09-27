package com.lucaf.robotic_core.NANOTEC;

import com.lucaf.robotic_core.exception.SerialConnectionException;
import com.nanotec.nanolib.*;
import com.nanotec.nanolib.helper.NanolibHelper;
import lombok.Getter;

public class NanotecRTU {

    @Getter
    private final NanolibHelper nanolibHelper;

    private BusHardwareId busHwId;

    public NanotecRTU(String port) throws SerialConnectionException {
        try {
            nanolibHelper = new NanolibHelper();
            nanolibHelper.setup();
            BusHWIdVector busHwIds = nanolibHelper.getBusHardware();
            if (busHwIds.isEmpty()) {
                throw new SerialConnectionException("No bus hardware found");
            }
            busHwId = null;
            for (BusHardwareId adapter : busHwIds) {
                if (adapter.getName().contains(port)) {
                    busHwId = adapter;
                }
            }
            if (busHwId == null) {
                throw new SerialConnectionException("No bus hardware found");
            }
            BusHardwareOptions busHwOptions = nanolibHelper.createBusHardwareOptions(busHwId);
            nanolibHelper.openBusHardware(busHwId, busHwOptions);
        }catch (Exception e){
            throw new SerialConnectionException(e.getMessage());
        }
    }

    public DeviceHandle createDevice(int nodeId, String name) throws SerialConnectionException {
        try {
            return nanolibHelper.createDevice(new DeviceId(busHwId, nodeId, name));
        } catch (NanolibHelper.NanolibException e) {
            throw new SerialConnectionException(e.getMessage());
        }
    }

    public void close() throws SerialConnectionException {
        try {
            nanolibHelper.closeBusHardware(busHwId);
        } catch (NanolibHelper.NanolibException e) {
            throw new SerialConnectionException(e.getMessage());
        }
    }


}
