package com.lucaf.robotic_core.dhRobotics.sacN;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.lucaf.robotic_core.Logger;
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

import static com.lucaf.robotic_core.dhRobotics.sacN.Constants.*;

public class SAC_N {

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
     * Internal state of the device
     */
    private AtomicBoolean is_moving = new AtomicBoolean(false);


    /**
     * Internal state of the device
     */
    AtomicBoolean is_initialized = new AtomicBoolean(false);

    /**
     * Internal state of the current position
     */
    private AtomicInteger current_position = new AtomicInteger(0);

    /**
     * Internal state of the target position
     */
    private AtomicInteger target_position = new AtomicInteger(0);

    /**
     * Internal state of the fault
     */
    private AtomicBoolean has_fault = new AtomicBoolean(false);

    /**
     * Global logger class
     */
    private final Logger logger;

    /**
     * Initializes the state
     */
    void initState() {
        if (state.containsKey("is_moving")) {
            if (state.get("is_moving") instanceof Boolean) {
                is_moving.set((Boolean) state.get("is_moving"));
            }else if (state.get("is_moving") instanceof AtomicBoolean) {
                is_moving = (AtomicBoolean) state.get("is_moving");
            }
        }
        state.put("is_moving", is_moving);
        state.put("is_initialized", is_initialized);
        state.put("has_fault", has_fault);
        state.put("fault", "");

        if(state.containsKey("current_position")){
            if (state.get("current_position") instanceof AtomicInteger) {
                current_position = (AtomicInteger) state.get("current_position");
            }else if (state.get("current_position") instanceof Integer) {
                current_position.set((Integer) state.get("current_position"));
            }else if (state.get("current_position") instanceof Double) {
                current_position.set(((Double) state.get("current_position")).intValue());
            }
        }
        state.put("current_position", current_position);

        if(state.containsKey("target_position")){
            if (state.get("target_position") instanceof AtomicInteger) {
                target_position = (AtomicInteger) state.get("target_position");
            }else if (state.get("target_position") instanceof Integer) {
                target_position.set((Integer) state.get("target_position"));
            }else if (state.get("target_position") instanceof Double) {
                target_position.set(((Double) state.get("target_position")).intValue());
            }
        }
        state.put("target_position", target_position);
    }

    /**
     * The executor service. It is used to run the move commands in a separate thread
     */
    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    /**
     * The scheduled executor service. It is used to check for faults
     */
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * The state class
     */
    private final State stateFunction;

    /**
     * Constructor that initializes the SAC_N object with an existing RS485 connection
     * @param rs485 the RS485 connection object
     * @param state the state of the device
     * @param notifyStateChange the state class, includes the onStateChange method
     * @param logger the logger class
     */
    public SAC_N(ModbusSerialMaster rs485, HashMap<String, Object> state, State notifyStateChange, Logger logger) {
        this.rs485 = rs485;
        this.state = state;
        this.stateFunction = notifyStateChange;
        if (stateFunction != null) initState();
        this.logger = logger;
    }

    /**
     * Constructor that initializes the SAC_N object with an existing RS485 connection and an id
     * @param rs485 the RS485 connection object
     * @param id the address id of the device
     * @param state the state of the device
     * @param notifyStateChange the state class, includes the onStateChange method
     * @param logger the logger class
     */
    public SAC_N(ModbusSerialMaster rs485, byte id, HashMap<String, Object> state, State notifyStateChange, Logger logger) {
        this.rs485 = rs485;
        setId(id);
        this.state = state;
        this.stateFunction = notifyStateChange;
        if (stateFunction != null) initState();
        this.logger = logger;
    }

    /**
     * Internal method that periodically checks for errors
     */
    private void setupErrorListener() {
        logger.log("[SAC_N] Setting up error listener");
        if (scheduledExecutorService != null) {
            logger.debug("[SAC_N] Shutting down previous error listener");
            scheduledExecutorService.shutdown();
        }
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                if (scheduledExecutorService== null || scheduledExecutorService.isShutdown()) return;
                ErrorFlags errorFlags = getErrors();
                logger.debug("[SAC_N] Checking for errors: " + errorFlags.toString());
                if (errorFlags.hasError()) {
                    state.put("fault", errorFlags.getErrorDescription());
                    has_fault.set(true);
                    stateFunction.notifyError();
                }
            } catch (DeviceCommunicationException e) {
                logger.error("[SAC_N] Error checking for errors: " + e.getMessage());
                state.put("fault", e.getMessage());
                has_fault.set(true);
                stateFunction.notifyError();
            }
        }, 1000, 1000, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the scheduled executor service and the device
     * @throws DeviceCommunicationException if there is an error in the communication with the device
     */
    public void stop(boolean listener) throws DeviceCommunicationException {
        logger.log("[SAC_N] Stopping device");
        if (scheduledExecutorService != null && listener) {
            scheduledExecutorService.shutdown();
        }
        is_moving.set(false);
        setEnabled(false);
    }

    /**
     * Stops the scheduled executor service and the device
     * @throws DeviceCommunicationException if there is an error in the communication with the device
     */
    public void stop() throws DeviceCommunicationException {
        stop(true);
    }

    /**
     * Internal method that reads a register
     *
     * @param register the register to read
     * @return the response of the device
     */
    private synchronized int readRegister(byte[] register) throws DeviceCommunicationException {
        try {
            logger.debug("[SAC_N] Reading register: " + Integer.toHexString(register[0] << 8 | register[1]));
            int startRegister = register[0] << 8 | register[1];
            Register[] regs = rs485.readMultipleRegisters(id, startRegister, 1);
            if (regs != null) {
                logger.debug("[SAC_N] Read register: " + regs[0].getValue());
                return regs[0].getValue();
            }
            return -1;
        } catch (ModbusException e) {
            logger.error("[SAC_N] Error reading register " + Integer.toHexString(register[0] << 8 | register[1]) + ": " + e.getMessage());
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Internal method that writes a register
     *
     * @param register the register to write
     * @param data     the data to write
     * @return true if the write is successful, false otherwise
     */
    private synchronized boolean writeRegister(byte[] register, int data) throws DeviceCommunicationException {
        try {
            logger.debug("[SAC_N] Writing register: " + Integer.toHexString(register[0] << 8 | register[1]) + " with data: " + data);
            int startRegister = register[0] << 8 | register[1];
            rs485.writeSingleRegister(id, startRegister, new SimpleInputRegister(data));
            return true;
        } catch (ModbusException e) {
            logger.error("[SAC_N] Error writing register " + Integer.toHexString(register[0] << 8 | register[1]) + ": " + e.getMessage());
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Sets the speed of the device
     *
     * @param speed the speed to write
     */
    public void setSpeed(int speed) throws DeviceCommunicationException {
        try {
            writeRegister(SPEED, speed);
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Sets the acceleration of the device
     *
     * @param velocity the acceleration to write
     */
    public void setAcceleration(int velocity) throws DeviceCommunicationException {
        try {
            writeRegister(ACCELERATION, velocity);
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Sets the device as enabled or disabled
     *
     * @param enabled true if the device is enabled, false otherwise
     */
    public void setEnabled(boolean enabled) throws DeviceCommunicationException {
        try {
            writeRegister(START, enabled ? 1 : 0);
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Reads if the device has reached the target position
     *
     * @return true if the device has reached the target position, false otherwise
     */
    public boolean is_arrived() throws DeviceCommunicationException {
        try {
            int response = readRegister(FEEDBACK_MOTION_STATE);
            if (response != -1) {
                int data = response;
                return data == 1;
            }
            return false;
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Reads the current status of the device
     *
     * @return Status object with the current status of the device
     */
    public Status getStatus() throws DeviceCommunicationException {
        try {
            int response = readRegister(STATUS);
            return new Status(response);
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Reads if the device has reached the home state
     *
     * @return the code of the home state
     */
    public int home_state() throws DeviceCommunicationException {
        try {
            return readRegister(FEEDBACK_INITIALIZATION_GRIP_STATE);
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Reads from the Status register if the device has initialized
     *
     * @return true if the device has initialized, false otherwise
     */
    public boolean hasInitialized() throws DeviceCommunicationException {
        try {
            Status status = getStatus();
            return status.is_back_home();
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Waits until the device has reached the target position
     */
    public synchronized void waitReachedPosition() throws DeviceCommunicationException {
        try {
            while (true) {
                if (has_fault.get()) {
                    throw new DeviceCommunicationException("Device has fault");
                }
                if (is_arrived()) return;
                Thread.sleep(100);
            }
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Initializes the device
     *
     * @return a Future object that will return true when the device has initialized
     */
    public Future<Boolean> initialize() {
        return executorService.submit(() -> {
            try {
                logger.log("[SAC_N] Initializing device");
                setEnabled(false);
                clearErrors();
                setEnabled(true);
                writeRegister(INITIALIZATION, 1);
                int i = 0;
                while (!hasInitialized()) {
                    Thread.sleep(300);
                    if (i > 20) throw new DeviceCommunicationException("Initialization failed");
                    i++;
                }
                is_initialized.set(true);
                target_position.set(0);
                current_position.set(0);
                stateFunction.notifyStateChange();
                setupErrorListener();
            } catch (Exception e) {
                logger.error("[SAC_N] Error initializing device: " + e.getMessage());
                return false;
            }
            return true;
        });
    }

    /**
     * Moves the device to the target position
     *
     * @param position the target position
     */
    public void move_absolute(int position) throws DeviceCommunicationException {
        try {
            writeRegister(TARGET_POSITION, position);
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Moves the device to the relative position
     *
     * @param position the relative position
     */
    public void move_relative(int position) throws DeviceCommunicationException {
        try {
            writeRegister(RELATIVE_POSITION, position);
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }


    public void clearErrors() throws DeviceCommunicationException {
        try {
            writeRegister(CLEAR_FAULT, 1);
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Moves the device to the target position and waits until the device has reached the target position
     *
     * @param position the target position
     * @return a Future object that will return true when the device has reached the target position
     */
    public Future<Boolean> move_absoluteAndWait(int position) {
        return executorService.submit(() -> {
            try {
                logger.log("[SAC_N] Moving to position: " + position);
                target_position.set(position);
                is_moving.set(true);
                stateFunction.notifyStateChange();
                move_absolute(position);
                waitReachedPosition();
                current_position.set(position);
                is_moving.set(false);
                stateFunction.notifyStateChange();
                return true;
            } catch (Exception e) {
                logger.error("[SAC_N] Error moving to position: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Moves the device to the relative position and waits until the device has reached the target position
     *
     * @param position the relative position
     * @return a Future object that will return true when the device has reached the target position
     */
    public Future<Boolean> move_relativeAndWait(int position) {
        return executorService.submit(() -> {
            try {
                logger.log("[SAC_N] Moving to relative position: " + position);
                move_relative(position);
                waitReachedPosition();
                return true;
            } catch (Exception e) {
                logger.error("[SAC_N] Error moving to relative position: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Get motor errors
     *
     * @return ErrorFlag class
     */
    public ErrorFlags getErrors() throws DeviceCommunicationException {
        int response = readRegister(FEEDBACK_ERROR_CODE);
        return new ErrorFlags(response);
    }
}
