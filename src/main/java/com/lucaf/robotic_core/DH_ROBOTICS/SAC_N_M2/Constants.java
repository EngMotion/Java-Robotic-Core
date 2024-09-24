package com.lucaf.robotic_core.DH_ROBOTICS.SAC_N_M2;

/**
 * Class that contains the constants of the SAC-N device
 */
public class Constants {

    //---------------0x0000 Special informations---------------
    public static final byte[] CCLINK_MODEL = {0x00, 0x10};
    public static final byte[] CCLINK_VERSION = {0x00, 0x11};
    public static final byte[] CCLINK_DEVICE = {0x00, 0x12};

    //---------------0x0100 Control addresses---------------
    public static final byte[] INITIALIZATION = {0x01, 0x00};
    public static final byte[] PUSH_FORCE = {0x01, 0x01};
    public static final byte[] PUSH_BANS = {0x01, 0x02};
    public static final byte[] TARGET_POSITION = {0x01, 0x03};
    public static final byte[] SPEED = {0x01, 0x04};
    //Use high in combination with low if you need a big rotation
    public static final byte[] ACCELERATION = {0x01, 0x05};
    public static final byte[] RELATIVE_POSITION = {0x01, 0x06};

    //---------------0x0200 Feedback addresses---------------
    //THOSE ARE READ ONLY
    public static final byte[] FEEDBACK_INITIALIZATION_GRIP_STATE = {0x02, 0x00};
    public static final byte[] FEEDBACK_MOTION_STATE = {0x02, 0x01};
    public static final byte[] FEEDBACK_POSITION = {0x02, 0x02};
    public static final byte[] FEEDBACK_SPEED = {0x02, 0x03};
    public static final byte[] FEEDBACK_CURRENT = {0x02, 0x04};
    public static final byte[] FEEDBACK_ERROR_CODE = {0x02, 0x05};
    public static final byte[] FEEDBACK_FORCE_SENSOR = {0x02, 0x06};
    public static final byte[] FEEDBACK_ENCODER_STATE = {0x02, 0x07};

    //---------------0x0300 Configuration addresses---------------
    public static final byte[] SAVE_CONFIG = {0x03, 0x00};
    public static final byte[] INITIALIZATION_DIRECTION = {0x03, 0x01};
    public static final byte[] SLAVE_ADDRESS = {0x03, 0x02};
    /**
     * The baudrate of the device.
     * 0-5:115200,57600,38400,19200,9600,4800
     */
    public static final byte[] BAUDRATE = {0x03, 0x03};
    public static final byte[] STOP_BITS = {0x03, 0x04};
    public static final byte[] PARITY = {0x03, 0x05};
    public static final byte[] HOME_OFFSET = {0x03, 0x08};
    public static final byte[] PUSH_SPEED = {0x03, 0x09};
    public static final byte[] PUSH_DIRECTION = {0x03, 0x0A};

    //---------------0x0400 I/O Parameters---------------
    //NOT USED IN THIS PROJECT

    //---------------0x0500 Functions---------------
    //WRITE ONLY
    public static final byte[] START = {0x05, 0x01};
    public static final byte[] STOP = {0x05, 0x02};
    public static final byte[] CLEAR_FAULT = {0x05, 0x03};
    public static final byte[] AUTO_INITIALIZE = {0x05, 0x04};

    //---------------0x1600 Device specific params---------------
    public static final byte[] STATUS = {0x16, 0x11};


}
