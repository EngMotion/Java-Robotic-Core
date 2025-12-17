package com.lucaf.robotic_core.dhRobotics.rgi100;

import com.lucaf.robotic_core.config.impl.BaseConfig;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class GripParameters extends BaseConfig {
    protected int turnSpeed = 100;
    protected int turnForce = 100;
    protected int gripSpeed = 100;
    protected int gripForce = 100;
}