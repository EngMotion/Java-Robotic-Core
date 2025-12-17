package com.lucaf.robotic_core.stepperOnline.iDmRs;

import com.lucaf.robotic_core.config.impl.BaseConfig;
import de.exlll.configlib.Configuration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Configuration
@AllArgsConstructor
@NoArgsConstructor
public class TravelParameters extends BaseConfig {
    int speed = 100;
    int acceleration = 100;
    int deceleration = 100;
    int rampMode = 1;
}
