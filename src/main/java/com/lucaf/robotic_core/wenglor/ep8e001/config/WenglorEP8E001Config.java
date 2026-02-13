package com.lucaf.robotic_core.wenglor.ep8e001.config;

import de.exlll.configlib.Configuration;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Configuration
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WenglorEP8E001Config {
    @Builder.Default
    private String ipAddress = "192.168.1.1";
    @Builder.Default
    private int    port      = 80;
    @Builder.Default
    private int    masterID  = 1;
    @Builder.Default
    private String username  = "ADMIN";
    @Builder.Default
    private String password  = "ADMIN";
    @Builder.Default
    private HashMap<String, String> devices = new HashMap<>(Map.of(
            "master1port1", "master1port1",
            "master1port2", "master1port2",
            "master1port3", "master1port3",
            "master1port4", "master1port4"
    ));
}
