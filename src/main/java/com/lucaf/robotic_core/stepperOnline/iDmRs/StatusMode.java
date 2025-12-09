package com.lucaf.robotic_core.stepperOnline.iDmRs;

import lombok.Getter;

/**
 * Class that represents the status of a motor.
 */
@Getter
public class StatusMode {
    /**
     * Output is in format 0x1P with P path 0-15
     * @param path the path
     * @return the segment positioning
     */
    public static int getSegmentPositioning (byte path){
        return (path & 0x0F) + 0x10;
    }

    /**
     * Homing mode constant
     */
    @Getter
    static final int HOMING = 0x20;

    /**
     * Set current position as zero constant
     */
    @Getter
    static final int SET_CURRENT_POSITION_AS_ZERO = 0x21;

    /**
     * Emergency stop constant
     */
    @Getter
    static final int EMERGENCY_STOP = 0x40;

    /**
     * Bit 0-7: Path Mode
     */
    byte PATH_MODE = 0x00;
    /**
     * Bit 8-23: Status Code
     */
    int STATUS_CODE = 0x00;


    /**
     * Constructor of the class
     * @param mode the mode
     */
    public StatusMode(int mode){
        PATH_MODE = (byte) (mode & 0x000F);
        STATUS_CODE = (mode & 0xFFF0) >> 4;
    }

    /**
     * Returns if the motor is ready
     * @return if the motor is ready
     */
    public boolean isComplete(){
        return STATUS_CODE == 0x000;
    }

    /**
     * Returns if the motor is not responding
     * @return if the motor is not responding
     */
    public boolean isNotResponding(){
        return STATUS_CODE == 0x001 || STATUS_CODE == 0x002 || STATUS_CODE == 0x003;
    }

    /**
     * Returns if the motor is running
     * @return if the motor is running
     */
    public boolean isRunning(){
        return STATUS_CODE == 0x010;
    }

    /**
     * Return the status of the motor in a human readable format
     * @return the status of the motor in a human readable format
     */
    @Override
    public String toString() {
        return "StatusMode{" +
                "PATH_MODE=" + PATH_MODE +
                ", STATUS_CODE=" + STATUS_CODE +
                '}';
    }

}
