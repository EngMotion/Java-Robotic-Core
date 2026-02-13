package com.lucaf.robotic_core.stepperOnline.iDmRs;

import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
public class HomingConfig extends TravelParameters {
    protected int speedLow = 100;
    protected boolean enabled = false;
    protected int position = 0;
    protected int sensor = 1;
    protected int timeout = 1500;
    protected long startPosition = 0;
    protected boolean positiveDirection = true;
    protected HomingMethod method = HomingMethod.HOMING_SWITCH;
}
