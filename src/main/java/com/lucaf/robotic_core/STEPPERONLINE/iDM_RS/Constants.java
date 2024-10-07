package com.lucaf.robotic_core.STEPPERONLINE.iDM_RS;

public class Constants {
    public static final byte[] MOTOR_DIRECTION = new byte[]{0x00, 0x07};
    public static final byte[] DI_1 = new byte[]{0x01, 0x45};
    public static final byte[] DI_2 = new byte[]{0x01, 0x47};
    public static final byte[] DI_3 = new byte[]{0x01, 0x49};
    public static final byte[] DI_4 = new byte[]{0x01, 0x4B};
    public static final byte[] DI_5 = new byte[]{0x01, 0x4D};
    public static final byte[] DI_6 = new byte[]{0x01, 0x4F};
    public static final byte[] DI_7 = new byte[]{0x01, 0x51};
    public static final byte[] DO_1 = new byte[]{0x01, 0x57};
    public static final byte[] DO_2 = new byte[]{0x01, 0x59};
    public static final byte[] PEAK_CURRENT = new byte[]{0x01, (byte) 0x91};
    public static final byte[] AUTO_TUNE = new byte[]{0x01, (byte) 0xAB};
    public static final byte[] RS485_ID = new byte[]{0x01, (byte) 0xBF};
    public static final byte[] MOTOR_STATUS = new byte[]{0x01, 0x03};
    public static final byte[] CONTROL_WORD = new byte[]{0x18, 0x01};
    public static final byte[] SAVE = new byte[]{0x19, 0x01};
    public static final byte[] ALARM = new byte[]{0x22, 0x03};
    public static final byte[] MOTOR_STATE = new byte[]{0x10, 0x03};

    public static final byte[] CONTROL_MODE = new byte[]{0x62, 0x00};
    public static final byte[] STATUS_MODE = new byte[]{0x60, 0x02};
    public static final byte[] TARGET_POSITION_HIGH = new byte[]{0x62, 0x01};
    public static final byte[] TARGET_POSITION_LOW = new byte[]{0x62, 0x02};
    public static final byte[] VELOCITY = new byte[]{0x62, 0x03};
    public static final byte[] ACCELERATION = new byte[]{0x62, 0x04};
    public static final byte[] DECELERATION = new byte[]{0x62, 0x05};
    public static final byte[] PAUSE_TIME = new byte[]{0x62, 0x06};
    public static final byte[] CURRENT = new byte[]{0x62, 0x07};

}
