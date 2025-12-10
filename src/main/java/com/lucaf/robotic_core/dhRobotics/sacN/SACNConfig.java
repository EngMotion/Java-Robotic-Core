package com.lucaf.robotic_core.dhRobotics.sacN;

import com.lucaf.robotic_core.config.impl.BaseConfig;
import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
public class SACNConfig extends BaseConfig {
    String serial="COMY";
    int id = 1;
    ActuatorParameters travelParameters = new ActuatorParameters();
}