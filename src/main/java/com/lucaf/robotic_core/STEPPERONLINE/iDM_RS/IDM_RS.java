package com.lucaf.robotic_core.STEPPERONLINE.iDM_RS;

import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.lucaf.robotic_core.COMMON.ModbusRTUDevice;
import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.State;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.lucaf.robotic_core.STEPPERONLINE.iDM_RS.Constants.*;

/**
 * Class that represents the iDM_RS StepperOnline device
 */
public class IDM_RS extends ModbusRTUDevice {

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
     *
     * @param rs485             the Modbus master connection
     * @param id                the id of the device
     * @param state             the state of the device
     * @param notifyStateChange the state class with the onStateChange method
     * @param logger            the logger
     */
    public IDM_RS(ModbusSerialMaster rs485, byte id, HashMap<String, Object> state, State notifyStateChange, Logger logger) {
        super( "iDM_RS", rs485, logger);
        this.state = state;
        this.stateFunction = notifyStateChange;
        setId(id);
        initState();
    }

    /**
     * Method to set the ramp mode. Refer to the datasheet for more information
     *
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
    private void initState() {
        if (state.containsKey("current_position")) {
            if (state.get("current_position") instanceof AtomicInteger) {
                currentPos = (AtomicLong) state.get("current_position");
            } else if (state.get("current_position") instanceof Integer) {
                currentPos.set((Integer) state.get("current_position"));
            } else if (state.get("current_position") instanceof Double) {
                currentPos.set(((Double) state.get("current_position")).intValue());
            } else if (state.get("current_position") instanceof Long) {
                currentPos.set((Long) state.get("current_position"));
            }
        }
        state.put("current_position", currentPos);

        if (state.containsKey("target_position")) {
            if (state.get("target_position") instanceof AtomicInteger) {
                targetPos = (AtomicLong) state.get("target_position");
            } else if (state.get("target_position") instanceof Integer) {
                targetPos.set((Integer) state.get("target_position"));
            } else if (state.get("target_position") instanceof Double) {
                targetPos.set(((Double) state.get("target_position")).intValue());
            } else if (state.get("target_position") instanceof Long) {
                targetPos.set((Long) state.get("target_position"));
            }
        }
        state.put("target_position", targetPos);
        state.put("is_moving", isMoving);
        state.put("initialized", initialized);
        stateFunction.notifyStateChange();
    }

    /**
     * Method that sets the speed of the device
     *
     * @param speed the speed to set
     * @throws DeviceCommunicationException if there is an error setting the speed
     */
    public void setSpeed(int speed) throws DeviceCommunicationException {
        writeRegister(VELOCITY, speed);
        if (controlMode.getCONTROL_MODE() == 2) {
            if (speed == 0) isMoving.set(false);
            else isMoving.set(true);
            stateFunction.notifyStateChange();
            writeRegister(STATUS_MODE, StatusMode.getSegmentPositioning((byte) 0x00));
        }

    }

    /**
     * Method that gets the speed of the device
     *
     * @return the speed of the device
     * @throws DeviceCommunicationException if there is an error getting the speed
     */
    public int getSpeed() throws DeviceCommunicationException {
        return readRegister(VELOCITY);
    }

    /**
     * Method that emergency stops the device
     *
     * @throws DeviceCommunicationException
     */
    public void stop() throws DeviceCommunicationException {
        logger.log("[iDM_RS] Emergency stop");
        isMoving.set(false);
        writeRegister(STATUS_MODE, StatusMode.getEMERGENCY_STOP());
        stateFunction.notifyStateChange();
    }

    /**
     * Method that sets the acceleration of the device
     *
     * @param acceleration the acceleration to set
     * @throws DeviceCommunicationException if there is an error setting the acceleration
     */
    public void setAcceleration(int acceleration) throws DeviceCommunicationException {
        writeRegister(ACCELERATION, acceleration);
    }

    /**
     * Method that gets the acceleration of the device
     *
     * @return the acceleration of the device
     * @throws DeviceCommunicationException if there is an error getting the acceleration
     */
    public int getAcceleration() throws DeviceCommunicationException {
        return readRegister(ACCELERATION);
    }

    /**
     * Method that sets the deceleration of the device
     *
     * @param deceleration the deceleration to set
     * @throws DeviceCommunicationException if there is an error setting the deceleration
     */
    public void setDeceleration(int deceleration) throws DeviceCommunicationException {
        writeRegister(DECELERATION, deceleration);
    }

    /**
     * Method that gets the deceleration of the device
     *
     * @return the deceleration of the device
     * @throws DeviceCommunicationException if there is an error getting the deceleration
     */
    public int getDeceleration() throws DeviceCommunicationException {
        return readRegister(DECELERATION);
    }

    /**
     * Method that moves the device to a position
     *
     * @param position the position to move to
     * @throws DeviceCommunicationException if there is an error moving the device
     */
    public void setPosition(long position) throws DeviceCommunicationException {
        if (controlMode.isRELATIVE_POSITIONING()) {
            targetPos.set(targetPos.get() + position);
        } else {
            targetPos.set(position);
        }
        stateFunction.notifyStateChange();
        writeLongRegister(TARGET_POSITION_HIGH, position, false);
        writeRegister(STATUS_MODE, StatusMode.getSegmentPositioning((byte) 0x00));
    }

    /**
     * Method that sets the control mode
     *
     * @throws DeviceCommunicationException if there is an error setting the control mode
     */
    public void writeControlMode() throws DeviceCommunicationException {
        writeRegister(CONTROL_MODE, controlMode.toInt());
    }

    /**
     * Method that sets mode to positioning
     *
     * @throws DeviceCommunicationException if there is an error setting the mode
     */
    public void setPositioningMode() throws DeviceCommunicationException {
        controlMode.setCONTROL_MODE(1);
        writeControlMode();
    }

    /**
     * Method that sets mode to velocity
     *
     * @throws DeviceCommunicationException if there is an error setting the mode
     */
    public void setVelocityMode() throws DeviceCommunicationException {
        controlMode.setCONTROL_MODE(2);
        writeControlMode();
    }

    /**
     * Methos that gets the status of the motor
     *
     * @return StatusMode class with the status of the motor
     * @throws DeviceCommunicationException if there is an error getting the status
     */
    public StatusMode getStatusMode() throws DeviceCommunicationException {
        return new StatusMode(readRegister(STATUS_MODE));
    }

    /**
     * Method that enables the relative positioning
     *
     * @param relative if relative positioning
     * @throws DeviceCommunicationException if there is an error setting the relative positioning
     */
    public void setRelativePositioning(boolean relative) throws DeviceCommunicationException {
        controlMode.setRELATIVE_POSITIONING(relative);
        writeControlMode();
    }

    /**
     * Method that gets the digital input statuses
     *
     * @return DigitalInputs class with the digital input statuses
     * @throws DeviceCommunicationException if there is an error getting the digital input statuses
     */
    public DigitalInputs getDigitalInputs() throws DeviceCommunicationException {
        return new DigitalInputs(readRegister(DIGITAL_INPUTS_STATUS));
    }

    /**
     * Method that sets the digital input statuses
     *
     * @param digitalInputs the digital input statuses to set
     * @throws DeviceCommunicationException if there is an error setting the digital input statuses
     */
    public void setDigitalInputs(DigitalInputs digitalInputs) throws DeviceCommunicationException {
        writeRegister(DIGITAL_INPUTS_STATUS, digitalInputs.toInt());
    }

    /**
     * Method that gets the digital output statuses
     *
     * @return DigitalOutputs class with the digital output statuses
     * @throws DeviceCommunicationException if there is an error getting the digital output statuses
     */
    public DigitalOutputs getDigitalOutputs() throws DeviceCommunicationException {
        return new DigitalOutputs(readRegister(DIGITAL_OUTPUTS_STATUS));
    }

    /**
     * Method that sets the digital output statuses
     *
     * @param digitalOutputs the digital output statuses to set
     * @throws DeviceCommunicationException if there is an error setting the digital output statuses
     */
    public void setDigitalOutputs(DigitalOutputs digitalOutputs) throws DeviceCommunicationException {
        writeRegister(DIGITAL_OUTPUTS_STATUS, digitalOutputs.toInt());
    }

    /**
     * Method that gets the digital input
     *
     * @param input the digital input number
     * @return DigitalInput class with the digital input settings
     * @throws DeviceCommunicationException if there is an error getting the digital input
     */
    public DigitalInput getDigitalInput(int input) throws DeviceCommunicationException {
        if (input <= 0 || input > 7) throw new RuntimeException("Invalid digital input number");
        switch (input) {
            case 1 -> {
                return new DigitalInput(readRegister(DI1));
            }
            case 2 -> {
                return new DigitalInput(readRegister(DI2));
            }
            case 3 -> {
                return new DigitalInput(readRegister(DI3));
            }
            case 4 -> {
                return new DigitalInput(readRegister(DI4));
            }
            case 5 -> {
                return new DigitalInput(readRegister(DI5));
            }
            case 6 -> {
                return new DigitalInput(readRegister(DI6));
            }
            case 7 -> {
                return new DigitalInput(readRegister(DI7));
            }
        }
        return null;
    }

    /**
     * Method that sets the digital input
     *
     * @param input        the digital input number
     * @param digitalInput the digital input settings to set
     * @throws DeviceCommunicationException if there is an error setting the digital input
     */
    public void setDigitalInput(int input, DigitalInput digitalInput) throws DeviceCommunicationException {
        if (input <= 0 || input > 7) throw new RuntimeException("Invalid digital input number");
        switch (input) {
            case 1 -> {
                writeRegister(DI1, digitalInput.toInt());
            }
            case 2 -> {
                writeRegister(DI2, digitalInput.toInt());
            }
            case 3 -> {
                writeRegister(DI3, digitalInput.toInt());
            }
            case 4 -> {
                writeRegister(DI4, digitalInput.toInt());
            }
            case 5 -> {
                writeRegister(DI5, digitalInput.toInt());
            }
            case 6 -> {
                writeRegister(DI6, digitalInput.toInt());
            }
            case 7 -> {
                writeRegister(DI7, digitalInput.toInt());
            }
        }
    }

    /**
     * Method that gets the digital output
     *
     * @param output the digital output number
     * @return DigitalOutput class with the digital output settings
     * @throws DeviceCommunicationException if there is an error getting the digital output
     */
    public DigitalOutput getDigitalOutput(int output) throws DeviceCommunicationException {
        if (output <= 0 || output > 3) throw new RuntimeException("Invalid digital output number");
        switch (output) {
            case 1 -> {
                return new DigitalOutput(readRegister(DO1));
            }
            case 2 -> {
                return new DigitalOutput(readRegister(DO2));
            }
            case 3 -> {
                return new DigitalOutput(readRegister(DO3));
            }
        }
        return null;
    }

    /**
     * Method that sets the digital output
     *
     * @param output        the digital output number
     * @param digitalOutput the digital output settings to set
     * @throws DeviceCommunicationException if there is an error setting the digital output
     */
    public void setDigitalOutput(int output, DigitalOutput digitalOutput) throws DeviceCommunicationException {
        if (output <= 0 || output > 3) throw new RuntimeException("Invalid digital output number");
        switch (output) {
            case 1 -> {
                writeRegister(DO1, digitalOutput.toInt());
            }
            case 2 -> {
                writeRegister(DO2, digitalOutput.toInt());
            }
            case 3 -> {
                writeRegister(DO3, digitalOutput.toInt());
            }
        }
    }

    /**
     * Method that sets the homing speed
     *
     * @param speed the speed to set
     * @throws DeviceCommunicationException if there is an error setting the homing speed
     */
    public void setHomingSpeed(long speed) throws DeviceCommunicationException {
        writeLongRegister(HOMING_SPEED_HIGH, speed, false);
    }

    /**
     * Method that gets the homing speed
     *
     * @return the homing speed
     * @throws DeviceCommunicationException if there is an error getting the homing speed
     */
    public long getHomingSpeed() throws DeviceCommunicationException {
        return readLongRegister(HOMING_SPEED_HIGH, false);
    }

    /**
     * Method that sets the homing acceleration
     *
     * @param acceleration the acceleration to set
     * @throws DeviceCommunicationException if there is an error setting the homing acceleration
     */
    public void setHomingAcceleration(int acceleration) throws DeviceCommunicationException {
        writeRegister(HOMING_ACCELERATION, acceleration);
    }

    /**
     * Method that gets the homing acceleration
     *
     * @return the homing acceleration
     * @throws DeviceCommunicationException if there is an error getting the homing acceleration
     */
    public int getHomingAcceleration() throws DeviceCommunicationException {
        return readRegister(HOMING_ACCELERATION);
    }

    /**
     * Method that sets the homing deceleration
     *
     * @param deceleration the deceleration to set
     * @throws DeviceCommunicationException if there is an error setting the homing deceleration
     */
    public void setHomingDeceleration(int deceleration) throws DeviceCommunicationException {
        writeRegister(HOMING_DECELERATION, deceleration);
    }

    /**
     * Method that gets the homing deceleration
     *
     * @return the homing deceleration
     * @throws DeviceCommunicationException if there is an error getting the homing deceleration
     */
    public int getHomingDeceleration() throws DeviceCommunicationException {
        return readRegister(HOMING_DECELERATION);
    }

    /**
     * Method that sets the homing stop position
     *
     * @param position the position to set
     * @throws DeviceCommunicationException if there is an error setting the homing stop position
     */
    public void setHomingStopPosition(long position) throws DeviceCommunicationException {
        writeLongRegister(HOMING_STOP_POSITION_HIGH, position, false);
    }

    /**
     * Method that gets the homing stop position
     *
     * @return the homing stop position
     * @throws DeviceCommunicationException if there is an error getting the homing stop position
     */
    public long getHomingStopPosition() throws DeviceCommunicationException {
        return readLongRegister(HOMING_STOP_POSITION_HIGH, false);
    }

    /**
     * Method that starts the homing
     *
     * @param homingControl the homing method to set
     * @throws DeviceCommunicationException if there is an error setting the homing method
     */
    public Future<Boolean> homing(HomingControl homingControl) {
        return executorService.submit(() -> {
            try{
                logger.log("[iDM_RS] Starting homing");
                writeRegister(HOMING_METHOD, homingControl.toInt());
                writeRegister(STATUS_MODE, StatusMode.HOMING);
                isMoving.set(true);
                while (true) {
                    if (!isMoving.get()) throw new DeviceCommunicationException("Device stopped");
                    StatusMode mode = getStatusMode();
                    if (mode.getSTATUS_CODE() == 0) break;
                    Thread.sleep(50);
                }
                isMoving.set(false);
                return true;
            }catch (Exception e){
                isMoving.set(false);
                logger.error("[iDM_RS] Error starting homing");
                logger.error(e.getMessage());
                return false;
            }
        });
    }

    /**
     * Method that gets the homing method
     *
     * @return the homing method
     * @throws DeviceCommunicationException if there is an error getting the homing method
     */
    public HomingControl getHomingMethod() throws DeviceCommunicationException {
        return new HomingControl(readRegister(HOMING_METHOD));
    }

    /**
     * Method that waits for the device to reach the position
     *
     * @throws DeviceCommunicationException if there is an error waiting for the position
     */
    @SneakyThrows
    public void waitReachedPosition() throws DeviceCommunicationException {
        while (true) {
            if (!isMoving.get()) throw new DeviceCommunicationException("Device stopped");
            StatusMode mode = getStatusMode();
            if (!mode.isRunning()) break;
            Thread.sleep(50);
        }
    }

    /**
     * Method that moves the device to a relative position and waits for the movement to end
     *
     * @param position the position to move to
     * @return a future with the result of the operation
     */
    public Future<Boolean> moveToPositionAndWait(int position) {
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
            } catch (Exception e) {
                logger.error("[iDM_RS] Error moving to position " + position);
                return false;
            }
        });
    }
}
