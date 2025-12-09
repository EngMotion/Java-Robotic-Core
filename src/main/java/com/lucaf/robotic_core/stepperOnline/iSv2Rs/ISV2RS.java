package com.lucaf.robotic_core.stepperOnline.iSv2Rs;

import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.State;
import com.lucaf.robotic_core.stepperOnline.iDmRs.IDM_RS;

import java.util.HashMap;

public class ISV2RS extends IDM_RS {
    /**
     * Constructor of the class
     *
     * @param rs485             the Modbus master connection
     * @param id                the id of the device
     * @param state             the state of the device
     * @param notifyStateChange the state class with the onStateChange method
     * @param logger            the logger
     */
    public ISV2RS(ModbusSerialMaster rs485, byte id, HashMap<String, Object> state, State notifyStateChange, Logger logger) {
        super(rs485, id, state, notifyStateChange, logger);
    }
}
