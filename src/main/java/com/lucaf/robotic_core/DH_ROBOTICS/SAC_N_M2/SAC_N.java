package com.lucaf.robotic_core.DH_ROBOTICS.SAC_N_M2;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lucaf.robotic_core.DH_ROBOTICS.SAC_N_M2.Constants.*;

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
     * Initializes the state
     */
    void initState() {
        state.put("is_moving", is_moving);
        state.put("is_initialized", is_initialized);

        if (state.containsKey("current_position"))
            current_position.set(((Double) state.get("current_position")).intValue());
        state.put("current_position", current_position);

        if (state.containsKey("target_position"))
            target_position.set(((Double) state.get("target_position")).intValue());
        state.put("target_position", target_position);
    }

    /**
     * The executor service. It is used to run the move commands in a separate thread
     */
    private ExecutorService executorService = Executors.newFixedThreadPool(4);

    /**
     * The state class
     */
    private final State stateFunction;

    /**
     * Constructor that initializes the SAC_N object with an existing RS485 connection
     *
     * @param rs485 the RS485 connection object
     */
    public SAC_N(ModbusSerialMaster rs485, HashMap<String, Object> state, State notifyStateChange) {
        this.rs485 = rs485;
        this.state = state;
        this.stateFunction = notifyStateChange;
        if (stateFunction != null) initState();
    }

    /**
     * Constructor that initializes the SAC_N object with an existing RS485 connection
     *
     * @param rs485 the RS485 connection object
     * @param id    the address id of the device
     */
    public SAC_N(ModbusSerialMaster rs485, byte id, HashMap<String, Object> state, State notifyStateChange) {
        this.rs485 = rs485;
        setId(id);
        this.state = state;
        this.stateFunction = notifyStateChange;
        if (stateFunction != null) initState();
    }

    /**
     * Internal method that reads a register
     *
     * @param register the register to read
     * @return the response of the device
     */
    private synchronized int readRegister(byte[] register) throws DeviceCommunicationException {
        try {
            int startRegister = register[0] << 8 | register[1];
            Register[] regs = rs485.readMultipleRegisters(id, startRegister, 1);
            if (regs != null) {
                return regs[0].getValue();
            }
            return -1;
        } catch (ModbusException e) {
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
            int startRegister = register[0] << 8 | register[1];
            rs485.writeSingleRegister(id, startRegister, new SimpleInputRegister(data));
            return true;
        } catch (ModbusException e) {
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
            while (!is_arrived()) {
                Thread.sleep(100);
            }
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Initializes the device
     */
    public void initialize() throws DeviceCommunicationException {
        try {
            setEnabled(false);
            clearErrors();
            setEnabled(true);
            writeRegister(INITIALIZATION, 1);
            while (!hasInitialized()) {
                Thread.sleep(300);
            }
        } catch (Exception e) {
            throw new DeviceCommunicationException(e.getMessage());
        } finally {
            is_initialized.set(true);
            target_position.set(0);
            current_position.set(0);
            stateFunction.notifyStateChange();
        }
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
                move_relative(position);
                waitReachedPosition();
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }
}
