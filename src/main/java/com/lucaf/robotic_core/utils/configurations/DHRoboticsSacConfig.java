package com.lucaf.robotic_core.utils.configurations;

import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
public class DHRoboticsSacConfig {
    String serial="COMY";
    int id = 1;
    int max_speed = 100;
    int acceleration = 100;
}