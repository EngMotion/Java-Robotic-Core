package com.lucaf.robotic_core.dhRobotics.sacN;

import com.lucaf.robotic_core.SerialParams;
import com.lucaf.robotic_core.config.impl.BaseConfig;
import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
public class SACNConfig extends BaseConfig {
    protected SerialParams connection = new SerialParams();
    protected ActuatorParameters travelParameters = new ActuatorParameters();
}