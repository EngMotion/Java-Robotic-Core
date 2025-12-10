package com.lucaf.robotic_core.dhRobotics.rgi100;

import com.lucaf.robotic_core.config.impl.BaseConfig;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class GripParameters extends BaseConfig {
    int turn_speed = 100;
    int turn_force = 100;
    int grip_speed = 100;
    int grip_force = 100;
}