package com.lucaf.robotic_core.stepperOnline.iDmRs;

import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
public class HomingConfig extends TravelParameters {
    boolean enabled = false;
    int position = 0;
    int sensor = 1;
    int timeout = 1500;
    long startPosition = 0;
    boolean positiveDirection = true;
    HomingMethod method = HomingMethod.HOMING_SWITCH;
}
