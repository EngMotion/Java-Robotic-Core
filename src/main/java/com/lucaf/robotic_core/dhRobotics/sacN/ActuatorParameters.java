package com.lucaf.robotic_core.dhRobotics.sacN;

import com.lucaf.robotic_core.config.impl.BaseConfig;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class ActuatorParameters extends BaseConfig {
    protected int speed = 100;
    protected int acceleration = 100;
}
