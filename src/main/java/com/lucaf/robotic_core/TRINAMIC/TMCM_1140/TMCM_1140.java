package com.lucaf.robotic_core.TRINAMIC.TMCM_1140;

import com.lucaf.robotic_core.TRINAMIC.TMCLCommand;
import com.lucaf.robotic_core.TRINAMIC.USB;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class that manages the communication with the TMCM_1140 module
 */
public class TMCM_1140 {

    public static byte MVP_ABS = 1;
    public static byte MVP_REL = 2;
    public static byte MVP_COORD = 3;
    static Map<String, Byte> parameters = Map.<String, Byte>ofEntries(
            Map.entry("PARAM_TARGET_POSITION", (byte) 0),
            Map.entry("PARAM_ACTUAL_POSITION", (byte) 1),
            Map.entry("PARAM_TARGET_SPEED", (byte) 2),
            Map.entry("PARAM_ACTUAL_SPEED", (byte) 3),
            Map.entry("PARAM_MAX_SPEED", (byte) 4),
            Map.entry("PARAM_MAX_ACCELERATION", (byte) 5),
            Map.entry("PARAM_MAX_CURRENT", (byte) 6),
            Map.entry("PARAM_STANDBY_CURRENT", (byte) 7),
            Map.entry("PARAM_POSITION_FLAG", (byte) 8),
            Map.entry("PARAM_HOME_FLAG", (byte) 9),
            Map.entry("PARAM_RIGHT_LIMIT_STATE", (byte) 10),
            Map.entry("PARAM_LEFT_LIMIT_STATE", (byte) 11),
            Map.entry("PARAM_RIGHT_LIMIT_FLAG", (byte) 12),
            Map.entry("PARAM_LEFT_LIMIT_FLAG", (byte) 13),
            Map.entry("PARAM_ACCELERATION_A1", (byte) 15),
            Map.entry("PARAM_VELOCITY_V1", (byte) 16),
            Map.entry("PARAM_MAX_DECELERATION", (byte) 17),
            Map.entry("PARAM_DECELERATION_D1", (byte) 18),
            Map.entry("PARAM_MINIMUM_SPEED", (byte) 130),
            Map.entry("PARAM_ACCELERATION", (byte) 135),
            Map.entry("PARAM_RAMP_MODE", (byte) 138),
            Map.entry("PARAM_MICROSTEP_RESOLUTION", (byte) 140),
            Map.entry("PARAM_SOFT_STOP_FLAG", (byte) 149),
            Map.entry("PARAM_END_SWITCH_FLAG", (byte) 150),
            Map.entry("PARAM_RAMP_DIVISOR", (byte) 153),
            Map.entry("PARAM_PULSE_DIVISOR", (byte) 154),
            Map.entry("PARAM_STEP_INTERPOLATION", (byte) 160),
            Map.entry("PARAM_DOUBLE_STEP_MODE", (byte) 161),
            Map.entry("PARAM_CHOPPER_BLANK_TIME", (byte) 162),
            Map.entry("PARAM_CONSTANT_T_OFF_MODE", (byte) 163),
            Map.entry("PARAM_FAST_DECAY_TIME_SETTING", (byte) 164),
            Map.entry("PARAM_FAST_DECAY_TIME", (byte) 165),
            Map.entry("PARAM_SINE_WAVE_OFFSET", (byte) 166),
            Map.entry("PARAM_CHOPPER_TIME", (byte) 167),
            Map.entry("PARAM_SMART_ENERGY_CURRENT", (byte) 168),
            Map.entry("PARAM_SMART_ENERGY_STALL_LEVEL", (byte) 169),
            Map.entry("PARAM_SMART_ENERGY_HYSTERESIS", (byte) 170),
            Map.entry("PARAM_SMART_ENERGY_STEP_UP", (byte) 171),
            Map.entry("PARAM_SMART_ENERGY_HYSTERESIS_START", (byte) 172),
            Map.entry("PARAM_STALLGUARD_FILTER_ENABLE", (byte) 173),
            Map.entry("PARAM_STALLGUARD_THRESHOLD", (byte) 174),
            Map.entry("PARAM_SLOPE_CONTROL_HIGH_SIDE", (byte) 175),
            Map.entry("PARAM_SLOPE_CONTROL_LOW_SIDE", (byte) 176),
            Map.entry("PARAM_SHORT_TO_GROUND_PROTECTION", (byte) 177),
            Map.entry("PARAM_SHORT_TO_VCC_TIME", (byte) 178),
            Map.entry("PARAM_VSENSE", (byte) 179),
            Map.entry("PARAM_SMART_ENERGY_ACTUAL_CURRENT", (byte) 180),
            Map.entry("PARAM_STOP_ON_STALL", (byte) 181),
            Map.entry("PARAM_SMART_ENERGY_THRESHOLD_SPEED", (byte) 182),
            Map.entry("PARAM_SMART_ENERGY_SLOW_RUN_CURRENT", (byte) 183),
            Map.entry("PARAM_RANDOM_TOFF_MODE", (byte) 184),
            Map.entry("PARAM_REFERENCE_SEARCH_MODE", (byte) 193),
            Map.entry("PARAM_REFERENCE_SEARCH_SPEED", (byte) 194),
            Map.entry("PARAM_REFERENCE_SWITCH_SPEED", (byte) 195),
            Map.entry("PARAM_END_SWITCH_DISTANCE", (byte) 196),
            Map.entry("PARAM_LAST_REFERENCE_POSITION", (byte) 197),
            Map.entry("PARAM_BOOST_CURRENT", (byte) 200),
            Map.entry("PARAM_FREWHEELING", (byte) 204),
            Map.entry("PARAM_LOAD_VALUE", (byte) 206),
            Map.entry("PARAM_EXTENDED_ERROR_FLAG", (byte) 207),
            Map.entry("PARAM_TMC262_ERROR_FLAGS", (byte) 208),
            Map.entry("PARAM_ENCODER_POSITION", (byte) 209),
            Map.entry("PARAM_GROUP_INDEX", (byte) 213),
            Map.entry("PARAM_POWER_DOWN_DELAY", (byte) 214),
            Map.entry("PARAM_EXTERNAL_ENCODER_POSITION", (byte) 216),
            Map.entry("PARAM_EXTERNAL_ENCODER_RESOLUTION", (byte) 217),
            Map.entry("PARAM_EXTERNAL_ENCODER_DEVIATION", (byte) 218),
            Map.entry("PARAM_REVERSE_SHAFT", (byte) 251),
            Map.entry("PARAM_STEP_DIRECTION_MODE", (byte) 254),
            Map.entry("PARAM_UNIT_MODE", (byte) 255)
    );

    static byte ROR= 1;
    static byte ROL= 2;
    static byte MST= 3;
    static byte MVP= 4;
    static byte SAP= 5;
    static byte GAP= 6;
    static byte RFS= 13;
    static byte SIO= 14;

    byte address=0x01;
    byte motor=0x00;

    /**
     * If enabled, the code will make sure to have the position set even if external events occur
     */
    private boolean ENABLE_FEEDBACK_POSITION = false;

    /**
     * The position of the motor
     */
    private int position = 0;

    /**
     * The USB object that manages the communication
     */
    private final USB usb;


    /**
     * The status of the motor. It is only used to check if there are any moving tasks in progress
     */
    private AtomicBoolean isMoving = new AtomicBoolean(false);

    /**
     * Constructor for the TMCM_1140 class
     * @param usb the USB object that manages the communication
     */
    public TMCM_1140(USB usb) {
        this.usb = usb;
    }

    /**
     * Constructor for the TMCM_1140 class
     * @param usb the USB object that manages the communication
     * @param address the address of the module
     * @param motor the motor number
     */
    public TMCM_1140(USB usb, byte address, byte motor) {
        this.usb = usb;
        this.address = address;
        this.motor = motor;
    }

    /**
     * Method that initializes the module with given parameters
     * @param params the parameters as a map of strings and integers representing the parameter name and value
     */
    public void setParameters(Map<String, Integer> params) {
        for (Map.Entry<String, Integer> entry : params.entrySet()) {
            setParameter(parameters.get(entry.getKey()), entry.getValue());
        }
    }


    private boolean setParameter(byte parameter, int value) {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(SAP);
        command.setValue(value);
        command.setType(parameter);
        TMCLCommand response = usb.write(command);
        return response != null;
    }

    private void rotateRight(int velocity) {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(ROR);
        command.setValue(velocity);
        usb.write(command);
    }

    private void rotateLeft(int velocity) {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(ROL);
        command.setValue(velocity);
        usb.write(command);
    }

    private void stopMotor() {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(MST);
        usb.write(command);
    }

    private void startReferenceSearch(byte mode) {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(RFS);
        command.setType(mode);
        usb.write(command);
        position = 0;
    }

    private void moveToPosition(byte mode, int position) {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(MVP);
        command.setType(mode);
        command.setValue(position);
        usb.write(command);
    }

    private int getParameter(byte parameter) {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(GAP);
        command.setType(parameter);
        TMCLCommand response = usb.write(command);
        //If null send MIN
        return response != null ? response.getValue() : Integer.MIN_VALUE;
    }

    /**
     * Method that moves the motor to the given position but waits for the movement to end
     * @param mode the mode of the movement
     * @param position the position to move to
     */
    private void MVP_till_end(byte mode, int position){
        moveToPosition(mode,position);
        waitTillPositionReached();
        checkAndFixPosition();
    }

    /**
     * Method that waits for the position to be reached. It checkes the position flag wich is 1 if the position is reached
     */
    private void waitTillPositionReached(){
        try {
            while (true) {
                int status = getParameter(parameters.get("PARAM_POSITION_FLAG"));
                //1 = reached , 0 = not reached
                if (status == 1) {
                    break;
                }
                Thread.sleep(50);
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that waits for the tasks to end. Used to claim
     */
    public void waitStatusUnlock(){
        try {
            while (true) {
                if (!isMoving.get()) {
                    break;
                }
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that checks if the position is correct and fixes it if it is not. The position is retrieved from the external encoder.
     */
    private void checkAndFixPosition(){
        if(ENABLE_FEEDBACK_POSITION){
            waitStatusUnlock();
            isMoving.set(true);
            while (true){
                int actualPosition = getParameter(parameters.get("PARAM_EXTERNAL_ENCODER_POSITION"));
                if (Math.abs(position-actualPosition)>100){
                    MVP_till_end(MVP_ABS,position);
                }else{
                    break;
                }
            }
            isMoving.set(false);
        }
    }

    /**
     * Method that moves the motor to the given position and waits for the movement to end
     * @param position the position to move to
     */
    public void moveToRelativePositionAndWait(int position){
        waitStatusUnlock();
        isMoving.set(true);
        MVP_till_end(MVP_REL,position);
        isMoving.set(false);
    }

    /**
     * Method that moves the motor to the given position and waits for the movement to end
     * @param position the position to move to
     */
    public void moveToAbsolutePositionAndWait(int position){
        waitStatusUnlock();
        isMoving.set(true);
        MVP_till_end(MVP_ABS,position);
        isMoving.set(false);
    }

    /**
     * Method to enable or disable the feedback position
     * @param enable true to enable, false to disable
     */
    public void setFeedbackPosition(boolean enable){
        ENABLE_FEEDBACK_POSITION = enable;
        //TODO: Start CRON job to check position
    }

}
