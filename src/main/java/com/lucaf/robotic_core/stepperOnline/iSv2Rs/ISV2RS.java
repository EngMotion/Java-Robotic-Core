package com.lucaf.robotic_core.stepperOnline.iSv2Rs;

import com.lucaf.robotic_core.State;
import com.lucaf.robotic_core.dataInterfaces.impl.RegisterInterface;
import com.lucaf.robotic_core.stepperOnline.iDmRs.IDMRS;

import java.io.IOException;
import java.util.HashMap;

import static com.lucaf.robotic_core.stepperOnline.iDmRs.Constants.PEAK_CURRENT;


public class ISV2RS extends IDMRS {

    @Override
    public void setPeakCurrent(int peakCurrent) throws IOException {
        // Do nothing
    }

    @Override
    public int getPeakCurrent() throws IOException {
        return 0;
    }

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
