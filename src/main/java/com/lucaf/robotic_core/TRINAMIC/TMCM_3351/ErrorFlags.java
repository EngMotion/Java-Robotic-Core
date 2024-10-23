package com.lucaf.robotic_core.TRINAMIC.TMCM_3351;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorFlags {
    /**
     * BIT 0: StallGuard stall detected
     */
    private boolean stallGuardStallDetected;

    /**
     * BIT 1: Over temperature
     */
    private boolean overTemperature;

    /**
     * BIT 2: Over temperature warning
     */
    private boolean overTemperatureWarning;

    /**
     * BIT 3: Short to GND A
     */
    private boolean shortToGND_A;

    /**
     * BIT 4: Short to GND B
     */
    private boolean shortToGND_B;

    /**
     * BIT 5: Open load A
     */
    private boolean openLoad_A;

    /**
     * BIT 6: Open load B
     */
    private boolean openLoad_B;

    /**
     * BIT 7: Stand still
     */
    private boolean standStill;

    public ErrorFlags(int code){
        stallGuardStallDetected = (code & 0x01) != 0;
        overTemperature = (code & 0x02) != 0;
        overTemperatureWarning = (code & 0x04) != 0;
        shortToGND_A = (code & 0x08) != 0;
        shortToGND_B = (code & 0x10) != 0;
        openLoad_A = (code & 0x20) != 0;
        openLoad_B = (code & 0x40) != 0;
        standStill = (code & 0x80) != 0;
    }

    public boolean hasError(){
        return stallGuardStallDetected || overTemperature || shortToGND_A || shortToGND_B || openLoad_A || openLoad_B || standStill;
    }

    public boolean hasWarning(){
        return overTemperatureWarning;
    }

    public String getErrorString(){
        StringBuilder sb = new StringBuilder();
        if(stallGuardStallDetected){
            sb.append("StallGuard stall detected\n");
        }
        if(overTemperature){
            sb.append("Over temperature\n");
        }
        if(shortToGND_A){
            sb.append("Short to GND A\n");
        }
        if(shortToGND_B){
            sb.append("Short to GND B\n");
        }
        if(openLoad_A){
            sb.append("Open load A\n");
        }
        if(openLoad_B){
            sb.append("Open load B\n");
        }
        if(standStill){
            sb.append("Stand still\n");
        }
        return sb.toString();
    }

    public String getWarningString(){
        StringBuilder sb = new StringBuilder();
        if(overTemperatureWarning){
            sb.append("Over temperature warning\n");
        }
        return sb.toString();
    }

    @Override
    public String toString(){
        return "ErrorFlags{" + "stallGuardStallDetected=" + stallGuardStallDetected + ", overTemperature=" + overTemperature + ", overTemperatureWarning=" + overTemperatureWarning + ", shortToGND_A=" + shortToGND_A + ", shortToGND_B=" + shortToGND_B + ", openLoad_A=" + openLoad_A + ", openLoad_B=" + openLoad_B + ", standStill=" + standStill + '}';
    }
}
