package com.lucaf.robotic_core.dhRobotics.rgi100;

import com.lucaf.robotic_core.SerialParams;
import com.lucaf.robotic_core.config.impl.BaseConfig;
import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
public class RGI100Config extends BaseConfig {
    protected SerialParams connection = new SerialParams();
    protected GripParameters travelParameters = new GripParameters();
}