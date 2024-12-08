package com.lucaf.robotic_core.utils.configurations;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
public class NanotecConfig {
    String serial="COMX";
    int id = 1;
    int speed = 100;
    int max_speed = 100;
    int acceleration = 100;
    int deceleration = 100;
    int max_current = 2100;
    int rated_current = 2100;
    boolean auto_disabler = true;
    @Comment("Set -1 to disable the brake")
    int brake = -1;
    int ratio = 1;
}