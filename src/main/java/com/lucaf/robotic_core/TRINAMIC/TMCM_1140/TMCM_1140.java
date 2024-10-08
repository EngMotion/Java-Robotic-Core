package com.lucaf.robotic_core.TRINAMIC.TMCM_1140;

import com.lucaf.robotic_core.TRINAMIC.utils.TMCLCommand;
import com.lucaf.robotic_core.TRINAMIC.USB;
import com.lucaf.robotic_core.exception.ConfigurationException;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.lucaf.robotic_core.TRINAMIC.TMCM_1140.Constants.*;

/**
 * Class that manages the communication with the TMCM_1140 module
 */
public class TMCM_1140 {

    byte address = 0x01;
    byte motor = 0x00;

    /**
     * The executor service that manages the feedback position
     */
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
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
     *
     * @param usb the USB object that manages the communication
     */
    public TMCM_1140(USB usb) {
        this.usb = usb;
    }

    /**
     * Constructor for the TMCM_1140 class
     *
     * @param usb     the USB object that manages the communication
     * @param address the address of the module
     * @param motor   the motor number
     */
    public TMCM_1140(USB usb, byte address, byte motor) {
        this.usb = usb;
        this.address = address;
        this.motor = motor;
    }

    /**
     * Method that initializes the module with given parameters
     *
     * @param params the parameters as a map of strings and integers representing the parameter name and value
     * @throws ConfigurationException if there is an error setting the parameters
     */
    public void setParameters(Map<String, Integer> params) throws ConfigurationException {
        try {
            Class<?> constantsClass = Class.forName("com.lucaf.robotic_core.TRINAMIC.TMCM_1140.Constants");
            for (Map.Entry<String, Integer> entry : params.entrySet()) {
                Field field = constantsClass.getField(entry.getKey());
                setParameter((byte) field.get(null), entry.getValue());
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException | IllegalAccessException | DeviceCommunicationException e) {
            throw new ConfigurationException("Parameter not found:" + e.getMessage());
        }

    }

    /**
     * Method that sets a parameter
     *
     * @param parameter the parameter to set
     * @param value     the value of the parameter
     * @throws DeviceCommunicationException if there is an error setting the parameter
     */
    private void setParameter(byte parameter, int value) throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(SAP);
        command.setValue(value);
        command.setType(parameter);
        TMCLCommand response = usb.write(command);
        if (response == null) {
            throw new DeviceCommunicationException("Error setting parameter");
        }
    }

    /**
     * Method that rotates the motor to the right
     *
     * @param velocity the velocity of the rotation
     * @throws DeviceCommunicationException if there is an error rotating the motor
     */
    private void rotateRight(int velocity) throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(ROR);
        command.setValue(velocity);
        usb.write(command);
    }

    /**
     * Method that rotates the motor to the right
     *
     * @param velocity the velocity of the rotation
     * @throws DeviceCommunicationException if there is an error rotating the motor
     */
    private void rotateLeft(int velocity) throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(ROL);
        command.setValue(velocity);
        usb.write(command);
    }

    /**
     * Method that stops the motor
     *
     * @throws DeviceCommunicationException if there is an error stopping the motor
     */
    private void stopMotor() throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(MST);
        usb.write(command);
    }

    /**
     * Method that starts the reference search
     *
     * @param mode the mode of the reference search
     * @throws DeviceCommunicationException if there is an error starting the reference search
     */
    private void startReferenceSearch(byte mode) throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(RFS);
        command.setType(mode);
        usb.write(command);
        position = 0;
    }

    /**
     * Method that moves the motor to the given position
     *
     * @param mode     the mode of the movement
     * @param position the position to move to
     * @throws DeviceCommunicationException if there is an error moving the motor
     */
    private void moveToPosition(byte mode, int position) throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(MVP);
        command.setType(mode);
        command.setValue(position);
        usb.write(command);
    }

    /**
     * Method that gets the value of a parameter
     *
     * @param parameter the parameter to get
     * @return the value of the parameter
     * @throws DeviceCommunicationException if there is an error getting the parameter
     */
    private int getParameter(byte parameter) throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(GAP);
        command.setType(parameter);
        TMCLCommand response = usb.write(command);
        if (response == null) {
            throw new DeviceCommunicationException("Error getting parameter");
        }
        return response.getValue();
    }

    /**
     * Method that moves the motor to the given position but waits for the movement to end
     *
     * @param mode     the mode of the movement
     * @param position the position to move to
     */
    private void MVP_till_end(byte mode, int position) throws DeviceCommunicationException {
        moveToPosition(mode, position);
        waitTillPositionReached();
        checkAndFixPosition();
    }

    /**
     * Method that waits for the position to be reached. It checkes the position flag wich is 1 if the position is reached
     */
    @SneakyThrows
    private void waitTillPositionReached() throws DeviceCommunicationException {
        while (true) {
            int status = getParameter(PARAM_POSITION_FLAG);
            //1 = reached , 0 = not reached
            if (status == 1) {
                break;
            }
            Thread.sleep(50);
        }
    }

    /**
     * Method that waits for the tasks to end. Used to claim
     */
    @SneakyThrows
    public void waitStatusUnlock() {
        while (true) {
            if (!isMoving.get()) {
                break;
            }
            Thread.sleep(50);
        }
    }

    /**
     * Method that checks if the position is correct and fixes it if it is not. The position is retrieved from the external encoder.
     * If the difference between the actual position and the desired position is greater than 100, the motor is moved to the desired position
     *
     * @throws DeviceCommunicationException if there is an error getting the parameter
     */
    private void checkAndFixPosition() throws DeviceCommunicationException {
        if (ENABLE_FEEDBACK_POSITION) {
            waitStatusUnlock();
            isMoving.set(true);
            while (true) {
                int actualPosition = getParameter(PARAM_EXTERNAL_ENCODER_POSITION);
                if (Math.abs(position - actualPosition) > 100) {
                    MVP_till_end(MVP_ABS, position);
                } else {
                    break;
                }
            }
            isMoving.set(false);
        }
    }

    /**
     * Method that moves the motor to the given position and waits for the movement to end
     *
     * @param position the position to move to
     * @throws DeviceCommunicationException if there is an error moving the motor
     */
    public void moveToRelativePositionAndWait(int position) throws DeviceCommunicationException {
        waitStatusUnlock();
        isMoving.set(true);
        MVP_till_end(MVP_REL, position);
        isMoving.set(false);
    }

    /**
     * Method that moves the motor to the given position and waits for the movement to end
     *
     * @param position the position to move to
     * @throws DeviceCommunicationException if there is an error moving the motor
     */
    public void moveToAbsolutePositionAndWait(int position) throws DeviceCommunicationException {
        waitStatusUnlock();
        isMoving.set(true);
        MVP_till_end(MVP_ABS, position);
        isMoving.set(false);
    }

    /**
     * Method to enable or disable the feedback position
     *
     * @param enable true to enable, false to disable
     */
    public void setFeedbackPosition(boolean enable) {
        if (enable && !ENABLE_FEEDBACK_POSITION) {
            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        checkAndFixPosition();
                    } catch (DeviceCommunicationException e) {
                        e.printStackTrace();
                    }
                }
            }, 1, 1, TimeUnit.MILLISECONDS);
        }
        if (!enable && ENABLE_FEEDBACK_POSITION) {
            executorService.shutdown();
        }
        ENABLE_FEEDBACK_POSITION = enable;
    }

}
