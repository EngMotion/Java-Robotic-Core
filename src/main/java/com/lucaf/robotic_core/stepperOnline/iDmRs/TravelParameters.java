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
    protected int speed = 100;
    protected int acceleration = 100;
    protected int deceleration = 100;
    protected int path = 0;
}
