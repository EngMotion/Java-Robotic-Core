package com.lucaf.robotic_core.TRINAMIC.TMCM_3351;

import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.State;
import com.lucaf.robotic_core.TRINAMIC.USB;
import com.lucaf.robotic_core.TRINAMIC.utils.TMCLCommand;
import com.lucaf.robotic_core.exception.ConfigurationException;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lucaf.robotic_core.TRINAMIC.TMCM_3351.Constants.*;

/**
 * Class that represents the TMCM_3351 motor
 */
@Slf4j
public class TMCM_3351_MOTOR {

    /**
     * The USB communication class
     */
    private final USB usb;

    /**
     * The motor number 0-2
     */
    private final byte motor;

    /**
     * The address of the device. Should be 0x01 as the global module
     */
    private final byte address;

    /**
     * The state of the motor
     */
    AtomicBoolean isMoving = new AtomicBoolean(false);

    /**
     * The state of the device
     */
    private final HashMap<String, Object> state;

    /**
     * The state class
     */
    private final State stateFunction;

    /**
     * The class of the constants. Used to get the parameters
     */
    private final Class<?> constantsClass;

    /**
     * The executor service for async operations
     */
    final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Last verified position
     */
    AtomicInteger currentPos = new AtomicInteger(0);

    /**
     * Target position sent to the device
     */
    AtomicInteger targetPos = new AtomicInteger(0);

    /**
     * The device is initialized
     */
    AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * The speed of the motor
     */
    AtomicInteger speed = new AtomicInteger(0);

    /**
     * The acceleration of the motor
     */
    AtomicInteger acceleration = new AtomicInteger(0);

    /**
     * The deceleration of the motor
     */
    AtomicInteger deceleration = new AtomicInteger(0);

    /**
     * Flag to indicate if the motor has a fault
     */
    @Getter
    AtomicBoolean hasFault = new AtomicBoolean(false);

    /**
     * Global Logger
     */
    final Logger logger;

    /**
     * Constructor of the class
     *
     * @param tmcm_3351     the TMCM_3351 class
     * @param motorNumber   the motor number
     * @param state         the state of the motor
     * @param stateFunction the state interface with the onStateChange method
     */
    public TMCM_3351_MOTOR(TMCM_3351 tmcm_3351, byte motorNumber, HashMap<String, Object> state, State stateFunction, Logger logger) {
        this.usb = tmcm_3351.getUsb();
        this.address = tmcm_3351.getAddress();
        this.motor = motorNumber;
        this.state = state;
        this.stateFunction = stateFunction;
        constantsClass = tmcm_3351.getConstantsClass();
        this.logger = logger;
        initState();
    }

    /**
     * Method that initializes the motor
     */
    public void start() {
        //
    }

    /**
     * Method that initializes the state of the specific motor
     */
    void initState() {
        if (state.containsKey("current_position")) {
            if (state.get("current_position") instanceof AtomicInteger) {
                currentPos = (AtomicInteger) state.get("current_position");
            } else if (state.get("current_position") instanceof Integer) {
                currentPos.set((Integer) state.get("current_position"));
            } else if (state.get("current_position") instanceof Double) {
                currentPos.set(((Double) state.get("current_position")).intValue());
            }
        }
        state.put("current_position", currentPos);

        if (state.containsKey("target_position")) {
            if (state.get("target_position") instanceof AtomicInteger) {
                targetPos = (AtomicInteger) state.get("target_position");
            } else if (state.get("target_position") instanceof Integer) {
                targetPos.set((Integer) state.get("target_position"));
            } else if (state.get("target_position") instanceof Double) {
                targetPos.set(((Double) state.get("target_position")).intValue());
            }
        }
        state.put("target_position", targetPos);
        state.put("is_moving", isMoving);
        state.put("initialized", initialized);
        initialized.set(true);
        stateFunction.notifyStateChange();
    }

    /**
     * Method that initializes the module with given parameters
     *
     * @param params the parameters as a map of strings and integers representing the parameter name and value
     * @throws ConfigurationException if there is an error setting the parameters
     */
    public void setParameters(Map<String, Integer> params) throws ConfigurationException {
        for (Map.Entry<String, Integer> entry : params.entrySet()) {
            setParameter(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Method that sets a parameter
     *
     * @param value     the value of the parameter
     * @param parameter the parameter to set as a string
     * @throws ConfigurationException if there is an error setting the parameter
     */
    public void setParameter(String value, int parameter) throws ConfigurationException {
        try {
            logger.log("[TMCM_3351_MOTOR] Setting parameter " + value + " to " + parameter);
            Field field = constantsClass.getField(value);
            setParameter((byte) field.get(null), parameter);
        } catch (NoSuchFieldException | IllegalAccessException | DeviceCommunicationException e) {
            throw new ConfigurationException("Parameter not found: " + e.getMessage());
        }
    }

    /**
     * Method that verifies if a parameter is set to a specific value
     * @param parameter the parameter to verify
     * @param value the value to verify
     * @return true if the parameter is set to the value, false otherwise
     * @throws DeviceCommunicationException if there is an error communicating with the device
     */
    private boolean verifyParameter(byte parameter, int value) throws DeviceCommunicationException {
        int attempts = 5;
        while (attempts > 0) {
            attempts--;
            try {
                Thread.sleep(10);
                int value_check = getParameter(parameter);
                if (value_check == value) {
                    return true;
                }
            } catch (InterruptedException e) {
                throw new DeviceCommunicationException(e.getMessage());
            }
        }
        return false;
    }

    /**
     * Method that sets a parameter
     *
     * @param parameter the parameter to set
     * @param value     the value of the parameter
     * @throws DeviceCommunicationException if there is an error setting the parameter
     */
    private void setParameter(byte parameter, int value) throws DeviceCommunicationException {
        logger.debug("[TMCM_3351_MOTOR] Setting parameter " + parameter + " to " + value);
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(SAP);
        command.setValue(value);
        command.setType(parameter);
        TMCLCommand response = usb.write(command);
        if (response == null) {
            throw new DeviceCommunicationException("Error setting parameter");
        } else {
            if (parameter == PARAM_MAX_SPEED) {
                speed.set(value);
            } else if (parameter == PARAM_MAX_ACCELERATION) {
                acceleration.set(value);
            } else if (parameter == PARAM_MAX_DECELERATION) {
                deceleration.set(value);
            }
        }
    }

    /**
     * Method that rotates the motor to the right
     *
     * @param velocity the velocity of the rotation
     * @throws DeviceCommunicationException if there is an error rotating the motor
     */
    public void rotateRight(int velocity) throws DeviceCommunicationException {
        if (velocity < 0) {
            rotateLeft(-velocity);
            return;
        }
        isMoving.set(velocity != 0);
        stateFunction.notifyStateChange();
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
    public void rotateLeft(int velocity) throws DeviceCommunicationException {
        if (velocity < 0) {
            rotateRight(-velocity);
            return;
        }
        isMoving.set(velocity != 0);
        stateFunction.notifyStateChange();
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
    public void stopMotor() throws DeviceCommunicationException {
        isMoving.set(false);
        stateFunction.notifyStateChange();
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
    private int referenceSearch(int mode) throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(RFS);
        command.setType((byte) mode);
        TMCLCommand response = usb.write(command);
        return response.getValue();
    }

    /**
     * Method that starts the reference search
     *
     * @throws DeviceCommunicationException if there is an error starting the reference search
     */
    private void startReferenceSearch() throws DeviceCommunicationException {
        referenceSearch(0);
        targetPos.set(0);
        currentPos.set(0);
    }

    /**
     * Method that stops the reference search
     *
     * @throws DeviceCommunicationException if there is an error stopping the reference search
     */
    public void stopReferenceSearch() throws DeviceCommunicationException {
        referenceSearch(1);
    }

    /**
     * Method that checks if the reference search is complete
     *
     * @return true if the reference search is complete, false otherwise
     * @throws DeviceCommunicationException if there is an error checking if the reference search is complete
     */
    public boolean isReferenceSearchComplete() throws DeviceCommunicationException {
        return referenceSearch(2) == 0;
    }

    /**
     * Method that starts the reference search and waits for it to end
     *
     * @return a future that represents the result of the operation
     */
    public Future<Boolean> startReferenceSearchAndWait() {
        return executorService.submit(() -> {
            logger.log("[TMCM_3351_MOTOR] Starting reference search");
            try {
                isMoving.set(true);
                startReferenceSearch();
                while (!isReferenceSearchComplete()) {
                    if (!isMoving.get()) throw new DeviceCommunicationException("Device is stopped.");
                    Thread.sleep(200);
                }
                isMoving.set(false);
                return true;
            } catch (Exception e) {
                logger.error("[TMCM_3351_MOTOR] Error starting reference search");
                logger.error(e.getMessage());
                return false;
            }
        });
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
    public int getParameter(byte parameter) throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(GAP);
        command.setType(parameter);
        TMCLCommand response = usb.write(command);
        if (response == null) {
            hasFault.set(true);
            throw new DeviceCommunicationException("Error getting parameter");
        }
        return response.getValue();
    }

    /**
     * Method that gets the value of a parameter
     * @param parameter the parameter to get
     * @throws ConfigurationException if there is an error getting the parameter
     * @return the value of the parameter
     */
    public int getParameter(String parameter) throws ConfigurationException {
        try {
            Field field = constantsClass.getField(parameter);
            return getParameter((byte) field.get(null));
        } catch (NoSuchFieldException | IllegalAccessException | DeviceCommunicationException e) {
            throw new ConfigurationException("Parameter not found: " + e.getMessage());
        }
    }

    /**
     * Method that moves the motor to the given position but waits for the movement to end
     *
     * @param mode     the mode of the movement
     * @param position the position to move to
     */
    private void MVP_till_end(byte mode, int position, long timeout) throws DeviceCommunicationException {
        moveToPosition(mode, position);
        waitTillPositionReached(timeout);
    }

    /**
     * Method that waits for the position to be reached. It checkes the position flag wich is 1 if the position is reached
     */
    private void waitTillPositionReached(long timeout) throws DeviceCommunicationException {
        long endTime = System.currentTimeMillis() + timeout;
        logger.debug("[TMCM_3351_MOTOR] Waiting for position to be reached, timeout: " + timeout + " ms, end time: " + endTime);
        while (true) {
            if (!isMoving.get()) throw new DeviceCommunicationException("Moving to position is cancelled");
            if (timeout > 0 && System.currentTimeMillis() > endTime) {
                this.setParameter(PARAM_CL_MODE, 0);
                this.stopMotor();
                this.hasFault.set(true);
                logger.error("[TMCM_3351_MOTOR] Moving to position out of time");
                throw new DeviceCommunicationException("Timeout waiting for position to be reached");
            }
            int status = getParameter(PARAM_POSITION_FLAG);
            //1 = reached , 0 = not reached
            if (status == 1) {
                break;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
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
     * Method that moves the motor to the given position and waits for the movement to end
     *
     * @param position the position to move to
     * @param timeout  the timeout in milliseconds
     * @return a future that represents the result of the operation
     */
    public Future<DeviceCommunicationException> moveToRelativePositionAndWait(int position, long timeout) {
        return executorService.submit(() -> {
            logger.debug("[TMCM_3351_MOTOR] Moving to relative position: " + position);
            try {
                waitStatusUnlock();
                isMoving.set(true);
                targetPos.set(currentPos.get() + position);
                long computedTimeout = timeout;
                if (timeout < 0) {
                    computedTimeout = estimatedArrivalTime(position) * 2L;
                }
                stateFunction.notifyStateChange();
                MVP_till_end(MVP_REL, position, computedTimeout);
                isMoving.set(false);
                currentPos.set(targetPos.get());
                stateFunction.notifyStateChange();
                return null;
            } catch (Exception e) {
                logger.error("[TMCM_3351_MOTOR] Error moving to position: " + e.getMessage());
                if (e instanceof DeviceCommunicationException) {
                    return (DeviceCommunicationException) e;
                } else {
                    return new DeviceCommunicationException(e.getMessage());
                }
            }
        });
    }

    /**
     * Method that estimates the arrival time of the motor to the given position
     *
     * @param distance the distance to the position
     * @return the estimated arrival time in milliseconds
     * @throws DeviceCommunicationException if there is an error getting the speed
     */
    long estimatedArrivalTime(int distance) throws DeviceCommunicationException {
        if (distance == 0) {
            return 0;
        }
        if (speed.get() == 0) {
            speed.set(getParameter(PARAM_MAX_SPEED));
        }
        if (acceleration.get() == 0) {
            acceleration.set(getParameter(PARAM_MAX_ACCELERATION));
        }
        if (deceleration.get() == 0) {
            deceleration.set(getParameter(PARAM_MAX_DECELERATION));
        }
        double speed_decimal = (double) speed.get();
        double acceleration_decimal = (double) acceleration.get();
        double deceleration_decimal = (double) deceleration.get();
        double distance_decimal = (double) Math.abs(distance);
        double time_speed = distance_decimal / speed_decimal;
        double time_acceleration = speed_decimal / acceleration_decimal;
        double time_deceleration = speed_decimal / deceleration_decimal;
        long time = (long) ((time_speed + time_acceleration + time_deceleration) * 1000);
        logger.debug("[TMCM_3351_MOTOR] Estimated arrival time: " + time + " ms for distance: " + distance_decimal + " at speed: " + speed_decimal + " with acceleration: " + acceleration_decimal + " and deceleration: " + deceleration_decimal);
        return Math.max(1000, time);
    }

    /**
     * Method that moves the motor to the given position and waits for the movement to end
     *
     * @param position the position to move to
     */
    public Future<DeviceCommunicationException> moveToRelativePositionAndWait(int position) {
        return moveToRelativePositionAndWait(position, 0);
    }

    /**
     * Method that moves the motor to the given position and waits for the movement to end
     *
     * @param position the position to move to
     * @param timeout  the timeout in milliseconds
     * @return a future that represents the result of the operation
     */
    public Future<DeviceCommunicationException> moveToAbsolutePositionAndWait(int position, long timeout) {
        return executorService.submit(() -> {
            logger.debug("[TMCM_3351_MOTOR] Moving to absolute position: " + position);
            try {
                waitStatusUnlock();
                isMoving.set(true);
                long computedTimeout = timeout;
                if (timeout < 0) {
                    computedTimeout = estimatedArrivalTime(position - currentPos.get()) * 2L;
                }
                targetPos.set(position);
                stateFunction.notifyStateChange();
                MVP_till_end(MVP_ABS, position, computedTimeout);
                isMoving.set(false);
                currentPos.set(targetPos.get());
                stateFunction.notifyStateChange();
                return null;
            } catch (Exception e) {
                logger.error("[TMCM_3351_MOTOR] Error moving to position: " + e.getMessage());
                if (e instanceof DeviceCommunicationException) {
                    return (DeviceCommunicationException) e;
                } else {
                    return new DeviceCommunicationException(e.getMessage());
                }
            }
        });
    }

    /**
     * Method that moves the motor to the given position and waits for the movement to end
     *
     * @param position the position to move to
     */
    public Future<DeviceCommunicationException> moveToAbsolutePositionAndWait(int position) {
        return moveToAbsolutePositionAndWait(position, 0);
    }


    /**
     * Method to query the driver for alarms
     *
     * @return the error flags
     * @throws DeviceCommunicationException if there is an error getting the error flags
     */
    public ErrorFlags getErrors() throws DeviceCommunicationException {
        return new ErrorFlags(getParameter(PARAM_ERROR_FLAGS));
    }

    /**
     * Method that clears the errors
     *
     * @throws DeviceCommunicationException if there is an error clearing the errors
     */
    public void clearErrors() throws DeviceCommunicationException {
        logger.debug("[TMCM_3351_MOTOR] Clearing errors");
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(CLE);
        command.setValue(0);
        usb.write(command);
    }

    /**
     * Method that enables the closed loop
     *
     * @param mode the mode of the closed loop
     * @throws DeviceCommunicationException if there is an error enabling the closed loop
     */
    public void enableClosedLoop(int mode) throws DeviceCommunicationException {
        setParameter(PARAM_CL_MODE, mode);
        while (true) {
            if (!isMoving.get()) throw new DeviceCommunicationException("Moving to position is cancelled");
            if (getParameter(PARAM_CLOSED_LOOP_INIT) == 1) {
                break;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new DeviceCommunicationException(e.getMessage());
            }
        }
    }

    /**
     * Method that disables the closed loop
     *
     * @throws DeviceCommunicationException if there is an error disabling the closed loop
     */
    public void disableClosedLoop() throws DeviceCommunicationException {
        setParameter(PARAM_CL_MODE, 0);
    }
}
