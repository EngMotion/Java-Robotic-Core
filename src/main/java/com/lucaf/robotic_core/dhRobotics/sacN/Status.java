package com.lucaf.robotic_core.dhRobotics.sacN;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Status {
    /**
     * The state of the device
     * 0: normal
     * 1: emergency stop
     */
    private boolean emergencyStopState = false;

    /**
     * The state of the device
     * 0: not prepared
     * 1: ready
     */
    private boolean powerSupplyState = false;

    /**
     * The state of the device
     */
    private boolean thrust = false;

    /**
     * The state of the device
     * 0: disabled
     * 1: enabled
     */
    private boolean isEnabled = false;

    /**
     * The state of the device
     * 0: normal
     * 1: error
     */
    private boolean hasAlarm = false;

    /**
     * The state of the device
     * 0: not in motion
     * 1: in motion
     */
    private boolean isInMotion = false;

    /**
     * The state of the device
     * 0: no zero
     * 1: compleated zero
     */
    private boolean isBackHome = false;


    /**
     * The state of the device
     * 0: not in place
     * 1: in place
     */
    private boolean isInPlace = false;


    /**
     * Default constructor
     */
    public Status() {}

    /**
     * Constructor from code
     * @param code the status code
     */
    public Status(int code){
        emergencyStopState = ((code >> 15) & 1) == 1;
        powerSupplyState = ((code >> 14) & 1) == 1;
        thrust = ((code >> 13) & 1) == 1;
        isEnabled = ((code >> 10) & 1) == 1;
        hasAlarm = ((code >> 9) & 1) == 1;
        isInMotion = ((code >> 8) & 1) == 1;
        isBackHome = ((code >> 6) & 1) == 1;
        isInPlace = ((code >> 5) & 1) == 1;
    }

    /**
     * Convert the status to code
     * @return the status code
     */
    public int toCode(){
        int code = 0;
        code |= (emergencyStopState ? 1 : 0) << 15;
        code |= (powerSupplyState ? 1 : 0) << 14;
        code |= (thrust ? 1 : 0) << 13;
        code |= (isEnabled ? 1 : 0) << 10;
        code |= (hasAlarm ? 1 : 0) << 9;
        code |= (isInMotion ? 1 : 0) << 8;
        code |= (isBackHome ? 1 : 0) << 6;
        code |= (isInPlace ? 1 : 0) << 5;
        return code;
    }

    /**
     * String representation of the status
     * @return the string representation
     */
    public String toString(){
        return "Emergency Stop: " + emergencyStopState + "\n" +
                "Power Supply: " + powerSupplyState + "\n" +
                "Thrust: " + thrust + "\n" +
                "Enabled: " + isEnabled + "\n" +
                "Alarm: " + hasAlarm + "\n" +
                "In Motion: " + isInMotion + "\n" +
                "Back Home: " + isBackHome + "\n" +
                "In Place: " + isInPlace + "\n";
    }
}
