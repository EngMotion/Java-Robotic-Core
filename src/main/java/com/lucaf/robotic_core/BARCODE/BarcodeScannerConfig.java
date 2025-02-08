package com.lucaf.robotic_core.BARCODE;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class BarcodeScannerConfig {
    @Comment("Types can be: SERIAL, USB")
    String type = "SERIAL";
    String serial = "COMX";
}