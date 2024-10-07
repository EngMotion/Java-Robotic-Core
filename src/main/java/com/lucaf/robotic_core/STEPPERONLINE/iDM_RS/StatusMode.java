package com.lucaf.robotic_core.STEPPERONLINE.iDM_RS;

import lombok.Getter;

import java.util.Map;
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

    @Getter
    static final int HOMING = 0x20;

    @Getter
    static final int SET_CURRENT_POSITION_AS_ZERO = 0x21;

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


    public StatusMode(int mode){
        PATH_MODE = (byte) (mode & 0x000F);
        STATUS_CODE = (mode & 0xFFF0) >> 4;
    }

    public boolean isComplete(){
        return STATUS_CODE == 0x000;
    }

    public boolean isNotResponding(){
        return STATUS_CODE == 0x001 || STATUS_CODE == 0x002 || STATUS_CODE == 0x003;
    }

    public boolean isRunning(){
        return STATUS_CODE == 0x010;
    }

    public boolean isCompleated(){
        return STATUS_CODE == 0x020;
    }

}
