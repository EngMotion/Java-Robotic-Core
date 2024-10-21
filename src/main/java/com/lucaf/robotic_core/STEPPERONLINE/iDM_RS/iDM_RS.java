package com.lucaf.robotic_core.STEPPERONLINE.iDM_RS;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.lucaf.robotic_core.State;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static com.lucaf.robotic_core.STEPPERONLINE.iDM_RS.Constants.*;

/**
 * Class that represents the iDM_RS StepperOnline device
 */
public class iDM_RS {

    /**
     * The Modbus master connection
     */
    final ModbusSerialMaster rs485;

    /**
     * The state of the device
     */
    final HashMap<String, Object> state;

    /**
     * The control mode of the device
     */
    final ControlMode controlMode = new ControlMode(0x00);

    /**
     * The state class
     */
    final State stateFunction;

    /**
     * The id of the device
     */
    final byte id;

    /**
     * The ramp acceleration mode
     */
    byte rampMode = 0x0;

    /**
     * The executor service for async operations
     */
    final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Last verified position
     */
    AtomicLong currentPos = new AtomicLong(0);

    /**
     * Target position sent to the device
     */
    AtomicLong targetPos = new AtomicLong(0);

    /**
     * The device is initialized
     */
    AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * The device is moving
     */
    AtomicBoolean isMoving = new AtomicBoolean(false);

    /**
     * Constructor of the class
     * @param rs485 the Modbus master connection
     * @param id the id of the device
     * @param state the state of the device
     * @param notifyStateChange the state class with the onStateChange method
     */
    public iDM_RS(ModbusSerialMaster rs485, byte id, HashMap<String, Object> state, State notifyStateChange){
        this.rs485 = rs485;
        this.state = state;
        this.stateFunction = notifyStateChange;
        this.id = id;
        initState();
    }

    /**
     * Method to set the ramp mode. Refer to the datasheet for more information
     * @param rampMode the ramp mode number
     */
    public void setRampMode(byte rampMode) {
        this.rampMode = rampMode;
        state.put("ramp_mode", rampMode);
        stateFunction.notifyStateChange();
    }

    /**
     * Method that initializes the device state
     */
    private void initState(){
        state.put("current_position", currentPos);
        state.put("target_position", targetPos);
        state.put("is_moving", isMoving);
        state.put("initialized", initialized);
        stateFunction.notifyStateChange();
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
     * Method that sets the speed of the device
     * @param speed the speed to set
     * @throws DeviceCommunicationException if there is an error setting the speed
     */
    public void setSpeed(int speed) throws DeviceCommunicationException {
        if (speed < 0) speed = 0;
        if (controlMode.getCONTROL_MODE()==2){
            if (speed==0) isMoving.set(false);
            else isMoving.set(true);
            stateFunction.notifyStateChange();
        }
        writeRegister(VELOCITY, speed);
    }

    /**
     * Method that gets the speed of the device
     * @return the speed of the device
     * @throws DeviceCommunicationException if there is an error getting the speed
     */
    public int getSpeed() throws DeviceCommunicationException {
        return readRegister(VELOCITY);
    }

    /**
     * Method that emergency stops the device
     * @throws DeviceCommunicationException
     */
    public void stop() throws DeviceCommunicationException {
        writeRegister(STATUS_MODE,StatusMode.getEMERGENCY_STOP());
        isMoving.set(false);
        stateFunction.notifyStateChange();
    }

    /**
     * Method that sets the acceleration of the device
     * @param acceleration the acceleration to set
     * @throws DeviceCommunicationException if there is an error setting the acceleration
     */
    public void setAcceleration(int acceleration) throws DeviceCommunicationException {
        writeRegister(ACCELERATION, acceleration);
    }

    /**
     * Method that gets the acceleration of the device
     * @return the acceleration of the device
     * @throws DeviceCommunicationException if there is an error getting the acceleration
     */
    public int getAcceleration() throws DeviceCommunicationException {
        return readRegister(ACCELERATION);
    }

    /**
     * Method that sets the deceleration of the device
     * @param deceleration the deceleration to set
     * @throws DeviceCommunicationException if there is an error setting the deceleration
     */
    public void setDeceleration(int deceleration) throws DeviceCommunicationException {
        writeRegister(DECELERATION, deceleration);
    }

    /**
     * Method that gets the deceleration of the device
     * @return the deceleration of the device
     * @throws DeviceCommunicationException if there is an error getting the deceleration
     */
    public int getDeceleration() throws DeviceCommunicationException {
        return readRegister(DECELERATION);
    }

    /**
     * Method that moves the device to a position
     * @param position the position to move to
     * @throws DeviceCommunicationException if there is an error moving the device
     */
    public void setPosition(long position) throws DeviceCommunicationException {
        if (controlMode.isRELATIVE_POSITIONING()){
            targetPos.set(targetPos.get() + position);
        }else{
            targetPos.set(position);
        }
        stateFunction.notifyStateChange();
        int position_high = (int) (position >> 16);
        int position_low = (int) (position & 0xFFFF);
        writeRegister(TARGET_POSITION_HIGH, position_high);
        writeRegister(TARGET_POSITION_LOW, position_low);
        writeRegister(STATUS_MODE, StatusMode.getSegmentPositioning((byte) 0x00));
    }

    /**
     * Method that sets the control mode
     * @throws DeviceCommunicationException if there is an error setting the control mode
     */
    public void writeControlMode() throws DeviceCommunicationException {
        writeRegister(CONTROL_MODE, controlMode.toInt());
    }

    /**
     * Method that sets mode to positioning
     * @throws DeviceCommunicationException if there is an error setting the mode
     */
    public void setPositioningMode() throws DeviceCommunicationException {
        controlMode.setCONTROL_MODE(1);
        writeControlMode();
    }

    /**
     * Method that sets mode to velocity
     * @throws DeviceCommunicationException if there is an error setting the mode
     */
    public void setVelocityMode() throws DeviceCommunicationException {
        controlMode.setCONTROL_MODE(2);
        writeControlMode();
    }

    /**
     * Methos that gets the status of the motor
     * @return StatusMode class with the status of the motor
     * @throws DeviceCommunicationException if there is an error getting the status
     */
    public StatusMode getStatusMode() throws DeviceCommunicationException {
        return new StatusMode(readRegister(STATUS_MODE));
    }

    /**
     * Method that enables the relative positioning
     * @param relative if relative positioning
     * @throws DeviceCommunicationException if there is an error setting the relative positioning
     */
    public void setRelativePositioning(boolean relative) throws DeviceCommunicationException {
        controlMode.setRELATIVE_POSITIONING(relative);
        writeControlMode();
    }

    /**
     * Method that waits for the device to reach the position
     * @throws DeviceCommunicationException if there is an error waiting for the position
     */
    @SneakyThrows
    public void waitReachedPosition() throws DeviceCommunicationException {
        while (true){
            StatusMode mode = getStatusMode();
            if (!mode.isRunning()) break;
            Thread.sleep(50);
        }
    }

    /**
     * Method that moves the device to a relative position and waits for the movement to end
     * @param position the position to move to
     * @return a future with the result of the operation
     */
    public Future<Boolean> moveToPositionAndWait(int position){
        return executorService.submit(() -> {
            try {
                isMoving.set(true);
                setPosition(position);
                stateFunction.notifyStateChange();
                waitReachedPosition();
                isMoving.set(false);
                currentPos.set(targetPos.get());
                stateFunction.notifyStateChange();
                return true;
            }catch (Exception e){
                return false;
            }
        });
    }
}
