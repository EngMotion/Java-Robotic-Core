package com.lucaf.robotic_core.STEPPERONLINE.iDM_RS;

import lombok.Getter;
import lombok.Setter;

/**
 * Class that represents the digital input settings of the motor.
 */
@Getter
@Setter
public class DigitalInput {

    @Getter
    public enum Mode {
        INVALID(0x00),
        ALARM_CLEARING(0x07),
        ENABLE(0x08),
        TRIGGER_COMMAND(0x20),
        TRIGGER_HOMING(0x21),
        EMERGENCY_STOP(0x22),
        JOG_POSITIVE(0x23),
        JOG_NEGATIVE(0x24),
        POSITIVE_LIMIT(0x25),
        NEGATIVE_LIMIT(0x26),
        HOME_SWITCH(0x27),
        PATH_ADDRESS_0(0x28),
        PATH_ADDRESS_1(0x29),
        PATH_ADDRESS_2(0x2A),
        PATH_ADDRESS_3(0x2B),
        JOG_VELOCITY(0x2C);
        private final byte value;

        Mode(int value) {
            this.value = (byte) value;
        }

        public static Mode fromValue(byte value) {
            for (Mode mode : Mode.values()) {
                if (mode.value == value) {
                    return mode;
                }
            }
            return INVALID;
        }

        public static Mode fromInt(int value) {
            for (Mode mode : Mode.values()) {
                if (mode.value == (byte) value) {
                    return mode;
                }
            }
            return INVALID;
        }
    }

    /**
     * Bit 0-7: Path Mode
     */
    Mode mode;

    /**
     * Adds +0x80 to the mode
     */
    boolean normally_closed = false;

    /**
     * Constructor of the class
     * @param data the data from register value
     */
    public DigitalInput(int data){
        mode = Mode.fromInt(data & 0xFF);
        normally_closed = (data & 0x80) != 0;
    }

    /**
     * Convert the object to an integer
     * @return The integer value
     */
    public int toInt(){
        int data = mode.getValue();
        if(normally_closed){
            data |= 0x80;
        }
        return data;
    }

    @Override
    public String toString() {
        return "DigitalInput{" +
                "mode=" + mode +
                ", normally_closed=" + normally_closed +
                '}';
    }
}
