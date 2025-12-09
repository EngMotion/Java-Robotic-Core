package com.lucaf.robotic_core.dhRobotics.rgi100;

/**
 * Class that contains the constants of the RG100-22.
 */
public class Constants {

    /**
     * Initialization address: 0x0100
     * Write:
     * 0x01: Clamping unidirectional return to zero (related to return to zero direction setting), rotating return to zero
     * 0x02: Clamping open return to zero (return to zero direction returns to open), rotating return to zero
     * 0x03: Clamping closed return to zero (return to zero direction returns to closed), rotating return to zero
     * 0x04: Clamping recalibration
     * 0x05: Clamping open return to zero
     * 0x06: Clamping closed return to zero
     * 0x07: Rotating counterclockwise return to zero
     * 0x00xA5: Recalibration initialization (rotation first, then clamping)
     * Read:
     * 0: not in the initialization process;
     * 1: In the initialization process;
     * 2: Initialization in progress
     */
    public static final byte[] INITIALIZATION = {0x01, 0x00};
    /**
     * The address of the grip force: 0x0101
     * Value: 20-100
     * Type: Read/Write
     */
    public static final byte[] FORCE = {0x01, 0x01};
    /**
     * The address of the target position of gripper clamp: 0x0103
     * Value: 0-1000%
     * Type: Read/Write
     */
    public static final byte[] TARGET_POSITION = {0x01, 0x03};
    /**
     * The address of the speed of the gripper clamp: 0x0104
     * Value: 0-100%
     * Type: Read/Write
     */
    public static final byte[] SPEED = {0x01, 0x04};

    /**
     * The address of the target angle of the rotation: 0x0105
     * Value: -32768 - 32767
     * Type: Read/Write
     */
    public static final byte[] ABSOLUTE_ROTATION = {0x01, 0x05};

    /**
     * The address of the target angle overflows of the rotation: 0x0106
     * Value: -160 - 160
     * Type: Read/Write
     */
    public static final byte[] ABSOLUTE_ROTATION_HIGH = {0x01, 0x06};

    /**
     * The address of the speed of the rotation: 0x0107
     * Value: 0-100%
     * Type: Read/Write
     */
    public static final byte[] ROTATION_SPEED = {0x01, 0x07};

    /**
     * The address of the force of the rotation: 0x0108
     * Value: 20-100
     * Type: Read/Write
     */
    public static final byte[] ROTATION_FORCE = {0x01, 0x08};

    /**
     * The address of the relative angle of the rotation: 0x0109
     * Value: -32768 - 32767
     * Type: Read/Write
     */
    public static final byte[] RELATIVE_ROTATION = {0x01, 0x09};

    /**
     * The address of the initialization state of the gripper: 0x0200
     * Value: 0 not initialized, 1 initialized
     * Type: Read
     */
    public static final byte[] FEEDBACK_INITIALIZATION_GRIP_STATE = {0x02, 0x00};

    /**
     * The address of the state of the gripper: 0x0201
     * Value:
     * 00 : The gripper is in motion.
     * 01 : The gripper stops moving and the gripper do not detect a clamped object.
     * 02 : The gripper stops moving, and the gripper detects a clamped object.
     * 03 : After the gripper detect the clamped object, the object is found to fall.
     * Type: Read
     */
    public static final byte[] FEEDBACK_GRIP_STATE = {0x02, 0x01};

    /**
     * The address of the position of the gripper: 0x0202
     * Value: 0-1000
     * Type: Read
     */
    public static final byte[] FEEDBACK_GRIP_POSITION = {0x02, 0x02};

    /**
     * The address of the speed of the gripper: 0x0203
     * Value: 0-100
     * Type: Read
     */
    public static final byte[] FEEDBACK_GRIP_SPEED = {0x02, 0x03};

    /**
     * The address of the current of the gripper: 0x0204
     * Value: 0-100
     * Type: Read
     */
    public static final byte[] FEEDBACK_GRIP_CURRENT = {0x02, 0x04};

    /**
     * The address of the error code of the gripper: 0x0205
     * Value: 0: no problem; 04 overheating; 08 overload; 11 overspeed
     * Type: Read
     */
    public static final byte[] FEEDBACK_GRIP_ERROR_CODE = {0x02, 0x05};

    /**
     * The address of the rotation angle of the gripper: 0x0206
     * Value: -32768 - 32767
     * Type: Read
     */
    public static final byte[] FEEDBACK_CURRENT_ROTATION = {0x02, 0x08};

    /**
     * The address of the rotation angle of the gripper: 0x0207
     * Value: -160 - 160
     * Type: Read
     */
    public static final byte[] FEEDBACK_CURRENT_ROTATION_HIGH = {0x02, 0x09};

    /**
     * The address of the initialization state of the rotation: 0x020A
     * Value:
     * 0: not initialized;
     * 1: initialized successfully;
     * 2: being initialized
     * Type: Read
     */
    public static final byte[] FEEDBACK_INITIALIZATION_ROTATION_STATE = {0x02, 0x0A};

    /**
     * The address of the state of the rotation: 0x020B
     * Value:
     * 0: in motion,
     * 1: reached position;
     * 2: blocked rotation;
     * Type: Read
     */
    public static final byte[] FEEDBACK_ROTATION_STATE = {0x02, 0x0B};

    /**
     * The address of the speed of the rotation: 0x020C
     * Value: 0-100
     * Type: Read
     */
    public static final byte[] FEEDBACK_ROTATION_SPEED = {0x02, 0x0C};

    /**
     * The address of the current of the rotation: 0x020D
     * Value: 0-100
     * Type: Read
     */
    public static final byte[] FEEDBACK_ROTATION_CURRENT = {0x02, 0x0D};

    /**
     * The address of the force of the rotation: 0x020E
     * Value: 0-100
     * Type: Read
     */
    public static final byte[] FEEDBACK_GRIPPING_FORCE = {0x02, 0x14};

    /**
     * The address of the speed of the gripping: 0x020F
     * Value: 0-100
     * Type: Read
     */
    public static final byte[] FEEDBACK_GRIPPING_SPEED = {0x02, 0x15};

    /**
     * The address of the force of the rotation: 0x0210
     * Value: 0-100
     * Type: Read
     */
    public static final byte[] FEEDBACK_ROTATION_FORCE = {0x02, 0x16};

    /**
     * The address of the speed of the rotation: 0x0211
     * Value: 0-100
     * Type: Read
     */
    public static final byte[] FEEDBACK_ROTATION_SPEED_2 = {0x02, 0x17};

    /**
     * The address of functional code to save the configuration: 0x0300
     * Value: 0: Restore factory settings; 1: Save configuration
     * Type: Write
     */
    public static final byte[] SAVE_CONFIG = {0x03, 0x00};

    /**
     * The address of the initialization direction: 0x0301
     * Value: 0: Open; 1: Close
     * Type: Read/Write
     */
    public static final byte[] INITIALIZATION_DIRECTION = {0x03, 0x01};

    /**
     * The address of the slave address: 0x0302
     * Value: 1-247
     * Type: Read/Write
     */
    public static final byte[] SLAVE_ADDRESS = {0x03, 0x02};
    /**
     * The baudrate of the device: 0x0303
     * Value: 0-5:115200,57600,38400,19200,9600,4800
     * Type: Read/Write
     */
    public static final byte[] BAUDRATE = {0x03, 0x03};

    /**
     * The address of the stop bits: 0x0304
     * Value: 0-1: 1, 2
     * Type: Read/Write
     */
    public static final byte[] STOP_BITS = {0x03, 0x04};

    /**
     * The address of the parity: 0x0305
     * Value:
     * 0: no parity;
     * 1: odd parity;
     * 2: even parity;
     * Type: Read/Write
     */
    public static final byte[] PARITY = {0x03, 0x05};



    //---------------0x0400 I/O Parameters---------------
    //NOT USED IN THIS PROJECT

    //---------------0x0500 Functions---------------
    //WRITE ONLY

    /**
     * The address of the function to stop the gripper: 0x0502
     * Value: 1: stop
     * Type: Write
     */
    public static final byte[] STOP = {0x05, 0x02};

    /**
     * The address of the function to automatically initialize the gripper on power up: 0x0504
     * Value:
     * 1: auto initialize
     * 0: no auto initialize
     * Type: Write
     */
    public static final byte[] AUTO_INITIALIZE = {0x05, 0x04};


}
