package com.lucaf.robotic_core.stepperOnline.iSv2Rs;

import com.lucaf.robotic_core.State;
import com.lucaf.robotic_core.dataInterfaces.impl.RegisterInterface;
import com.lucaf.robotic_core.stepperOnline.iDmRs.IDMRS;

import java.util.HashMap;


public class ISV2RS extends IDMRS {
    public ISV2RS(RegisterInterface registerInterface, HashMap<String, Object> state, State notifyStateChange) {
        super(registerInterface, state, notifyStateChange);
    }

    public ISV2RS(RegisterInterface registerInterface, HashMap<String, Object> state) {
        super(registerInterface, state);
    }

    public ISV2RS(RegisterInterface registerInterface) {
        super(registerInterface);
    }
}
