package com.lucaf.robotic_core.stepperOnline.dm556Rs;

import com.lucaf.robotic_core.State;
import com.lucaf.robotic_core.dataInterfaces.impl.RegisterInterface;
import com.lucaf.robotic_core.stepperOnline.iDmRs.IDMRS;

import java.util.HashMap;

public class DM556RS extends IDMRS {

    public DM556RS(RegisterInterface registerInterface, HashMap<String, Object> state, State notifyStateChange) {
        super(registerInterface, state, notifyStateChange);
    }

    public DM556RS(RegisterInterface registerInterface, HashMap<String, Object> state) {
        super(registerInterface, state);
    }

    public DM556RS(RegisterInterface registerInterface) {
        super(registerInterface);
    }
}
