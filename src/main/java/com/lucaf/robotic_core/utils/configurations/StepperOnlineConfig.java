package com.lucaf.robotic_core.utils.configurations;

import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
public class StepperOnlineConfig {
    String serial="COMX";
    int id = 1;
    int speed = 100;
    int acceleration = 100;
    int deceleration = 100;
    int homing_speed = 100;
    int homing_acceleration = 100;
    int homing_deceleration = 100;
    int turn_resolution = 10000;
    int ratio = 1;
}