package com.lucaf.robotic_core.DH_ROBOTICS.RGI100_22;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.lucaf.robotic_core.State;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import lombok.Setter;

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
public class RGI100_22 {

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
     * The address id of the device
     * -- SETTER --
     * Public method that sets the address id of the device
     *
     * @param id the address id of the device
     */
    @Setter
    private byte id = 0x01;

    /**
     * The RS485 connection object
     */
    private final ModbusSerialMaster rs485;

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
    private AtomicBoolean is_moving = new AtomicBoolean(false);

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
        if (state.containsKey("is_moving")) is_moving.set((Boolean) state.get("is_moving"));
        state.put("is_moving", is_moving);
        state.put("is_initialized", is_initialized);
        state.put("has_fault", has_fault);
        state.put("fault", "");

        if (state.containsKey("current_position"))
            current_position.set(((Double) state.get("current_position")).intValue());
        state.put("current_position", current_position);

        if (state.containsKey("target_position"))
            target_position.set(((Double) state.get("target_position")).intValue());
        state.put("target_position", target_position);

        if (state.containsKey("current_angle")) target_position.set(((Double) state.get("current_angle")).intValue());
        state.put("current_angle", current_angle);

        if (state.containsKey("target_angle")) target_position.set(((Double) state.get("target_angle")).intValue());
        state.put("target_angle", target_angle);

        stateFunction.notifyStateChange();
    }


    /**
     * Constructor that initializes the RGI100_22 object with an existing RS485 connection
     *
     * @param rs485 the RS485 connection object
     */
    public RGI100_22(ModbusSerialMaster rs485, HashMap<String, Object> state, State notifyStateChange) {
        this.rs485 = rs485;
        this.state = state;
        this.stateFunction = notifyStateChange;
        if (stateFunction != null) initState();
    }

    public static boolean ping(ModbusSerialMaster rs485, int id) {
        try {
            Register[] regs = rs485.readMultipleRegisters(id, 0, 1);
            return regs != null;
        } catch (ModbusException e) {
            return false;
        }
    }

    /**
     * Constructor that initializes the RGI100_22 object with an existing RS485 connection
     *
     * @param rs485 the RS485 connection object
     * @param id    the address id of the device
     */
    public RGI100_22(ModbusSerialMaster rs485, byte id, HashMap<String, Object> state, State notifyStateChange) {
        this.rs485 = rs485;
        setId(id);
        this.state = state;
        this.stateFunction = notifyStateChange;
        if (stateFunction != null) initState();
        setupErrorListener();
    }

    /**
     * Internal method that periodically checks for errors
     */
    private void setupErrorListener() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                ErrorFlags errorFlags = getErrorFlags();
                if (errorFlags.hasError()) {
                    state.put("fault", errorFlags.getErrorDescription());
                    has_fault.set(true);
                    stateFunction.notifyError();
                }
            } catch (DeviceCommunicationException e) {
                state.put("fault", e.getMessage());
                has_fault.set(true);
                stateFunction.notifyError();
            }
        }, 1000, 1000, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Internal method that reads a register
     *
     * @param register the register to read
     * @return the response of the device
     */
    private synchronized int readRegister(byte[] register) throws ModbusException {

        int startRegister = register[0] << 8 | register[1];
        Register[] regs = rs485.readMultipleRegisters(id, startRegister, 1);
        if (regs != null) {
            return regs[0].getValue();
        }
        return -1;

    }

    /**
     * Internal method that writes a register
     *
     * @param register the register to write
     * @param data     the data to write
     * @return true if the write is successful, false otherwise
     */
    private synchronized boolean writeRegister(byte[] register, int data) throws ModbusException {
        int startRegister = register[0] << 8 | register[1];
        rs485.writeSingleRegister(id, startRegister, new SimpleInputRegister(data));
        return true;

    }

    /**
     * Checks if the grip is ready. You need to initialize the grip before using it
     *
     * @return true if the grip is initialized, false otherwise
     */
    public boolean hasGripInitialized() throws DeviceCommunicationException {
        try {
            int response = readRegister(FEEDBACK_INITIALIZATION_GRIP_STATE);
            if (response == -1) {
                return false;
            }
            return response == 1;
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
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
    public Future<Boolean> initialize(){
        return executorServiceGrip.submit(() -> {
            try {
                writeRegister(INITIALIZATION, 1);
                while (!hasGripInitialized() || !hasRotationInitialized()) {
                    Thread.sleep(500);
                }
                is_initialized.set(true);
                stateFunction.notifyStateChange();
                setupErrorListener();
            } catch (Exception e) {
                return false;
            }
            is_initialized.set(true);
            target_angle.set(0);
            target_position.set(0);
            current_angle.set(0);
            current_position.set(0);
            stateFunction.notifyStateChange();
            setupErrorListener();
            return true;
        });
    }

    /**
     * Moves the rotation to an absolute angle
     *
     * @param angle the angle to move to
     */
    public void moveToAbsoluteAngle(int angle) throws DeviceCommunicationException {
        try {
            writeRegister(ABSOLUTE_ROTATION, angle);
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
        FALL
    }

    /**
     * Checks if the grip is rotating
     *
     * @return the feedback of the position
     */
    public PositionFeedback isRotationMoving() throws DeviceCommunicationException {
        try {
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
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Checks if the grip is moving
     *
     * @return the feedback of the position
     */
    public PositionFeedback isGripMoving() throws DeviceCommunicationException {
        try {
            int response = readRegister(FEEDBACK_GRIP_STATE);
            if (response == -1) {
                return PositionFeedback.MOVING;
            }
            return switch (response) {
                case 0 -> PositionFeedback.MOVING;
                case 1 -> PositionFeedback.REACHED_WITH_OBJ;
                case 2 -> PositionFeedback.REACHED_WITHOUT_OBJ;
                case 3 -> PositionFeedback.FALL;
                default -> PositionFeedback.MOVING;
            };
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Gets the grip position
     *
     * @return the grip position
     */
    public int getGripPosition() throws DeviceCommunicationException {
        try {
            int response = readRegister(FEEDBACK_GRIP_POSITION);
            if (response == -1) {
                return -1;
            }
            return response;
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Waits for the grip to reach the end position. This method is thread blocking and should be used after an interpolation.
     */
    public void waitEndPosition() throws DeviceCommunicationException {
        try {
            is_moving.set(true);
            while (true) {
                if (has_fault.get()) {
                    throw new DeviceCommunicationException("Device has fault");
                }
                PositionFeedback feedback = isRotationMoving();
                if (feedback == PositionFeedback.REACHED || feedback == PositionFeedback.BLOCKED) {
                    break;
                }
                Thread.sleep(100);
            }
            is_moving.set(false);
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Waits for the grip to reach the end position. This method is thread blocking and should be used after an interpolation.
     */
    public void waitEndGrip() throws DeviceCommunicationException {
        try {
            is_moving.set(true);
            while (true) {
                if (has_fault.get()) {
                    throw new DeviceCommunicationException("Device has fault");
                }
                PositionFeedback feedback = isGripMoving();
                if (feedback == PositionFeedback.REACHED_WITH_OBJ || feedback == PositionFeedback.REACHED_WITHOUT_OBJ) {
                    break;
                }
                Thread.sleep(300);
            }
            is_moving.set(false);
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Moves the rotation to an absolute angle and waits for the end position
     *
     * @param angle the angle to move to
     * @return a future that returns true if the movement is successful
     */
    public Future<Boolean> moveToRelativeAngleAndWait(int angle) {
        return executorServiceRotator.submit(() -> {
            try {
                int newAngle = angle + current_position.get();
                target_angle.set(newAngle);
                is_moving.set(true);
                stateFunction.notifyStateChange();
                moveToRelativeAngle(angle);
                waitEndPosition();
                current_angle.set(newAngle);
                is_moving.set(false);
                stateFunction.notifyStateChange();
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Moves the rotation to a relative angle and waits for the end position
     *
     * @param angle the angle to move to
     * @return a future that returns true if the movement is successful
     */
    public Future<Boolean> moveToAbsoluteAngleAndWait(int angle) {
        return executorServiceRotator.submit(() -> {
            try {
                target_angle.set(angle);
                is_moving.set(true);
                stateFunction.notifyStateChange();
                moveToAbsoluteAngle(angle);
                waitEndPosition();
                current_angle.set(angle);
                is_moving.set(false);
                stateFunction.notifyStateChange();
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Allows to set the grip position. The position is a value between 0 and 1000
     *
     * @param position the position to set
     */
    public void setGripPosition(int position) throws DeviceCommunicationException {
        try {
            position = Math.max(0, position);
            position = Math.min(1000, position);
            writeRegister(TARGET_POSITION, position);
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Allows to set the grip position and wait for the end position
     *
     * @param position the position to set
     * @return a future that returns true if the movement is successful
     */
    public Future<Boolean> setGripPositionAndWait(int position) {
        return executorServiceGrip.submit(() -> {
            try {
                target_position.set(position);
                is_moving.set(true);
                stateFunction.notifyStateChange();
                setGripPosition(position);
                waitEndGrip();
                current_position.set(position);
                is_moving.set(false);
                stateFunction.notifyStateChange();
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Gets the grip force value
     *
     * @return the grip force value between 0 and 100
     */
    public int getGripForce() throws DeviceCommunicationException {
        try {
            int response = readRegister(FORCE);
            return response;
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Sets the grip force value
     *
     * @param force the force value between 0 and 100
     */
    public void setGripForce(int force) throws DeviceCommunicationException {
        try {
            force = Math.max(0, force);
            force = Math.min(100, force);
            writeRegister(FORCE, force);
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Gets the grip speed value
     *
     * @return the grip speed value between 0 and 100
     */
    public int getGripSpeed() throws DeviceCommunicationException {
        try {
            int response = readRegister(SPEED);
            if (response == -1) {
                return -1;
            }
            return response;
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Changes the address of the device
     *
     * @param id the new address id
     * @return true if the address is changed, false otherwise
     */
    public boolean changeAddress(int id) throws DeviceCommunicationException {
        try {
            boolean ok = writeRegister(SLAVE_ADDRESS, id);
            //if (ok) setId((byte) id);
            return ok;
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
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
        try {
            speed = Math.min(100, speed);
            speed = Math.max(0, speed);
            writeRegister(SPEED, speed);
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Gets the rotation speed value
     *
     * @return the rotation speed value between 0 and 100
     */
    public int getRotationSpeed() throws DeviceCommunicationException {
        try {
            int response = readRegister(ROTATION_SPEED);
            if (response == -1) {
                return -1;
            }
            return response;
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Sets the rotation speed value
     *
     * @param speed the speed value between 0 and 100
     */
    public void setRotationSpeed(int speed) throws DeviceCommunicationException {
        try {
            speed = Math.max(0, speed);
            speed = Math.min(100, speed);
            writeRegister(ROTATION_SPEED, speed);
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Gets the rotation force value
     *
     * @return the rotation force value between 0 and 100
     */
    public int getRotationForce() throws DeviceCommunicationException {
        try {
            int response = readRegister(ROTATION_FORCE);
            if (response == -1) {
                return -1;
            }
            return response;
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Sets the rotation force value
     *
     * @param force the force value between 0 and 100
     */
    public void setRotationForce(int force) throws DeviceCommunicationException {
        try {
            force = Math.max(0, force);
            force = Math.min(100, force);
            writeRegister(ROTATION_FORCE, force);
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Saves the configuration of the device. Speed, force, position and grip force are not saved on restart by default
     *
     * @param defaults if true, the default configuration is restored
     * @return
     */
    public boolean saveConfig(boolean defaults) throws DeviceCommunicationException {
        try {
            return writeRegister(SAVE_CONFIG, defaults ? 0 : 1);
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Gets the error flags of the device
     *
     * @return the error flags
     * @throws DeviceCommunicationException if the thread is interrupted
     */
    public ErrorFlags getErrorFlags() throws DeviceCommunicationException {
        try {
            int response = readRegister(FEEDBACK_GRIP_ERROR_CODE);
            return new ErrorFlags(response);
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Clears the error flags of the device
     *
     * @throws DeviceCommunicationException if the thread is interrupted
     */
    public void clearError() throws DeviceCommunicationException {
        try {
            //Todo, cant find register
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }
}
