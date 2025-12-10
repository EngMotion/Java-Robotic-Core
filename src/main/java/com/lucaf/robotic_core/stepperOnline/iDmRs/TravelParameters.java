package com.lucaf.robotic_core.stepperOnline.iDmRs;

import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
public class TravelParameters {
    int speed = 100;
    int acceleration = 100;
    int deceleration = 100;
    int rampMode = 0;
}
