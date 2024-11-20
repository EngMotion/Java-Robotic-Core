package com.lucaf.robotic_core.NANOTEC.PD4E_RTU;

import com.lucaf.robotic_core.Pair;
import com.lucaf.robotic_core.State;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import com.nanotec.nanolib.DeviceHandle;
import com.nanotec.nanolib.OdIndex;
import com.nanotec.nanolib.helper.NanolibHelper;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lucaf.robotic_core.NANOTEC.PD4E_RTU.Constants.*;

/**
 * Class that represents the PD4E module of Nanotec
 */
public class PD4E {

    /**
     * The Nanolib helper
     */
    private final NanolibHelper nanolibHelper;

    /**
     * The device handle
     */
    private final DeviceHandle deviceHandle;

    /**
     * The state of the device
     */
    private final HashMap<String, Object> state;

    /**
     * The state class, for the onStateChange method
     */
    private final State stateFunction;

    /**
     * The operation mode of the device
     */
    private int operationMode = Constants.OperationMode.PROFILE_POSITION;

    /**
     * The address of the brake. The index of digital output to control the brake
     */
    private int brakeAddress = 0;

    /**
     * The control word of the device
     */
    private ControlWord operationControl = new ControlWord();

    /**
     * The position of the device
     */
    private int position = 0;

    /**
     * The executor service for the async operations
     */
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Executor service for the error handling
     */
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    /**
     * Current verified position
     */
    AtomicInteger currentPos = new AtomicInteger(0);

    /**
     * Last target position command
     */
    AtomicInteger targetPos = new AtomicInteger(0);

    /**
     * IsMoving flag
     */
    AtomicBoolean isMoving = new AtomicBoolean(false);

    /**
     * Initialized flag
     */
    AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * Error flag
     */
    AtomicBoolean has_fault = new AtomicBoolean(false);

    /**
     * Constructor of the class
     *
     * @param nanolibHelper Native methods helper
     * @param deviceHandle  The device handle
     * @param state         The state of the device
     * @param stateFunction The state class
     */
    public PD4E(NanolibHelper nanolibHelper, DeviceHandle deviceHandle, HashMap<String, Object> state, State stateFunction) {
        this.nanolibHelper = nanolibHelper;
        this.deviceHandle = deviceHandle;
        this.state = state;
        this.stateFunction = stateFunction;
        initState();
        setupErrorListener();
    }

    /**
     * Method that initializes the state of the device
     */
    private void initState() {
        state.put("current_position", currentPos);
        state.put("target_position", targetPos);
        state.put("is_moving", isMoving);
        state.put("initialized", initialized);
        state.put("has_fault", has_fault);
        state.put("fault","");
    }

    /**
     * Internal method that periodically checks for errors
     */
    private void setupErrorListener() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                ErrorFlags errorFlags = getErrors();
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
     * Method that writes a register to the device
     *
     * @param register The register to write
     * @param value    The value to write
     * @throws NanolibHelper.NanolibException if the register is not found or there is communication error
     */
    public void writeRegister(Pair<OdIndex, Integer> register, int value) throws NanolibHelper.NanolibException {
        nanolibHelper.writeNumber(deviceHandle, value, register.first, register.second);
    }

    /**
     * Method that reads a register from the device
     *
     * @param register The register to read
     * @return The value of the register
     * @throws NanolibHelper.NanolibException if the register is not found or there is communication error
     */
    private int readRegister(Pair<OdIndex, Integer> register) throws NanolibHelper.NanolibException {
        return (int) nanolibHelper.readNumber(deviceHandle, register.first);
    }

    /**
     * Method that reads the status word and cast it to the StatusWord helper class
     *
     * @return The status word
     * @throws DeviceCommunicationException if there is an error reading the status word
     */
    private StatusWord getStatusWord() throws DeviceCommunicationException {
        try {
            int statusWord = readRegister(STATUS_WORD);
            return new StatusWord(statusWord);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the brake address
     *
     * @param address The address of the brake. Between 1 and 4. Represents the DIO output to control the brake
     */
    public void setBrakeAddress(int address) {
        if (address <= 0 || address > 4) throw new RuntimeException("Address must be between 1 and 4");
        brakeAddress = address;
    }

    /**
     * Method that sets the brake status
     *
     * @param active true to activate the brake, false to deactivate
     * @throws DeviceCommunicationException if there is an error setting the brake status
     */
    public void setBrakeStatus(boolean active) throws DeviceCommunicationException {
        if (brakeAddress == 0) return;
        setDigitalOutput(brakeAddress, active);
    }

    /**
     * Method that sets the value of a digital output
     *
     * @param output The output to set. Between 1 and 4
     * @param value  The value of the output. True for high, false for low
     * @throws DeviceCommunicationException if there is an error setting the digital output
     */
    public void setDigitalOutput(int output, boolean value) throws DeviceCommunicationException {
        if (output < 1 || output > 4) throw new RuntimeException("Output must be between 1 and 4");
        try {
            DigitalOutputs digitalOutputs = new DigitalOutputs(readRegister(DIGITAL_OUTPUTS));
            switch (output) {
                case 1:
                    digitalOutputs.setOutput1(value);
                    break;
                case 2:
                    digitalOutputs.setOutput2(value);
                    break;
                case 3:
                    digitalOutputs.setOutput3(value);
                    break;
                case 4:
                    digitalOutputs.setOutput4(value);
                    break;
            }
            writeRegister(DIGITAL_OUTPUTS, digitalOutputs.toInt());
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the control word and waits for acknowledgment from device
     *
     * @param controlWord     The control word to set
     * @param statusWordCheck The status word check interface that determines when the acknowledgment is received
     * @throws NanolibHelper.NanolibException if there is an error setting the control word
     */
    @SneakyThrows
    private void setControlWordAndWaitForAck(ControlWord controlWord, StatusWordCheck statusWordCheck) throws NanolibHelper.NanolibException {
        writeRegister(CONTROL_WORD, controlWord.toInt());
        while (true) {
            StatusWord sw = getStatusWord();
            if (statusWordCheck.checkStatusWord(sw)) break;
            Thread.sleep(100);
        }
    }

    /**
     * Method that sets the homing speed of the device
     *
     * @param speed The speed of the homing in user defined units
     * @throws DeviceCommunicationException if there is an error setting the homing speed
     */
    public void setHomingSpeed(int speed) throws DeviceCommunicationException {
        try {
            writeRegister(HOMING_SPEED, speed);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the homing speed of the device
     *
     * @return The speed of the homing in user defined units
     * @throws DeviceCommunicationException if there is an error getting the homing speed
     */
    public int getHomingSpeed() throws DeviceCommunicationException {
        try {
            return readRegister(HOMING_SPEED);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the homing acceleration of the device
     *
     * @param acceleration The acceleration of the homing in user defined units
     * @throws DeviceCommunicationException if there is an error setting the homing acceleration
     */
    public void setHomingAcceleration(int acceleration) throws DeviceCommunicationException {
        try {
            writeRegister(HOMING_ACCELERATION, acceleration);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the homing acceleration of the device
     *
     * @return The acceleration of the homing in user defined units
     * @throws DeviceCommunicationException if there is an error getting the homing acceleration
     */
    public int getHomingAcceleration() throws DeviceCommunicationException {
        try {
            return readRegister(HOMING_ACCELERATION);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the homing method of the device
     *
     * @param homeMethod The homing method. Please refer to manual for the values
     * @throws DeviceCommunicationException if there is an error setting the homing method
     */
    public void home(int homeMethod) throws NanolibHelper.NanolibException, DeviceCommunicationException, InterruptedException {
        setBrakeStatus(false);
        InputSpecialFunction inputSpecialFunction = new InputSpecialFunction(readRegister(INPUT_SPECIAL_FUNCTION));
        inputSpecialFunction.setHomeSwitch(true);
        writeRegister(INPUT_SPECIAL_FUNCTION, inputSpecialFunction.toInt());
        writeRegister(HOME_METHOD, homeMethod);
        writeRegister(MODE_OF_OPERATION, OperationMode.HOMING);
        enable();
        this.operationControl.setOperationModeSpecific_4(true);
        setControlWordAndWaitForAck(this.operationControl, new StatusWordCheck() {
            @Override
            public boolean checkStatusWord(StatusWord statusWord) {
                return statusWord.targetReached;
            }
        });
        this.operationControl.setOperationModeSpecific_4(false);
        setBrakeStatus(true);
    }

    /**
     * Method that enables the device
     *
     * @throws NanolibHelper.NanolibException if there is an error enabling the device
     */
    public void enable() throws NanolibHelper.NanolibException {
        ControlWord controlWord = new ControlWord();
        controlWord.setQuickStop(true);
        controlWord.setEnableVoltage(true);
        setControlWordAndWaitForAck(controlWord, new StatusWordCheck() {
            @Override
            public boolean checkStatusWord(StatusWord statusWord) {
                return statusWord.getStateCode() == StatusWord.States.READY_TO_SWITCH_ON;
            }
        });
        controlWord.setSwitchOn(true);
        setControlWordAndWaitForAck(controlWord, new StatusWordCheck() {
            @Override
            public boolean checkStatusWord(StatusWord statusWord) {
                return statusWord.getStateCode() == StatusWord.States.SWITCHED_ON;
            }
        });

        controlWord.setEnableOperation(true);
        setControlWordAndWaitForAck(controlWord, new StatusWordCheck() {
            @Override
            public boolean checkStatusWord(StatusWord statusWord) {
                return statusWord.getStateCode() == StatusWord.States.QUICK_STOP_ACTIVE || statusWord.getStateCode() == StatusWord.States.OPERATION_ENABLED;
            }
        });
        this.operationControl = controlWord;
    }

    /**
     * Method that disables the device. Only turns off the power. Not the logic
     *
     * @throws NanolibHelper.NanolibException if there is an error disabling the device
     */
    public void disable() throws NanolibHelper.NanolibException {
        ControlWord controlWord = new ControlWord();
        controlWord.setEnableOperation(false);
        controlWord.setSwitchOn(false);
        controlWord.setEnableVoltage(false);
        controlWord.setQuickStop(false);
        writeRegister(CONTROL_WORD, controlWord.toInt());
        setControlWordAndWaitForAck(controlWord, new StatusWordCheck() {
            @Override
            public boolean checkStatusWord(StatusWord statusWord) {
                return !statusWord.readyToSwitchOn;
            }
        });
    }

    /**
     * Method that starts the device
     *
     * @param operationMode The operation mode of the device
     * @param homeMethod    The homing method of the device, 0 to skip homing
     * @throws DeviceCommunicationException if there is an error starting the device
     */
    public void start(int operationMode, int homeMethod) throws DeviceCommunicationException {
        this.operationMode = Math.max(OperationMode.PROFILE_POSITION, Math.min(OperationMode.HOMING, operationMode));
        try {
            setBrakeStatus(false);
            if (homeMethod == 0) {
                writeRegister(MODE_OF_OPERATION, operationMode);
                enable();
            } else {
                home(homeMethod);
                writeRegister(MODE_OF_OPERATION, operationMode);
            }
            setBrakeStatus(true);
            initialized.set(true);
            stateFunction.notifyStateChange();
        } catch (NanolibHelper.NanolibException | InterruptedException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that starts the device
     *
     * @param operationMode The operation mode of the device
     * @throws DeviceCommunicationException if there is an error starting the device
     */
    public void start(int operationMode) throws DeviceCommunicationException {
        start(operationMode, 0);
    }

    /**
     * Method that stops the device
     *
     * @throws DeviceCommunicationException if there is an error stopping the device
     */
    public void stop() throws DeviceCommunicationException {
        try {
            setBrakeStatus(true);
            ControlWord controlWord = new ControlWord(0);
            writeRegister(CONTROL_WORD, controlWord.toInt());
            initialized.set(false);
            stateFunction.notifyStateChange();
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the velocity of the device
     *
     * @param velocity The velocity of the device in user defined units
     * @throws DeviceCommunicationException if there is an error setting the velocity
     */
    public void setVelocity(int velocity) throws DeviceCommunicationException {
        if (operationMode != OperationMode.VELOCITY_MODE && operationMode != OperationMode.PROFILE_VELOCITY)
            throw new RuntimeException("Operation mode is not VELOCITY");
        try {
            isMoving.set(velocity != 0);
            stateFunction.notifyStateChange();
            setBrakeStatus(velocity == 0);
            writeRegister(TARGET_VELOCITY, velocity);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the velocity of the device
     *
     * @return The velocity of the device in user defined units
     * @throws DeviceCommunicationException if there is an error getting the velocity
     */
    public int getVelocity() throws DeviceCommunicationException {
        if (operationMode != OperationMode.VELOCITY_MODE && operationMode != OperationMode.PROFILE_VELOCITY)
            throw new RuntimeException("Operation mode is not VELOCITY");
        try {
            return readRegister(TARGET_VELOCITY);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the position of the device
     *
     * @param position The position of the device in user defined units
     * @return A future that resolves to true if the operation is successful
     * @throws DeviceCommunicationException if there is an error setting the position
     */
    public Future<Boolean> setPositionAbsolute(int position) throws DeviceCommunicationException {
        if (operationMode != OperationMode.PROFILE_POSITION)
            throw new RuntimeException("Operation mode is not PROFILE_POSITION");
        position = Math.max(-8388608, Math.min(8388607, position));
        this.position = position;
        int finalPosition = position;
        targetPos.set(position);
        isMoving.set(true);
        stateFunction.notifyStateChange();
        return executorService.submit(() -> {
            try {
                setBrakeStatus(false);
                writeRegister(TARGET_POSITION, finalPosition);
                operationControl.setOperationModeSpecific_4(true);
                operationControl.setOperationModeSpecific_5(true);
                operationControl.setOperationModeSpecific_6(false);
                setControlWordAndWaitForAck(operationControl, new StatusWordCheck() {
                    @Override
                    public boolean checkStatusWord(StatusWord statusWord) {
                        return statusWord.targetReached || !initialized.get();
                    }
                });
                if (!initialized.get()) return false;
                operationControl.setOperationModeSpecific_4(false);
                setControlWordAndWaitForAck(operationControl, new StatusWordCheck() {
                    @Override
                    public boolean checkStatusWord(StatusWord statusWord) {
                        return true;
                    }
                });
                setBrakeStatus(true);
                currentPos.set(finalPosition);
                isMoving.set(false);
                stateFunction.notifyStateChange();
                return true;
            } catch (Exception e) {
                return false;
            }
        });

    }

    /**
     * Method that sets the position of the device relative to the current position
     *
     * @param position The position to add to the current position in user defined units
     * @return A future that resolves to true if the operation is successful
     * @throws DeviceCommunicationException if there is an error setting the position
     */
    public Future<Boolean> setPositionRelative(int position) throws DeviceCommunicationException {
        if (operationMode != OperationMode.PROFILE_POSITION)
            throw new RuntimeException("Operation mode is not PROFILE_POSITION");
        position = Math.max(-8388608, Math.min(8388607, position));
        this.position += position;
        int finalPosition = position;
        targetPos.set(this.position);
        isMoving.set(true);
        stateFunction.notifyStateChange();
        return executorService.submit(() -> {
            try {
                setBrakeStatus(false);
                writeRegister(TARGET_POSITION, finalPosition);
                operationControl.setOperationModeSpecific_4(true);
                operationControl.setOperationModeSpecific_5(true);
                operationControl.setOperationModeSpecific_6(true);
                setControlWordAndWaitForAck(operationControl, new StatusWordCheck() {
                    @Override
                    public boolean checkStatusWord(StatusWord statusWord) {
                        return statusWord.targetReached || !initialized.get();
                    }
                });
                if (!initialized.get()) return false;
                operationControl.setOperationModeSpecific_4(false);
                setControlWordAndWaitForAck(operationControl, new StatusWordCheck() {
                    @Override
                    public boolean checkStatusWord(StatusWord statusWord) {
                        return true;
                    }
                });
                setBrakeStatus(true);
                currentPos.set(this.position);
                isMoving.set(false);
                stateFunction.notifyStateChange();
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Method that gets the position of the device
     *
     * @return The position of the device in user defined units
     * @throws DeviceCommunicationException if there is an error getting the position
     */
    public int getPosition() throws DeviceCommunicationException {
        if (operationMode != OperationMode.VELOCITY_MODE) throw new RuntimeException("Operation mode is not VELOCITY");
        try {
            return readRegister(TARGET_POSITION);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the torque of the device
     *
     * @param torque The torque of the device in user defined units
     * @throws DeviceCommunicationException if there is an error setting the torque
     */
    public void setTorque(int torque) throws DeviceCommunicationException {
        if (operationMode != OperationMode.PROFILE_TORQUE) throw new RuntimeException("Operation mode is not VELOCITY");
        try {
            writeRegister(TARGET_TORQUE, torque);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the torque of the device
     *
     * @return The torque of the device in user defined units
     * @throws DeviceCommunicationException if there is an error getting the torque
     */
    public int getTorque() throws DeviceCommunicationException {
        if (operationMode != OperationMode.PROFILE_TORQUE) throw new RuntimeException("Operation mode is not VELOCITY");
        try {
            return readRegister(TARGET_TORQUE);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the maximum current of the device
     *
     * @param current The maximum current of the device in user defined units
     * @throws DeviceCommunicationException if there is an error setting the maximum current
     */
    public void setMaxCurrent(int current) throws DeviceCommunicationException {
        try {
            writeRegister(MAX_MOTOR_CURRENT, current);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the maximum current of the device
     *
     * @return The maximum current of the device in user defined units
     * @throws DeviceCommunicationException if there is an error getting the maximum current
     */
    public int getMaxCurrent() throws DeviceCommunicationException {
        try {
            return readRegister(MAX_MOTOR_CURRENT);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the acceleration of the device
     *
     * @param acceleration The acceleration of the device in user defined units
     * @throws DeviceCommunicationException if there is an error setting the acceleration
     */
    public void setAcceleration(int acceleration) throws DeviceCommunicationException {
        try {
            writeRegister(PROFILE_ACCELERATION, acceleration);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the ramp speed of the device
     * @param speed The speed of the device in user defined units
     * @throws DeviceCommunicationException if there is an error setting the speed
     */
    public void setTravelSpeed(int speed) throws DeviceCommunicationException {
        try {
            writeRegister(PROFILE_VELOCITY, speed);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the ramp speed of the device
     * @return The speed of the device in user defined units
     * @throws DeviceCommunicationException if there is an error getting the speed
     */
    public int getTravelSpeed() throws DeviceCommunicationException {
        try {
            return readRegister(PROFILE_VELOCITY);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the deceleration of the device
     *
     * @param deceleration The deceleration of the device in user defined units
     * @throws DeviceCommunicationException if there is an error setting the deceleration
     */
    public void setDeceleration(int deceleration) throws DeviceCommunicationException {
        try {
            writeRegister(PROFILE_DECELERATION, deceleration);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the deceleration of the device
     *
     * @return The deceleration of the device in user defined units
     * @throws DeviceCommunicationException if there is an error getting the deceleration
     */
    public int getDeceleration() throws DeviceCommunicationException {
        try {
            return readRegister(PROFILE_DECELERATION);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the closed loop position proportional gain
     *
     * @param gain The gain to set
     */
    public void setClosedLoopPositionProportionalGain(int gain) throws DeviceCommunicationException {
        try {
            writeRegister(CL_POSITION_PROPORTIONAL_GAIN, gain);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the closed loop position proportional gain
     * @return The gain of the closed loop position
     * @throws DeviceCommunicationException if there is an error getting the gain
     */
    public int getClosedLoopPositionProportionalGain() throws DeviceCommunicationException {
        try {
            return readRegister(CL_POSITION_PROPORTIONAL_GAIN);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the closed loop position integral gain
     *
     * @param gain The gain to set
     * @throws DeviceCommunicationException if there is an error setting the gain
     */
    public void setClosedLoopPositionIntegralGain(int gain) throws DeviceCommunicationException {
        try {
            writeRegister(CL_POSITION_INTEGRAL_GAIN, gain);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the closed loop position integral gain
     * @return The gain of the closed loop position
     * @throws DeviceCommunicationException if there is an error getting the gain
     */
    public int getClosedLoopPositionIntegralGain() throws DeviceCommunicationException {
        try {
            return readRegister(CL_POSITION_INTEGRAL_GAIN);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the closed loop velocity proportional gain
     *
     * @param gain The gain to set
     * @throws DeviceCommunicationException if there is an error setting the gain
     */
    public void setClosedLoopVelocityProportionalGain(int gain) throws DeviceCommunicationException {
        try {
            writeRegister(CL_VELOCITY_PROPORTIONAL_GAIN, gain);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the closed loop velocity proportional gain
     * @return The gain of the closed loop velocity
     * @throws DeviceCommunicationException if there is an error getting the gain
     */
    public int getClosedLoopVelocityProportionalGain() throws DeviceCommunicationException {
        try {
            return readRegister(CL_VELOCITY_PROPORTIONAL_GAIN);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the closed loop velocity integral gain
     *
     * @param gain The gain to set
     * @throws DeviceCommunicationException if there is an error setting the gain
     */
    public void setClosedLoopVelocityIntegralGain(int gain) throws DeviceCommunicationException {
        try {
            writeRegister(CL_VELOCITY_INTEGRAL_GAIN, gain);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the closed loop velocity integral gain
     * @return The gain of the closed loop velocity
     * @throws DeviceCommunicationException if there is an error getting the gain
     */
    public int getClosedLoopVelocityIntegralGain() throws DeviceCommunicationException {
        try {
            return readRegister(CL_VELOCITY_INTEGRAL_GAIN);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the closed loop torque proportional gain
     *
     * @param gain The gain to set
     * @throws DeviceCommunicationException if there is an error setting the gain
     */
    public void setClosedLoopTorqueProportionalGain(int gain) throws DeviceCommunicationException {
        try {
            writeRegister(CL_TORQUE_PROPORTIONAL_GAIN, gain);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the closed loop torque proportional gain
     * @return The gain of the closed loop torque
     * @throws DeviceCommunicationException if there is an error getting the gain
     */
    public int getClosedLoopTorqueProportionalGain() throws DeviceCommunicationException {
        try {
            return readRegister(CL_TORQUE_PROPORTIONAL_GAIN);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the closed loop torque integral gain
     *
     * @param gain The gain to set
     * @throws DeviceCommunicationException if there is an error setting the gain
     */
    public void setClosedLoopTorqueIntegralGain(int gain) throws DeviceCommunicationException {
        try {
            writeRegister(CL_TORQUE_INTEGRAL_GAIN, gain);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the closed loop torque integral gain
     * @return The gain of the closed loop torque
     * @throws DeviceCommunicationException if there is an error getting the gain
     */
    public int getClosedLoopTorqueIntegralGain() throws DeviceCommunicationException {
        try {
            return readRegister(CL_TORQUE_INTEGRAL_GAIN);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the closed loop flux proportional gain
     *
     * @param gain The gain to set
     * @throws DeviceCommunicationException if there is an error setting the gain
     */
    public void setClosedLoopFluxProportionalGain(int gain) throws DeviceCommunicationException {
        try {
            writeRegister(CL_FLUX_PROPORTIONAL_GAIN, gain);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the closed loop flux proportional gain
     * @return The gain of the closed loop flux
     * @throws DeviceCommunicationException if there is an error getting the gain
     */
    public int getClosedLoopFluxProportionalGain() throws DeviceCommunicationException {
        try {
            return readRegister(CL_FLUX_PROPORTIONAL_GAIN);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the closed loop flux integral gain
     *
     * @param gain The gain to set
     * @throws DeviceCommunicationException if there is an error setting the gain
     */
    public void setClosedLoopFluxIntegralGain(int gain) throws DeviceCommunicationException {
        try {
            writeRegister(CL_FLUX_INTEGRAL_GAIN, gain);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the closed loop flux integral gain
     * @return The gain of the closed loop flux
     * @throws DeviceCommunicationException if there is an error getting the gain
     */
    public int getClosedLoopFluxIntegralGain() throws DeviceCommunicationException {
        try {
            return readRegister(CL_FLUX_INTEGRAL_GAIN);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the closed loop position window
     * @param window The window in user defined units of witch the position is considered "REACHED"
     * @throws DeviceCommunicationException if there is an error setting the window
     */
    public void setPositionWindow(int window) throws DeviceCommunicationException {
        try {
            writeRegister(POSITION_WINDOW, window);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the closed loop position window
     * @return The window in user defined units of witch the position is considered "REACHED"
     * @throws DeviceCommunicationException if there is an error getting the window
     */
    public int getPositionWindow() throws DeviceCommunicationException {
        try {
            return readRegister(POSITION_WINDOW);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the closed loop position window time
     * @param time The time in milliseconds of witch the position is considered "REACHED"
     * @throws DeviceCommunicationException if there is an error setting the window time
     */
    public void setPositionWindowTime(int time) throws DeviceCommunicationException {
        try {
            writeRegister(POSITION_WINDOW_TIME, time);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the closed loop position window time
     * @return The time in milliseconds of witch the position is considered "REACHED"
     * @throws DeviceCommunicationException if there is an error getting the window time
     */
    public int getPositionWindowTime() throws DeviceCommunicationException {
        try {
            return readRegister(POSITION_WINDOW_TIME);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the offset between actual 0 and hardware 0
     * @param offset The offset to set
     * @throws DeviceCommunicationException if there is an error setting the offset
     */
    public void setHomeOffset(int offset) throws DeviceCommunicationException {
        try {
            writeRegister(HOME_OFFSET, offset);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the offset between actual 0 and hardware 0
     * @return The offset between actual 0 and hardware 0
     * @throws DeviceCommunicationException if there is an error getting the offset
     */
    public int getHomeOffset() throws DeviceCommunicationException {
        try {
            return readRegister(HOME_OFFSET);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the unit position factor
     *
     * @param factor The factor to set
     * @throws DeviceCommunicationException if there is an error setting the factor
     */
    public void setUnitPositionFactor(int factor) throws DeviceCommunicationException {
        try {
            UnitControl unitControl = new UnitControl(readRegister(SI_UNIT_POSITION));
            unitControl.setFactor(UnitControl.FACTOR(factor));
            writeRegister(SI_UNIT_POSITION, unitControl.toInt());
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the unit position factor
     *
     * @return The factor of the unit position
     * @throws DeviceCommunicationException if there is an error getting the factor
     */
    public UnitControl getUnitPosition() throws DeviceCommunicationException {
        try {
            return new UnitControl(readRegister(SI_UNIT_POSITION));
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the unit position unit
     *
     * @param unit The unit to set
     * @throws DeviceCommunicationException if there is an error setting the unit
     */
    public void setUnitPositionUnit(byte unit) throws DeviceCommunicationException {
        try {
            UnitControl unitControl = new UnitControl(readRegister(SI_UNIT_POSITION));
            unitControl.setUnit(unit);
            writeRegister(SI_UNIT_POSITION, unitControl.toInt());
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the maximum speed of the device
     *
     * @param speed The maximum speed of the device in user defined units
     * @throws DeviceCommunicationException if there is an error setting the maximum speed
     */
    public void setMaxSpeed(int speed) throws DeviceCommunicationException {
        try {
            writeRegister(MAX_MOTOR_SPEED, speed);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the maximum speed of the device
     *
     * @return The maximum speed of the device in user defined units
     * @throws DeviceCommunicationException if there is an error getting the maximum speed
     */
    public int getMaxSpeed() throws DeviceCommunicationException {
        try {
            return readRegister(MAX_MOTOR_SPEED);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the acceleration of the device
     *
     * @return The acceleration of the device in user defined units
     * @throws DeviceCommunicationException if there is an error getting the acceleration
     */
    public int getAcceleration() throws DeviceCommunicationException {
        try {
            return readRegister(PROFILE_ACCELERATION);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method thar read the error register
     *
     * @return The error flags
     * @throws DeviceCommunicationException if there is an error reading the error register
     */
    public ErrorFlags getErrors() throws DeviceCommunicationException {
        try {
            return new ErrorFlags(readRegister(ERROR_CODE));
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that clears the errors of the device
     *
     * @throws DeviceCommunicationException if there is an error clearing the errors
     */
    public void clearErrors() throws DeviceCommunicationException {
        try {
            operationControl.setFaultReset(true);
            setControlWordAndWaitForAck(operationControl, new StatusWordCheck() {
                @Override
                public boolean checkStatusWord(StatusWord statusWord) {
                    return true;
                }
            });
            operationControl.setFaultReset(false);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that stores all the parameters of the device
     *
     * @throws DeviceCommunicationException if there is an error storing the parameters
     */
    public void storeAllParams() throws DeviceCommunicationException {
        try {
            writeRegister(STORE_ALL_PARAMETERS, 1702257011);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that sets the target current of the device
     * @param current The target current of the device in user defined units
     * @throws DeviceCommunicationException if there is an error setting the target current
     */
    public void setTargetCurrent(int current) throws DeviceCommunicationException {
        try {
            writeRegister(TARGET_MOTOR_CURRENT, current);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Method that gets the target current of the device
     * @return The target current of the device in user defined units
     * @throws DeviceCommunicationException if there is an error getting the target current
     */
    public int getTargetCurrent() throws DeviceCommunicationException {
        try {
            return readRegister(TARGET_MOTOR_CURRENT);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

}
