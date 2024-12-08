package com.lucaf.robotic_core.STEPPERONLINE.iDM_RS;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.lucaf.robotic_core.Logger;
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
     * Global Logger
     */
    final Logger logger;

    /**
     * Constructor of the class
     * @param rs485 the Modbus master connection
     * @param id the id of the device
     * @param state the state of the device
     * @param notifyStateChange the state class with the onStateChange method
     * @param logger the logger
     */
    public iDM_RS(ModbusSerialMaster rs485, byte id, HashMap<String, Object> state, State notifyStateChange, Logger logger) {
        this.rs485 = rs485;
        this.state = state;
        this.stateFunction = notifyStateChange;
        this.id = id;
        this.logger = logger;
        initState();
    }

    /**
     * Method to set the ramp mode. Refer to the datasheet for more information
     * @param rampMode the ramp mode number
     */
    public void setRampMode(byte rampMode) {
        logger.debug("[iDM_RS] Setting ramp mode to " + rampMode);
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
            logger.debug("[iDM_RS] Reading register " + register[0] + " " + register[1]);
            int startRegister = register[0] << 8 | register[1];
            Register[] regs = rs485.readMultipleRegisters(id, startRegister, 1);
            if (regs != null) {
                logger.debug("[iDM_RS] Read register " + register[0] + " " + register[1] + " value " + regs[0].getValue());
                return regs[0].getValue();
            }
            return -1;
        } catch (ModbusException e) {
            logger.error("[iDM_RS] Error reading register " + register[0] + " " + register[1]);
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
            logger.debug("[iDM_RS] Writing register " + register[0] + " " + register[1] + " value " + data);
            int startRegister = register[0] << 8 | register[1];
            rs485.writeSingleRegister(id, startRegister, new SimpleInputRegister(data));
            return true;
        } catch (ModbusException e) {
            logger.error("[iDM_RS] Error writing register " + register[0] + " " + register[1] + " value " + data);
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
        writeRegister(VELOCITY, speed);
        if (controlMode.getCONTROL_MODE()==2){
            if (speed==0) isMoving.set(false);
            else isMoving.set(true);
            stateFunction.notifyStateChange();
            writeRegister(STATUS_MODE, StatusMode.getSegmentPositioning((byte) 0x00));
        }

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
        logger.log("[iDM_RS] Emergency stop");
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
     * Method that sets the homing speed
     * @param speed the speed to set
     * @throws DeviceCommunicationException if there is an error setting the homing speed
     */
    public void setHomingSpeed(int speed) throws DeviceCommunicationException {
        int speed_high = (int) (speed >> 16);
        int speed_low = (int) (speed & 0xFFFF);
        writeRegister(HOMING_SPEED_HIGH, speed_high);
        writeRegister(HOMING_SPEED_LOW, speed_low);
    }

    /**
     * Method that gets the homing speed
     * @return the homing speed
     * @throws DeviceCommunicationException if there is an error getting the homing speed
     */
    public int getHomingSpeed() throws DeviceCommunicationException {
        int speed_high = readRegister(HOMING_SPEED_HIGH);
        int speed_low = readRegister(HOMING_SPEED_LOW);
        return (speed_high << 16) | speed_low;
    }

    /**
     * Method that sets the homing acceleration
     * @param acceleration the acceleration to set
     * @throws DeviceCommunicationException if there is an error setting the homing acceleration
     */
    public void setHomingAcceleration(int acceleration) throws DeviceCommunicationException {
        writeRegister(HOMING_ACCELERATION, acceleration);
    }

    /**
     * Method that gets the homing acceleration
     * @return the homing acceleration
     * @throws DeviceCommunicationException if there is an error getting the homing acceleration
     */
    public int getHomingAcceleration() throws DeviceCommunicationException {
        return readRegister(HOMING_ACCELERATION);
    }

    /**
     * Method that sets the homing deceleration
     * @param deceleration the deceleration to set
     * @throws DeviceCommunicationException if there is an error setting the homing deceleration
     */
    public void setHomingDeceleration(int deceleration) throws DeviceCommunicationException {
        writeRegister(HOMING_DECELERATION, deceleration);
    }

    /**
     * Method that gets the homing deceleration
     * @return the homing deceleration
     * @throws DeviceCommunicationException if there is an error getting the homing deceleration
     */
    public int getHomingDeceleration() throws DeviceCommunicationException {
        return readRegister(HOMING_DECELERATION);
    }

    /**
     * Method that starts the homing
     * @param homingControl the homing method to set
     * @throws DeviceCommunicationException if there is an error setting the homing method
     */
    @SneakyThrows
    public void homing(HomingControl homingControl) {
        logger.log("[iDM_RS] Starting homing");
        writeRegister(HOMING_METHOD, homingControl.toInt());
        writeRegister(STATUS_MODE, StatusMode.HOMING);
        while (true){
            StatusMode mode = getStatusMode();
            if (mode.getSTATUS_CODE() == 0) break;
            Thread.sleep(50);
        }
    }

    /**
     * Method that gets the homing method
     * @return the homing method
     * @throws DeviceCommunicationException if there is an error getting the homing method
     */
    public HomingControl getHomingMethod() throws DeviceCommunicationException {
        return new HomingControl(readRegister(HOMING_METHOD));
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
            logger.log("[iDM_RS] Moving to position " + position);
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
                logger.error("[iDM_RS] Error moving to position " + position);
                return false;
            }
        });
    }


}
