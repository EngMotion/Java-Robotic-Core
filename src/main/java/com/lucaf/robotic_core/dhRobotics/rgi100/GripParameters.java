package com.lucaf.robotic_core.dhRobotics.rgi100;

import com.lucaf.robotic_core.config.impl.BaseConfig;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class GripParameters extends BaseConfig {
    int turnSpeed = 100;
    int turnForce = 100;
    int gripSpeed = 100;
    int gripForce = 100;
}