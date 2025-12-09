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
    private boolean emergency_stop_state = false;

    /**
     * The state of the device
     * 0: not prepared
     * 1: ready
     */
    private boolean power_supply_state = false;

    /**
     * The state of the device
     */
    private boolean thrust = false;

    /**
     * The state of the device
     * 0: disabled
     * 1: enabled
     */
    private boolean is_enabled = false;

    /**
     * The state of the device
     * 0: normal
     * 1: error
     */
    private boolean has_alarm = false;

    /**
     * The state of the device
     * 0: not in motion
     * 1: in motion
     */
    private boolean is_in_motion = false;

    /**
     * The state of the device
     * 0: no zero
     * 1: compleated zero
     */
    private boolean is_back_home = false;


    /**
     * The state of the device
     * 0: not in place
     * 1: in place
     */
    private boolean is_in_place = false;


    public Status() {
    }

    public Status(int code){
        //int to 16 bit binary
        String binary = Integer.toBinaryString(code);
        while (binary.length() < 16) {
            binary = "0" + binary;
        }
        emergency_stop_state = binary.charAt(0) == '1';
        power_supply_state = binary.charAt(1) == '1';
        thrust = binary.charAt(2) == '1';
        is_enabled = binary.charAt(5) == '1';
        has_alarm = binary.charAt(6) == '1';
        is_in_motion = binary.charAt(7) == '1';
        is_back_home = binary.charAt(9) == '1';
        is_in_place = binary.charAt(10) == '1';
    }

    public String toString(){
        return "Emergency Stop: " + emergency_stop_state + "\n" +
                "Power Supply: " + power_supply_state + "\n" +
                "Thrust: " + thrust + "\n" +
                "Enabled: " + is_enabled + "\n" +
                "Alarm: " + has_alarm + "\n" +
                "In Motion: " + is_in_motion + "\n" +
                "Back Home: " + is_back_home + "\n" +
                "In Place: " + is_in_place + "\n";
    }

}
