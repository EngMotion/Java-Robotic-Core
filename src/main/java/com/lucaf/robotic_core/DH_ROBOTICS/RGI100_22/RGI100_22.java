package com.lucaf.robotic_core.DH_ROBOTICS.RGI100_22;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.lucaf.robotic_core.COMMON.ModbusRTUDevice;
import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.Pair;
import com.lucaf.robotic_core.State;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lucaf.robotic_core.DH_ROBOTICS.RGI100_22.Constants.*;

/**
 * Class that manages the RGI100_22 device (Gripper and Rotation)
 */
public class RGI100_22 extends ModbusRTUDevice {

    /**
     * The executor service. It is used to run the commands in parallel
     */
    private ExecutorService executorServiceGrip = Executors.newFixedThreadPool(1);

    /**
     * The executor service. It is used to run the commands in parallel
     */
    private ExecutorService executorServiceRotator = Executors.newFixedThreadPool(1);

    /**
     * The scheduled executor service. It is used to periodic check of errors
     */
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * The state of the device
     */
    private final HashMap<String, Object> state;

    /**
     * The state class
     */
    private final State stateFunction;

    /**
     * Internal state of the device
     */
    private AtomicBoolean is_moving_grip = new AtomicBoolean(false);

    /**
     * Internal state of the device
     */
    private AtomicBoolean is_moving_rotator = new AtomicBoolean(false);

    /**
     * Internal state of the device
     */
    private AtomicBoolean is_initialized = new AtomicBoolean(false);

    /**
     * Internal state of the current position
     */
    private AtomicInteger current_position = new AtomicInteger(0);

    /**
     * Internal state of the target position
     */
    private AtomicInteger target_position = new AtomicInteger(0);

    /**
     * Internal state of the target angle
     */
    private AtomicInteger target_angle = new AtomicInteger(0);

    /**
     * Internal state of the current angle
     */
    private AtomicInteger current_angle = new AtomicInteger(0);

    /**
     * Fault boolean
     */
    private AtomicBoolean has_fault = new AtomicBoolean(false);

    /**
     * Initializes the state
     */
    void initState() {
        if (state.containsKey("is_moving_grip")) {
            if (state.get("is_moving_grip") instanceof Boolean) {
                is_moving_grip.set((Boolean) state.get("is_moving_grip"));
            } else if (state.get("is_moving_grip") instanceof AtomicBoolean) {
                is_moving_grip = (AtomicBoolean) state.get("is_moving_grip");
            }
        }
        state.put("is_moving_grip", is_moving_grip);
        if (state.containsKey("is_moving_rotator")) {
            if (state.get("is_moving_rotator") instanceof Boolean) {
                is_moving_rotator.set((Boolean) state.get("is_moving_rotator"));
            } else if (state.get("is_moving_rotator") instanceof AtomicBoolean) {
                is_moving_rotator = (AtomicBoolean) state.get("is_moving_rotator");
            }
        }
        state.put("is_moving_rotator", is_moving_rotator);
        state.put("is_initialized", is_initialized);
        state.put("has_fault", has_fault);
        state.put("fault", "");

        if (state.containsKey("current_position")) {
            if (state.get("current_position") instanceof AtomicInteger) {
                current_position = (AtomicInteger) state.get("current_position");
            } else if (state.get("current_position") instanceof Integer) {
                current_position.set((Integer) state.get("current_position"));
            } else if (state.get("current_position") instanceof Double) {
                current_position.set(((Double) state.get("current_position")).intValue());
            }
        }
        state.put("current_position", current_position);

        if (state.containsKey("target_position")) {
            if (state.get("target_position") instanceof AtomicInteger) {
                target_position = (AtomicInteger) state.get("target_position");
            } else if (state.get("target_position") instanceof Integer) {
                target_position.set((Integer) state.get("target_position"));
            } else if (state.get("target_position") instanceof Double) {
                target_position.set(((Double) state.get("target_position")).intValue());
            }
        }
        state.put("target_position", target_position);

        if (state.containsKey("current_angle")) {
            if (state.get("current_angle") instanceof AtomicInteger) {
                current_angle = (AtomicInteger) state.get("current_angle");
            } else if (state.get("current_angle") instanceof Integer) {
                current_angle.set((Integer) state.get("current_angle"));
            } else if (state.get("current_angle") instanceof Double) {
                current_angle.set(((Double) state.get("current_angle")).intValue());
            }
        }
        state.put("current_angle", current_angle);

        if (state.containsKey("target_angle")) {
            if (state.get("target_angle") instanceof AtomicInteger) {
                target_angle = (AtomicInteger) state.get("target_angle");
            } else if (state.get("target_angle") instanceof Integer) {
                target_angle.set((Integer) state.get("target_angle"));
            } else if (state.get("target_angle") instanceof Double) {
                target_angle.set(((Double) state.get("target_angle")).intValue());
            }
        }
        state.put("target_angle", target_angle);

        stateFunction.notifyStateChange();
    }

    /**
     * Constructor that initializes the RGI100_22 object with an existing RS485 connection
     *
     * @param rs485             the RS485 connection object
     * @param state             the state of the device
     * @param notifyStateChange the state class, includes the onStateChange method
     * @param logger            the logger class
     */
    public RGI100_22(ModbusSerialMaster rs485, HashMap<String, Object> state, State notifyStateChange, Logger logger) {
        super("RGI100_22", rs485, logger);
        this.state = state;
        this.stateFunction = notifyStateChange;
        if (stateFunction != null) initState();
    }

    /**
     * Constructor that initializes the RGI100_22 object with an existing RS485 connection
     *
     * @param rs485             the RS485 connection object
     * @param id                the address id of the device
     * @param state             the state of the device
     * @param notifyStateChange the state class, includes the onStateChange method
     * @param logger            the logger class
     */
    public RGI100_22(ModbusSerialMaster rs485, byte id, HashMap<String, Object> state, State notifyStateChange, Logger logger) {
        super("RGI100_22", rs485, logger);
        setId(id);
        this.state = state;
        this.stateFunction = notifyStateChange;
        if (stateFunction != null) initState();
    }

    /**
     * Method that checks if the device is connected
     *
     * @param rs485 the RS485 connection object
     * @param id    the address id of the device
     * @return true if the device is connected, false otherwise
     */
    public static boolean ping(ModbusSerialMaster rs485, int id) {
        try {
            Register[] regs = rs485.readMultipleRegisters(id, 0, 1);
            return regs != null;
        } catch (ModbusException e) {
            return false;
        }
    }

    /**
     * Internal method that periodically checks for errors
     */
    private void setupErrorListener() {
        logger.debug("[RGI100_22] Setting up error listener");
        if (scheduledExecutorService != null) {
            logger.debug("[RGI100_22] Shutting down previous error listener");
            scheduledExecutorService.shutdown();
        }
        logger.debug("[RGI100_22] Setting up new error listener");
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (scheduledExecutorService == null || scheduledExecutorService.isShutdown() || has_fault.get()) return;
            try {
                logger.debug("[RGI100_22] Checking for errors");
                ErrorFlags errorFlags = getErrorFlags();
                logger.debug("[RGI100_22] Error flags: " + errorFlags);
                if (errorFlags.hasError()) {
                    state.put("fault", errorFlags.getErrorDescription());
                    has_fault.set(true);
                    stateFunction.notifyError();
                }
            } catch (DeviceCommunicationException e) {
                logger.error("[RGI100_22] Error checking for errors: " + e.getMessage());
                state.put("fault", e.getMessage());
                has_fault.set(true);
                stateFunction.notifyError();
            }
        }, 1000, 1000, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the scheduled executor service and the device
     *
     * @throws DeviceCommunicationException if there is an error in the communication with the device
     */
    public void stop(boolean listener) throws DeviceCommunicationException {
        logger.log("[RGI100_22] Stopping device");
        if (listener && scheduledExecutorService != null) {
            logger.debug("[RGI100_22] Shutting down error listener");
            scheduledExecutorService.shutdown();
        }
        logger.debug("[RGI100_22] Stopping device");
        is_moving_grip.set(false);
        is_moving_rotator.set(false);
        writeRegister(STOP, 1);
    }

    /**
     * Stops the scheduled executor service and the device
     *
     * @throws ModbusException if there is an error in the communication with the device
     */
    public void stop() throws DeviceCommunicationException {
        stop(false);
    }

    /**
     * Checks if the grip is ready. You need to initialize the grip before using it
     *
     * @return true if the grip is initialized, false otherwise
     */
    public boolean hasGripInitialized() throws DeviceCommunicationException {
        int response = readRegister(FEEDBACK_INITIALIZATION_GRIP_STATE);
        if (response == -1) {
            return false;
        }
        return response == 1;
    }

    /**
     * Checks if the rotation is ready. You need to initialize the rotation before using it
     *
     * @return true if the rotation is initialized, false otherwise
     */
    public boolean hasRotationInitialized() throws DeviceCommunicationException {
        try {
            int response = readRegister(FEEDBACK_INITIALIZATION_ROTATION_STATE);
            if (response == -1) {
                return false;
            }
            return response == 1;
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Initializes the grip and the rotation
     *
     * @return a future that returns true if the initialization is successful
     */
    public Future<Boolean> initialize() {
        return executorServiceGrip.submit(() -> {
            logger.log("[RGI100_22] Initializing device");
            is_moving_grip.set(true);
            is_moving_rotator.set(true);
            try {
                writeRegister(INITIALIZATION, 1);
                do {
                    if (!is_moving_grip.get()) return false;
                    Thread.sleep(300);
                } while (!hasGripInitialized() || !hasRotationInitialized());
                logger.debug("[RGI100_22] Device initialized");
                is_initialized.set(true);
                target_angle.set(0);
                target_position.set(0);
                current_angle.set(0);
                current_position.set(0);
                is_moving_grip.set(false);
                is_moving_rotator.set(false);
                stateFunction.notifyStateChange();
                setupErrorListener();
                return true;
            } catch (Exception e) {
                logger.error("[RGI100_22] Error initializing device: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Moves the rotation to an absolute angle
     *
     * @param angle the angle to move to
     */
    public void moveToAbsoluteAngle(long angle) throws DeviceCommunicationException {
        try {
            writeLongRegister(ABSOLUTE_ROTATION, angle, true);
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Moves the rotation to a relative angle
     *
     * @param angle the angle to move to
     */
    public void moveToRelativeAngle(int angle) throws DeviceCommunicationException {
        try {
            writeRegister(RELATIVE_ROTATION, angle);
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Enum that defines the possible feedbacks of the position
     */
    public enum PositionFeedback {
        MOVING,
        REACHED,
        BLOCKED,
        REACHED_WITH_OBJ,
        REACHED_WITHOUT_OBJ,
        FALL,
        ERROR
    }


    /**
     * Checks if the grip is rotating
     *
     * @return the feedback of the position
     */
    public PositionFeedback isRotationMoving() throws DeviceCommunicationException {
        int response = readRegister(FEEDBACK_ROTATION_STATE);
        if (response == -1) {
            return PositionFeedback.MOVING;
        }
        return switch (response) {
            case 0 -> PositionFeedback.MOVING;
            case 1 -> PositionFeedback.REACHED;
            case 2 -> PositionFeedback.BLOCKED;
            default -> PositionFeedback.MOVING;
        };
    }

    /**
     * Checks if the grip is moving
     *
     * @return the feedback of the position
     */
    public PositionFeedback isGripMoving() throws DeviceCommunicationException {
        int response = readRegister(FEEDBACK_GRIP_STATE);
        if (response == -1) {
            return PositionFeedback.MOVING;
        }
        return switch (response) {
            case 0 -> PositionFeedback.MOVING;
            case 1 -> PositionFeedback.REACHED_WITHOUT_OBJ;
            case 2 -> PositionFeedback.REACHED_WITH_OBJ;
            case 3 -> PositionFeedback.FALL;
            default -> PositionFeedback.MOVING;
        };
    }

    /**
     * Gets the grip position
     *
     * @return the grip position
     */
    public int getGripPosition() throws DeviceCommunicationException {
        int response = readRegister(FEEDBACK_GRIP_POSITION);
        if (response == -1) {
            return -1;
        }
        return response;
    }

    /**
     * Waits for the grip to reach the end position. This method is thread blocking and should be used after an interpolation.
     */
    public PositionFeedback waitEndPosition() throws DeviceCommunicationException {
        is_moving_rotator.set(true);
        PositionFeedback feedback;
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (!is_moving_rotator.get()) {
                throw new DeviceCommunicationException("Device is not moving");
            }
            if (has_fault.get()) {
                throw new DeviceCommunicationException("Device has fault");
            }
            feedback = isRotationMoving();
            if (feedback == PositionFeedback.REACHED || feedback == PositionFeedback.BLOCKED) {
                break;
            }
        }
        is_moving_rotator.set(false);
        return feedback;
    }

    /**
     * Waits for the grip to reach the end position. This method is thread blocking and should be used after an interpolation.
     */
    public PositionFeedback waitEndGrip() throws DeviceCommunicationException {
        is_moving_grip.set(true);
        PositionFeedback feedback;
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (!is_moving_grip.get()) {
                throw new DeviceCommunicationException("Device is not moving");
            }
            if (has_fault.get()) {
                throw new DeviceCommunicationException("Device has fault");
            }
            feedback = isGripMoving();
            if (feedback == PositionFeedback.REACHED_WITH_OBJ || feedback == PositionFeedback.REACHED_WITHOUT_OBJ || feedback == PositionFeedback.FALL) {
                break;
            }
        }
        is_moving_grip.set(false);
        return feedback;
    }

    /**
     * Moves the rotation to an absolute angle and waits for the end position
     *
     * @param angle the angle to move to
     * @return a future that returns true if the movement is successful
     */
    public Future<Pair<Boolean, PositionFeedback>> moveToRelativeAngleAndWait(int angle) {
        return executorServiceRotator.submit(() -> {
            try {
                logger.debug("[RGI100_22] Moving to relative angle: " + angle);
                int newAngle = angle + current_position.get();
                target_angle.set(newAngle);
                is_moving_rotator.set(true);
                stateFunction.notifyStateChange();
                moveToRelativeAngle(angle);
                PositionFeedback feedback = waitEndPosition();
                current_angle.set(newAngle);
                is_moving_rotator.set(false);
                stateFunction.notifyStateChange();
                return new Pair<>(true, feedback);
            } catch (Exception e) {
                logger.error("[RGI100_22] Error moving to relative angle: " + e.getMessage());
                return new Pair<>(false, PositionFeedback.ERROR);
            }
        });
    }

    /**
     * Moves the rotation to a relative angle and waits for the end position
     *
     * @param angle the angle to move to
     * @return a future that returns true if the movement is successful
     */
    public Future<Pair<Boolean, PositionFeedback>> moveToAbsoluteAngleAndWait(int angle) {
        return executorServiceRotator.submit(() -> {
            try {
                logger.debug("[RGI100_22] Moving to absolute angle: " + angle);
                target_angle.set(angle);
                is_moving_rotator.set(true);
                stateFunction.notifyStateChange();
                moveToAbsoluteAngle(angle);
                PositionFeedback feedback = waitEndPosition();
                current_angle.set(angle);
                is_moving_rotator.set(false);
                stateFunction.notifyStateChange();
                return new Pair<>(true, feedback);
            } catch (Exception e) {
                logger.error("[RGI100_22] Error moving to absolute angle: " + e.getMessage());
                return new Pair<>(false, PositionFeedback.ERROR);
            }
        });
    }

    /**
     * Allows to set the grip position. The position is a value between 0 and 1000
     *
     * @param position the position to set
     */
    public void setGripPosition(int position) throws DeviceCommunicationException {
        position = Math.max(0, position);
        position = Math.min(1000, position);
        writeRegister(TARGET_POSITION, position);
    }

    /**
     * Allows to set the grip position and wait for the end position
     *
     * @param position the position to set
     * @return a future that returns true if the movement is successful
     */
    public Future<Pair<Boolean, PositionFeedback>> setGripPositionAndWait(int position) {
        return executorServiceGrip.submit(() -> {
            try {
                logger.debug("[RGI100_22] Setting grip position: " + position);
                target_position.set(position);
                is_moving_grip.set(true);
                stateFunction.notifyStateChange();
                setGripPosition(position);
                PositionFeedback feedback = waitEndGrip();
                current_position.set(position);
                is_moving_grip.set(false);
                stateFunction.notifyStateChange();
                return new Pair<>(true, feedback);
            } catch (Exception e) {
                logger.error("[RGI100_22] Error setting grip position: " + e.getMessage());
                return new Pair<>(false, PositionFeedback.ERROR);
            }
        });
    }

    /**
     * Gets the grip force value
     *
     * @return the grip force value between 0 and 100
     */
    public int getGripForce() throws DeviceCommunicationException {

        return readRegister(FORCE);
    }

    /**
     * Sets the grip force value
     *
     * @param force the force value between 0 and 100
     */
    public void setGripForce(int force) throws DeviceCommunicationException {
        force = Math.max(0, force);
        force = Math.min(100, force);
        writeRegister(FORCE, force);
    }

    /**
     * Gets the grip speed value
     *
     * @return the grip speed value between 0 and 100
     */
    public int getGripSpeed() throws DeviceCommunicationException {
        return readRegister(SPEED);
    }

    /**
     * Changes the address of the device
     *
     * @param id the new address id
     * @return true if the address is changed, false otherwise
     */
    public boolean changeAddress(int id) throws DeviceCommunicationException {
        return writeRegister(SLAVE_ADDRESS, id);
    }

    /**
     * Saves the configuration of the device
     *
     * @return true if the configuration is saved, false otherwise
     */
    public boolean saveConfig() throws DeviceCommunicationException {
        return saveConfig(false);
    }

    /**
     * Sets the grip speed value
     *
     * @param speed the speed value between 0 and 100
     */
    public void setGripSpeed(int speed) throws DeviceCommunicationException {

        speed = Math.min(100, speed);
        speed = Math.max(0, speed);
        writeRegister(SPEED, speed);
    }

    /**
     * Gets the rotation speed value
     *
     * @return the rotation speed value between 0 and 100
     */
    public int getRotationSpeed() throws DeviceCommunicationException {
        return readRegister(ROTATION_SPEED);
    }

    /**
     * Sets the rotation speed value
     *
     * @param speed the speed value between 0 and 100
     */
    public void setRotationSpeed(int speed) throws DeviceCommunicationException {
        speed = Math.max(0, speed);
        speed = Math.min(100, speed);
        writeRegister(ROTATION_SPEED, speed);
    }

    /**
     * Gets the rotation force value
     *
     * @return the rotation force value between 0 and 100
     */
    public int getRotationForce() throws DeviceCommunicationException {
        return readRegister(ROTATION_FORCE);
    }

    /**
     * Sets the rotation force value
     *
     * @param force the force value between 0 and 100
     */
    public void setRotationForce(int force) throws DeviceCommunicationException {
        force = Math.max(0, force);
        force = Math.min(100, force);
        writeRegister(ROTATION_FORCE, force);
    }

    /**
     * Saves the configuration of the device. Speed, force, position and grip force are not saved on restart by default
     *
     * @param defaults if true, the default configuration is restored
     * @return
     */
    public boolean saveConfig(boolean defaults) throws DeviceCommunicationException {
        return writeRegister(SAVE_CONFIG, defaults ? 0 : 1);
    }

    /**
     * Gets the error flags of the device
     *
     * @return the error flags
     * @throws DeviceCommunicationException if the thread is interrupted
     */
    public ErrorFlags getErrorFlags() throws DeviceCommunicationException {
        int response = readRegister(FEEDBACK_GRIP_ERROR_CODE);
        return new ErrorFlags(response);
    }

    /**
     * Clears the error flags of the device
     *
     * @throws DeviceCommunicationException if the thread is interrupted
     */
    public void clearError() throws DeviceCommunicationException {
        //Todo, cant find register
        logger.error("[RGI100_22] Clearing error not implemented");
    }
}
