package com.lucaf.robotic_core.stepperOnline.iDmRs;

import com.lucaf.robotic_core.State;
import com.lucaf.robotic_core.dataInterfaces.impl.RegisterInterface;
import com.lucaf.robotic_core.motors.impl.MotorInterface;
import com.lucaf.robotic_core.utils.StateUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.lucaf.robotic_core.stepperOnline.iDmRs.Constants.*;

/**
 * Class that represents the iDM_RS StepperOnline device
 */
public class IDMRS extends MotorInterface {

    /**
     * The low-level register/communication interface used to talk with the device.
     */
    final RegisterInterface connection;

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
    final AtomicInteger rampMode = new AtomicInteger(0x00);

    /**
     * The executor service for async operations
     */
    final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Last verified position
     */
    final AtomicLong currentPosition = new AtomicLong(0);

    /**
     * Target position sent to the device
     */
    final AtomicLong targetPosition = new AtomicLong(0);

    /**
     * The current set speed of the device
     */
    final AtomicInteger speed = new AtomicInteger(0);

    /**
     * The current acceleration of the device
     */
    final AtomicInteger acceleration = new AtomicInteger(0);

    /**
     * The current deceleration of the device
     */
    final AtomicInteger deceleration = new AtomicInteger(0);

    @Setter
    HomingControl homingControl = new HomingControl();

    /**
     * Constructor of the class
     *
     * @param registerInterface low-level interface used to read/write registers
     * @param state             shared map containing the device state
     * @param notifyStateChange optional callback that will be notified on state changes
     */
    public IDMRS(RegisterInterface registerInterface, HashMap<String, Object> state, State notifyStateChange) {
        this.connection = registerInterface;
        this.state = state;
        this.stateFunction = notifyStateChange;
        initState();
    }

    /**
     * Constructor of the class
     *
     * @param registerInterface low-level interface used to read/write registers
     * @param state             shared map containing the device state
     */
    public IDMRS(RegisterInterface registerInterface, HashMap<String, Object> state) {
        this(registerInterface, state, null);
    }

    /**
     * Constructor of the class
     *
     * @param registerInterface low-level interface used to read/write registers
     */
    public IDMRS(RegisterInterface registerInterface) {
        this(registerInterface, new HashMap<>(), null);
    }

    /**
     * Initializes the device state by setting default values and updating the state map.
     */
    private void initState() {
        if (state.containsKey("current_position")) {
            currentPosition.set(StateUtils.getLong(state.get("current_position")));
        }
        state.put("current_position", currentPosition);
        if (state.containsKey("target_position")) {
            targetPosition.set(StateUtils.getLong(state.get("target_position")));
        }
        state.put("target_position", targetPosition);
        if (state.containsKey("speed")) {
            speed.set(StateUtils.getInt(state.get("speed")));
        }
        state.put("speed", speed);
        if (state.containsKey("acceleration")) {
            acceleration.set(StateUtils.getInt(state.get("acceleration")));
        }
        state.put("acceleration", acceleration);
        if (state.containsKey("deceleration")) {
            deceleration.set(StateUtils.getInt(state.get("deceleration")));
        }
        if (state.containsKey("ramp_mode")) {
            rampMode.set(StateUtils.getInt(state.get("ramp_mode")));
        }
        state.put("ramp_mode", rampMode);

        state.put("deceleration", deceleration);
        state.put("is_moving", isMoving);
        state.put("initialized", isInitialized);
        state.put("has_fault", hasError);
        state.put("fault", "");
        state.put("stopped", isStopped);
        notifyStateChange();
    }

    /**
     * Notify the registered state callback (if present) when the internal state changes.
     * This method invokes the callback method on `stateFunction`.
     */
    void notifyStateChange() {
        if (stateFunction != null)
            stateFunction.notifyStateChange();
    }

    /**
     * Sets the ramp mode for the device. The ramp mode determines the acceleration profile.
     *
     * @param rampMode the ramp mode number to set
     */
    public void setRampMode(byte rampMode) {
        this.rampMode.set(rampMode);
        notifyStateChange();
    }

    /**
     * Initializes the device asynchronously. This method sets up the device and performs homing if required.
     *
     * @return a Future object containing the result of the initialization (true if successful, false otherwise)
     * @throws RejectedExecutionException if the executor service is shut down
     */
    public Future<Boolean> initialize() throws RejectedExecutionException {
        return executorService.submit(() -> {
            if (isShutdown()) return false;
            connection.logInfo("Initializing device");
            if (!homingControl.getHomingMethod().equals(HomingMethod.NO_HOMING)) {
                try {
                    connection.logInfo("Starting homing");
                    connection.writeInteger(HOMING_METHOD, homingControl.toInt());
                    connection.writeInteger(STATUS_MODE, StatusMode.HOMING);
                    isMoving.set(true);
                    long startTime = System.currentTimeMillis();
                    while (true) {
                        if (!isMoving.get()) throw new IOException("Device stopped");
                        if (homingControl.getHomingTimeout() > 0 && startTime + homingControl.getHomingTimeout() < System.currentTimeMillis()) {
                            throw new IOException("Homing timeout reached");
                        }
                        if (getStatusMode().getSTATUS_CODE() == 0) break;
                        Thread.sleep(50);
                    }
                } catch (Exception e) {
                    connection.logError("Error homing: " + e.getMessage());
                    isMoving.set(false);
                    stop();
                    return false;
                }
            }
            isInitialized.set(true);
            targetPosition.set(0);
            currentPosition.set(0);
            isMoving.set(false);
            isStopped.set(false);
            return true;
        });
    }

    /**
     * Method that emergency stops the device
     *
     * @throws IOException if there is an error stopping the device
     */
    @Override
    public void stop() throws IOException {
        connection.logInfo("Stopping device");
        isMoving.set(false);
        isStopped.set(true);
        connection.writeInteger(STATUS_MODE, StatusMode.getEMERGENCY_STOP());
    }

    /**
     * Performs a graceful shutdown of internal executors and stops the device.
     * This attempts to stop periodic polling and cancels queued tasks.
     *
     * @throws IOException if an I/O error occurs while stopping the device
     */
    @Override
    public void shutdown() throws IOException {
        connection.logDebug("Shutting down device");
        executorService.shutdownNow();
        isShutdown.set(true);
        stop();
    }

    /**
     * Method that sets the speed of the device
     *
     * @param speed the speed to set
     * @throws IOException if there is an error setting the speed
     */
    public void setSpeed(int speed) throws IOException {
        this.speed.set(speed);
        connection.writeInteger(VELOCITY, speed);
        if (controlMode.getCONTROL_MODE() == ControlType.VELOCITY_MODE.getValue()) {
            isMoving.set(speed != 0);
            notifyStateChange();
            connection.writeInteger(STATUS_MODE, StatusMode.getSegmentPositioning((byte) rampMode.get()));
        }

    }

    /**
     * Method that gets the speed of the device
     *
     * @return the speed of the device
     * @throws IOException if there is an error getting the speed
     */
    public int getSpeed() throws IOException {
        int speed = connection.readShort(VELOCITY);
        this.speed.set(speed);
        return speed;
    }

    /**
     * Method that sets the acceleration of the device
     *
     * @param acceleration the acceleration to set
     * @throws IOException if there is an error setting the acceleration
     */
    public void setAcceleration(int acceleration) throws IOException {
        this.acceleration.set(acceleration);
        connection.writeInteger(ACCELERATION, acceleration);
    }

    /**
     * Method that gets the acceleration of the device
     *
     * @return the acceleration of the device
     * @throws IOException if there is an error getting the acceleration
     */
    public int getAcceleration() throws IOException {
        int acceleration = connection.readShort(ACCELERATION);
        this.acceleration.set(acceleration);
        return acceleration;
    }

    /**
     * Method that sets the deceleration of the device
     *
     * @param deceleration the deceleration to set
     * @throws IOException if there is an error setting the deceleration
     */
    public void setDeceleration(int deceleration) throws IOException {
        this.deceleration.set(deceleration);
        connection.writeInteger(DECELERATION, deceleration);
    }

    /**
     * Method that gets the deceleration of the device
     *
     * @return the deceleration of the device
     * @throws IOException if there is an error getting the deceleration
     */
    public int getDeceleration() throws IOException {
        int deceleration = connection.readShort(DECELERATION);
        this.deceleration.set(deceleration);
        return deceleration;
    }

    /**
     * Method that moves the device to a position
     *
     * @param position the position to move to
     * @throws IOException if there is an error moving the device
     */
    public void setPosition(long position) throws IOException {
        if (controlMode.isRELATIVE_POSITIONING()) {
            targetPosition.set(targetPosition.get() + position);
        } else {
            targetPosition.set(position);
        }
        connection.writeSignedLong(TARGET_POSITION_HIGH, position, false);
        connection.writeInteger(STATUS_MODE, StatusMode.getSegmentPositioning((byte) rampMode.get()));
    }

    /**
     * Method that sets the control mode
     *
     * @throws IOException if there is an error setting the control mode
     */
    public void writeControlMode() throws IOException {
        connection.writeInteger(CONTROL_MODE, controlMode.toCode());
    }

    /**
     * Method that sets mode to positioning
     *
     * @throws IOException if there is an error setting the mode
     */
    public void setPositioningMode() throws IOException {
        controlMode.setCONTROL_MODE(ControlType.POSITION_MODE.getValue());
        writeControlMode();
    }

    /**
     * Sets the mode to velocity control.
     *
     * @throws IOException if there is an error setting the mode
     */
    public void setVelocityMode() throws IOException {
        controlMode.setCONTROL_MODE(ControlType.VELOCITY_MODE.getValue());
        writeControlMode();
    }

    /**
     * Methos that gets the status of the motor
     *
     * @return StatusMode class with the status of the motor
     * @throws IOException if there is an error getting the status
     */
    public StatusMode getStatusMode() throws IOException {
        return new StatusMode(connection.readShort(STATUS_MODE));
    }

    /**
     * Enables or disables relative positioning mode.
     *
     * @param relative true to enable relative positioning, false to disable
     * @throws IOException if there is an error setting the relative positioning mode
     */
    public void setRelativePositioning(boolean relative) throws IOException {
        controlMode.setRELATIVE_POSITIONING(relative);
        writeControlMode();
    }

    /**
     * Retrieves the digital input statuses.
     *
     * @return a DigitalInputs object representing the current digital input statuses
     * @throws IOException if there is an error retrieving the statuses
     */
    public DigitalInputs getDigitalInputs() throws IOException {
        return new DigitalInputs(connection.readShort(DIGITAL_INPUTS_STATUS));
    }

    /**
     * Sets the digital input statuses.
     *
     * @param digitalInputs the digital input statuses to set
     * @throws IOException if there is an error setting the statuses
     */
    public void setDigitalInputs(DigitalInputs digitalInputs) throws IOException {
        connection.writeInteger(DIGITAL_INPUTS_STATUS, digitalInputs.toInt());
    }

    /**
     * Retrieves the digital output statuses.
     *
     * @return a DigitalOutputs object representing the current digital output statuses
     * @throws IOException if there is an error retrieving the statuses
     */
    public DigitalOutputs getDigitalOutputs() throws IOException {
        return new DigitalOutputs(connection.readShort(DIGITAL_OUTPUTS_STATUS));
    }

    /**
     * Sets the digital output statuses.
     *
     * @param digitalOutputs the digital output statuses to set
     * @throws IOException if there is an error setting the statuses
     */
    public void setDigitalOutputs(DigitalOutputs digitalOutputs) throws IOException {
        connection.writeInteger(DIGITAL_OUTPUTS_STATUS, digitalOutputs.toInt());
    }

    /**
     * Retrieves the digital input settings for a specific input.
     *
     * @param input the digital input number (1-7)
     * @return a DigitalInput object representing the settings for the specified input
     * @throws IOException if there is an error retrieving the settings
     */
    public DigitalInput getDigitalInput(int input) throws IOException {
        if (input <= 0 || input > 7) throw new RuntimeException("Invalid digital input number");
        switch (input) {
            case 1 -> {
                return new DigitalInput(connection.readShort(DI1));
            }
            case 2 -> {
                return new DigitalInput(connection.readShort(DI2));
            }
            case 3 -> {
                return new DigitalInput(connection.readShort(DI3));
            }
            case 4 -> {
                return new DigitalInput(connection.readShort(DI4));
            }
            case 5 -> {
                return new DigitalInput(connection.readShort(DI5));
            }
            case 6 -> {
                return new DigitalInput(connection.readShort(DI6));
            }
            case 7 -> {
                return new DigitalInput(connection.readShort(DI7));
            }
        }
        return null;
    }

    /**
     * Sets the digital input settings for a specific input.
     *
     * @param input the digital input number (1-7)
     * @param digitalInput the settings to apply to the specified input
     * @throws IOException if there is an error setting the input
     */
    public void setDigitalInput(int input, DigitalInput digitalInput) throws IOException {
        if (input <= 0 || input > 7) throw new RuntimeException("Invalid digital input number");
        switch (input) {
            case 1 -> {
                connection.writeInteger(DI1, digitalInput.toInt());
            }
            case 2 -> {
                connection.writeInteger(DI2, digitalInput.toInt());
            }
            case 3 -> {
                connection.writeInteger(DI3, digitalInput.toInt());
            }
            case 4 -> {
                connection.writeInteger(DI4, digitalInput.toInt());
            }
            case 5 -> {
                connection.writeInteger(DI5, digitalInput.toInt());
            }
            case 6 -> {
                connection.writeInteger(DI6, digitalInput.toInt());
            }
            case 7 -> {
                connection.writeInteger(DI7, digitalInput.toInt());
            }
        }
    }

    /**
     * Retrieves the digital output settings for a specific output.
     *
     * @param output the digital output number (1-3)
     * @return a DigitalOutput object representing the settings for the specified output
     * @throws IOException if there is an error retrieving the settings
     */
    public DigitalOutput getDigitalOutput(int output) throws IOException {
        if (output <= 0 || output > 3) throw new RuntimeException("Invalid digital output number");
        switch (output) {
            case 1 -> {
                return new DigitalOutput(connection.readShort(DO1));
            }
            case 2 -> {
                return new DigitalOutput(connection.readShort(DO2));
            }
            case 3 -> {
                return new DigitalOutput(connection.readShort(DO3));
            }
        }
        return null;
    }

    /**
     * Sets the digital output settings for a specific output.
     *
     * @param output the digital output number (1-3)
     * @param digitalOutput the settings to apply to the specified output
     * @throws IOException if there is an error setting the output
     */
    public void setDigitalOutput(int output, DigitalOutput digitalOutput) throws IOException {
        if (output <= 0 || output > 3) throw new RuntimeException("Invalid digital output number");
        switch (output) {
            case 1 -> {
                connection.writeInteger(DO1, digitalOutput.toInt());
            }
            case 2 -> {
                connection.writeInteger(DO2, digitalOutput.toInt());
            }
            case 3 -> {
                connection.writeInteger(DO3, digitalOutput.toInt());
            }
        }
    }

    /**
     * Sets the homing speed for the device.
     *
     * @param speed the homing speed to set
     * @throws IOException if there is an error setting the speed
     */
    public void setHomingSpeed(long speed) throws IOException {
        //connection.writeInteger(HOMING_SPEED_LOW, (int) speed);
        connection.writeSignedLong(HOMING_SPEED_HIGH, speed, false);
    }

    /**
     * Retrieves the current homing speed of the device.
     *
     * @return the current homing speed
     * @throws IOException if there is an error retrieving the speed
     */
    public long getHomingSpeed() throws IOException {
        return connection.readSignedLong(HOMING_SPEED_HIGH, false);
    }

    /**
     * Sets the homing acceleration for the device.
     *
     * @param acceleration the homing acceleration to set
     * @throws IOException if there is an error setting the acceleration
     */
    public void setHomingAcceleration(int acceleration) throws IOException {
        connection.writeInteger(HOMING_ACCELERATION, acceleration);
    }

    /**
     * Retrieves the current homing acceleration of the device.
     *
     * @return the current homing acceleration
     * @throws IOException if there is an error retrieving the acceleration
     */
    public int getHomingAcceleration() throws IOException {
        return connection.readShort(HOMING_ACCELERATION);
    }

    /**
     * Sets the homing deceleration for the device.
     *
     * @param deceleration the homing deceleration to set
     * @throws IOException if there is an error setting the deceleration
     */
    public void setHomingDeceleration(int deceleration) throws IOException {
        connection.writeInteger(HOMING_DECELERATION, deceleration);
    }

    /**
     * Retrieves the current homing deceleration of the device.
     *
     * @return the current homing deceleration
     * @throws IOException if there is an error retrieving the deceleration
     */
    public int getHomingDeceleration() throws IOException {
        return connection.readShort(HOMING_DECELERATION);
    }

    /**
     * Sets the homing stop position for the device.
     *
     * @param position the homing stop position to set
     * @throws IOException if there is an error setting the position
     */
    public void setHomingStopPosition(long position) throws IOException {
        connection.writeSignedLong(HOMING_STOP_POSITION_HIGH, position, false);
    }

    /**
     * Retrieves the current homing stop position of the device.
     *
     * @return the current homing stop position
     * @throws IOException if there is an error retrieving the position
     */
    public long getHomingStopPosition() throws IOException {
        return connection.readSignedLong(HOMING_STOP_POSITION_HIGH, false);
    }

    /**
     * Retrieves the current homing method of the device.
     *
     * @return a HomingControl object representing the current homing method
     * @throws IOException if there is an error retrieving the method
     */
    public HomingControl getHomingMethod() throws IOException {
        return new HomingControl(connection.readShort(HOMING_METHOD));
    }

    /**
     * Method that waits for the device to reach the position
     *
     * @throws IOException if there is an error waiting for the position
     */
    public void waitReachedPosition(int timeout) throws IOException {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (timeout > 0 && startTime + timeout < System.currentTimeMillis()) {
                connection.writeInteger(STATUS_MODE, StatusMode.getEMERGENCY_STOP());
                throw new IOException("Timeout reached while waiting for position to be reached");
            }
            if (isStopped.get() || !isMoving.get()) throw new IOException("Device stopped");
            StatusMode mode = getStatusMode();
            if (!mode.isRunning()) break;
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
    }

    /**
     * Estimates the time required for the device to reach a specified distance.
     *
     * @param distance the distance to cover (in steps)
     * @return the estimated time (in seconds), or -1 if the estimation is not available
     * @throws IOException if there is an error during the estimation
     */
    int estimateArrivalTime(int distance) throws IOException {
        distance = Math.abs(distance);
        if (controlMode.getCONTROL_MODE() == ControlType.VELOCITY_MODE.getValue()) {
            return -1; // Not available in velocity mode
        }
        int speed = this.speed.get();
        if (speed == 0) {
            this.speed.set(getSpeed());
            speed = this.speed.get();
        }
        int acceleration = this.acceleration.get();
        if (acceleration == 0) {
            this.acceleration.set(getAcceleration());
            acceleration = this.acceleration.get();
        }
        int deceleration = this.deceleration.get();
        if (deceleration == 0) {
            this.deceleration.set(getDeceleration());
            deceleration = this.deceleration.get();
        }
        if (speed == 0 || acceleration == 0 || deceleration == 0) {
            return -1;
        }
        int timeToReachSpeed = speed / acceleration;
        int timeToStop = speed / deceleration;
        int distanceToReachSpeed = (speed * timeToReachSpeed) / 2;
        int distanceToStop = (speed * timeToStop) / 2;
        int distanceToCover = distance - distanceToReachSpeed - distanceToStop;
        if (distanceToCover < 0) {
            return (int) Math.ceil(Math.sqrt((2.0 * distance) / acceleration));
        } else {
            int timeAtConstantSpeed = distanceToCover / speed;
            return timeToReachSpeed + timeAtConstantSpeed + timeToStop;
        }
    }

    /**
     * Method that moves the device to a relative position and waits for the movement to end
     *
     * @param position the position to move to
     * @return a future with the result of the operation
     */
    public Future<Boolean> moveToPositionAndWait(int position, int timeout) {
        AtomicInteger finalTimeout = new AtomicInteger(timeout);
        return executorService.submit(() -> {
            connection.logDebug("Moving to position " + position);
            try {
                isMoving.set(true);
                setPosition(position);
                notifyStateChange();
                if (finalTimeout.get() < 0) {
                    finalTimeout.set(Math.max(2000, estimateArrivalTime((int) (position - currentPosition.get())) * 2));
                }
                waitReachedPosition(finalTimeout.get());
                isMoving.set(false);
                currentPosition.set(targetPosition.get());
                notifyStateChange();
                return true;
            } catch (Exception e) {
                connection.logError("Error moving to position " + position);
                return false;
            }
        });
    }

    /**
     * Method that moves the device to a relative position and waits for the movement to end
     *
     * @param position the position to move to
     * @return a future with the result of the operation
     */
    public Future<Boolean> moveToPositionAndWait(int position) {
        return moveToPositionAndWait(position, 0);
    }
}
