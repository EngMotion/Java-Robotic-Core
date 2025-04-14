package com.lucaf.robotic_core.STEPPERONLINE.iDM_RS;

import lombok.Getter;
import lombok.Setter;

/**
 * Class that represents the digital output settings of the motor.
 */
@Getter
@Setter
public class DigitalOutput {

    @Getter
    public enum Mode {
        INVALID(0x00),
        COMMMAND_COMPLETED(0x20),
        PATH_COMPLETED(0x21),
        HOMING_COMPLETED(0x22),
        IN_POSITION(0x23),
        BRAKE(0x24),
        ALARM(0x25);
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
     * Constructor of the DigitalOutput class.
     * @param data register data
     */
    public DigitalOutput(int data){
        mode = Mode.fromInt(data & 0xFF);
        normally_closed = (data & 0x80) != 0;
    }

    /**
     * Converts the digital output to an integer.
     * @return the integer representation of the digital output
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
        return "DigitalOutput{" +
                "mode=" + mode +
                ", normally_closed=" + normally_closed +
                '}';
    }
}
