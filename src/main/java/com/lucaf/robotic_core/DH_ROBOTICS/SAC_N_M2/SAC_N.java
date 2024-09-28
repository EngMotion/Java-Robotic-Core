package com.lucaf.robotic_core.DH_ROBOTICS.SAC_N_M2;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.lucaf.robotic_core.State;
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

        if (state.containsKey("current_position")) current_position.set(( (Double) state.get("current_position")).intValue());
        state.put("current_position", current_position);

        if (state.containsKey("target_position")) target_position.set(( (Double) state.get("target_position")).intValue());
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
    private synchronized int readRegister(byte[] register) {
        try {
            int startRegister = register[0] << 8 | register[1];
            Register[] regs = rs485.readMultipleRegisters(id, startRegister, 1);
            if (regs != null) {
                return regs[0].getValue();
            }
            return -1;
        } catch (ModbusException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Internal method that writes a register
     *
     * @param register the register to write
     * @param data     the data to write
     * @return true if the write is successful, false otherwise
     */
    private synchronized boolean writeRegister(byte[] register, int data) {
        try {
            int startRegister = register[0] << 8 | register[1];
            rs485.writeSingleRegister(id, startRegister, new SimpleInputRegister(data));
            return true;
        } catch (ModbusException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sets the speed of the device
     *
     * @param speed the speed to write
     */
    public void setSpeed(int speed) {
        writeRegister(SPEED, speed);
    }

    /**
     * Sets the acceleration of the device
     *
     * @param velocity the acceleration to write
     */
    public void setAcceleration(int velocity) {
        writeRegister(ACCELERATION, velocity);
    }

    /**
     * Sets the device as enabled or disabled
     *
     * @param enabled true if the device is enabled, false otherwise
     */
    public void setEnabled(boolean enabled) {
        writeRegister(START, enabled ? 1 : 0);
    }

    /**
     * Reads if the device has reached the target position
     *
     * @return true if the device has reached the target position, false otherwise
     */
    public boolean is_arrived() {
        int response = readRegister(FEEDBACK_MOTION_STATE);
        if (response != -1) {
            int data = response;
            return data == 1;
        }
        return false;
    }

    /**
     * Reads the current status of the device
     *
     * @return Status object with the current status of the device
     */
    public Status getStatus() {
        int response = readRegister(STATUS);
        if (response != -1) {
            return new Status(response);
        }
        return null;
    }

    /**
     * Reads if the device has reached the home state
     *
     * @return the code of the home state
     */
    public int home_state() {
        int response = readRegister(FEEDBACK_INITIALIZATION_GRIP_STATE);
        if (response != -1) {
            return response;
        }
        return -1;
    }

    /**
     * Reads from the Status register if the device has initialized
     *
     * @return true if the device has initialized, false otherwise
     */
    public boolean hasInitialized() {
        Status status = getStatus();
        if (status != null) {
            return status.is_back_home();
        }
        return false;
    }

    /**
     * Waits until the device has reached the target position
     */
    public synchronized void waitReachedPosition() {
        while (true) {
            if (is_arrived()) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initializes the device
     */
    public void initialize() {
        try {
            //if (!hasInitialized()) {
                setEnabled(false);
                clearErrors();
                setEnabled(true);
                writeRegister(INITIALIZATION, 1);
                while (!hasInitialized()) {
                    Thread.sleep(300);
                }
            //}
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            is_initialized.set(true);
            /*if (target_position.get() != 0 ){
                if (Main.DEVELOPER_MODE) System.out.println("Moving to target position: " + target_position.get());
                Future<Boolean> task = move_absoluteAndWait(target_position.get());
                try {
                    task.get();
                } catch (Exception e) {
                    if (Main.DEVELOPER_MODE) e.printStackTrace();
                }
            }*/
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
    public void move_absolute(int position) {
        writeRegister(TARGET_POSITION, position);
    }

    /**
     * Moves the device to the relative position
     *
     * @param position the relative position
     */
    public void move_relative(int position) {
        writeRegister(RELATIVE_POSITION, position);
    }


    public void clearErrors() {
        writeRegister(CLEAR_FAULT, 1);
    }

    /**
     * Moves the device to the target position and waits until the device has reached the target position
     *
     * @param position the target position
     * @return a Future object that will return true when the device has reached the target position
     */
    public Future<Boolean> move_absoluteAndWait(int position) {
        return executorService.submit(() -> {
            target_position.set(position);
            is_moving.set(true);
            stateFunction.notifyStateChange();
            move_absolute(position);
            waitReachedPosition();
            current_position.set(position);
            is_moving.set(false);
            stateFunction.notifyStateChange();
            return true;
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
            move_relative(position);
            waitReachedPosition();
            return true;
        });
    }
}
