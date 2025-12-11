package com.lucaf.robotic_core.UI;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.jthemedetecor.OsThemeDetector;

public class ThemeSetup {
    public static void setupTheme() {
        OsThemeDetector detector = OsThemeDetector.getDetector();
        if (detector.isDark()){
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
    }
}
