package com.lucaf.robotic_core.wenglor.ep8e001.config;

import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Configuration
public class WenglorEP8E001Config {
    private String ipAddress = "192.168.1.1";
    private int    port      = 80;
    private int    masterID  = 1;
    private String username  = "ADMIN";
    private String password  = "ADMIN";
    private HashMap<String, String> devices = new HashMap<>(Map.of(
            "master1port1", "master1port1",
            "master1port2", "master1port2",
            "master1port3", "master1port3",
            "master1port4", "master1port4"
    ));
}
