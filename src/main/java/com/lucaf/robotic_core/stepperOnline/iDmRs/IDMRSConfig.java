package com.lucaf.robotic_core.stepperOnline.iDmRs;

import com.lucaf.robotic_core.SerialParams;
import com.lucaf.robotic_core.config.impl.BaseConfig;
import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
public class IDMRSConfig extends BaseConfig {
    SerialParams connection = new SerialParams();
    int turnResolution = 10000;
    int ratio = 1;
    boolean positioningMode = true;
    boolean relativePositioning = true;
    TravelParameters travelParameters = new TravelParameters();
    HomingConfig homing = new HomingConfig();
}