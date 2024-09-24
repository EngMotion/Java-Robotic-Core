package com.lucaf.robotic_core.DH_ROBOTICS.RGI100_22;

/**
 * Class that contains the constants of the RG100-22.
 */
public class Constants {

    //---------------0x0000 Special informations---------------
    public static final byte[] CCLINK_MODEL = {0x00, 0x00};
    public static final byte[] CCLINK_VERSION = {0x00, 0x01};
    public static final byte[] CCLINK_DEVICE = {0x00, 0x02};

    //---------------0x0100 Control addresses---------------
    public static final byte[] INITIALIZATION = {0x01, 0x00};
    public static final byte[] FORCE = {0x01, 0x01};
    //0x0102 Is a mystery
    public static final byte[] TARGET_POSITION = {0x01, 0x03};
    public static final byte[] SPEED = {0x02, 0x04};
    public static final byte[] ABSOLUTE_ROTATION = {0x01, 0x05};
    //Use high in combination with low if you need a big rotation
    public static final byte[] ABSOLUTE_ROTATION_HIGH = {0x01, 0x06};
    public static final byte[] ROTATION_SPEED = {0x01, 0x07};
    public static final byte[] ROTATION_FORCE = {0x01, 0x08};
    public static final byte[] RELATIVE_ROTATION = {0x01, 0x09};

    //---------------0x0200 Feedback addresses---------------
    //THOSE ARE READ ONLY
    public static final byte[] FEEDBACK_INITIALIZATION_GRIP_STATE = {0x02, 0x00};
    public static final byte[] FEEDBACK_GRIP_STATE = {0x02, 0x01};
    public static final byte[] FEEDBACK_GRIP_POSITION = {0x02, 0x02};
    public static final byte[] FEEDBACK_GRIP_SPEED = {0x02, 0x03};
    public static final byte[] FEEDBACK_GRIP_CURRENT = {0x02, 0x04};
    public static final byte[] FEEDBACK_GRIP_ERROR_CODE = {0x02, 0x05};
    public static final byte[] FEEDBACK_CURRENT_ROTATION = {0x02, 0x08};
    public static final byte[] FEEDBACK_CURRENT_ROTATION_HIGH = {0x02, 0x09};
    public static final byte[] FEEDBACK_INITIALIZATION_ROTATION_STATE = {0x02, 0x0A};
    public static final byte[] FEEDBACK_ROTATION_STATE = {0x02, 0x0B};
    public static final byte[] FEEDBACK_ROTATION_SPEED = {0x02, 0x0C};
    public static final byte[] FEEDBACK_ROTATION_CURRENT = {0x02, 0x0D};
    public static final byte[] FEEDBACK_GRIPPING_FORCE = {0x02, 0x14};
    public static final byte[] FEEDBACK_GRIPPING_SPEED = {0x02, 0x15};
    public static final byte[] FEEDBACK_ROTATION_FORCE = {0x02, 0x16};
    public static final byte[] FEEDBACK_ROTATION_SPEED_2 = {0x02, 0x17};

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
    public static final byte[] MAX_CURRENT_SCALE = {0x03, 0x06};

    //---------------0x0400 I/O Parameters---------------
    //NOT USED IN THIS PROJECT

    //---------------0x0500 Functions---------------
    //WRITE ONLY
    public static final byte[] STOP = {0x05, 0x02};
    public static final byte[] AUTO_INITIALIZE = {0x05, 0x04};


}
