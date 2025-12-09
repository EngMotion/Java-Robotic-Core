package com.lucaf.robotic_core.STEPPERONLINE.iDM_RS;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

/**
 * Class that represents the control mode of a motor.
 */
@Getter
@Setter
public class ControlMode {
    public enum ControlType {
        NO_MODE(0),
        VELOCITY_MODE(2),
        POSITION_MODE(1);

        private final int value;

        ControlType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * BIT 0-3: Control Mode
     */
    public int CONTROL_MODE = ControlType.NO_MODE.getValue();

    /**
     * BIT 4: Interrupt
     */
    public boolean INTERRUPT = false;

    /**
     * BIT 5: Overlap
     */
    public boolean OVERLAP = false;

    /**
     * BIT 6: Relative Positioning
     */
    public boolean RELATIVE_POSITIONING = false;

    /**
     * BIT 8-13: Path mode
     */
    public boolean[] PATH_MODE = new boolean[6];
    
    /**
     * BIT 14: Jump
     */
    public boolean JUMP = false;
    
    public ControlMode(int mode) {
        CONTROL_MODE = mode & 0x0F;
        INTERRUPT = (mode & 0x10) != 0;
        OVERLAP = (mode & 0x20) != 0;
        RELATIVE_POSITIONING = (mode & 0x40) != 0;
        for(int i = 0; i < 6; i++){
            PATH_MODE[i] = (mode & (0x01 << (i+8))) != 0;
        }
        JUMP = (mode & 0x4000) != 0;
    }



    public ControlMode(){}

    public int toInt(){
        int mode = CONTROL_MODE;
        if(INTERRUPT) mode |= 0x10;
        if(OVERLAP) mode |= 0x20;
        if(RELATIVE_POSITIONING) mode |= 0x40;
        for(int i = 0; i < 6; i++){
            if(PATH_MODE[i]) mode |= 0x01 << (i+8);
        }
        if(JUMP) mode |= 0x4000;
        return mode;
    }

    @Override
    public String toString(){
        return "ControlMode{" +
                "CONTROL_MODE=" + CONTROL_MODE +
                ", INTERRUPT=" + INTERRUPT +
                ", OVERLAP=" + OVERLAP +
                ", RELATIVE_POSITIONING=" + RELATIVE_POSITIONING +
                ", PATH_MODE=" + Arrays.toString(PATH_MODE) +
                ", JUMP=" + JUMP +
                '}';

    }

}
