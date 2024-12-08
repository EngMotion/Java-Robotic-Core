package com.lucaf.robotic_core.utils.configurations;

import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
public class DHRoboticsGripperConfig {
    String serial="COMZ";
    int id = 1;
    int turn_speed = 100;
    int turn_force = 100;
    int grip_speed = 100;
    int grip_force = 100;
}