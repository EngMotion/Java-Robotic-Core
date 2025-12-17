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
    protected SerialParams connection = new SerialParams();
    protected int turnResolution = 10000;
    protected int ratio = 1;
    protected boolean positioningMode = true;
    protected boolean relativePositioning = true;
    protected TravelParameters travelParameters = new TravelParameters();
    protected HomingConfig homing = new HomingConfig();
}