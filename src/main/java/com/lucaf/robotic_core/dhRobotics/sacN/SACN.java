package com.lucaf.robotic_core.dhRobotics.sacN;

import com.lucaf.robotic_core.State;
import com.lucaf.robotic_core.dataInterfaces.impl.RegisterInterface;
import com.lucaf.robotic_core.motors.impl.MotorInterface;
import com.lucaf.robotic_core.utils.StateUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lucaf.robotic_core.dhRobotics.sacN.Constants.*;

/**
 * Class representing a SAC_N motor controller device.
 */
public class SACN extends MotorInterface {

    /**
     * The low-level register/communication interface used to talk with the device.
     */
    final RegisterInterface connection;

    /**
     * The state map for the device
     */
    private final HashMap<String, Object> state;
    /**
     * Internal state holding the current position
     */
    private final AtomicInteger currentPosition = new AtomicInteger(0);

    /**
     * Internal state holding the target position
     */
    private final AtomicInteger targetPosition = new AtomicInteger(0);

    /**
     * The executor service used to run movement-related tasks in a separate thread
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    /**
     * Scheduled executor service used to poll device fault/status periodically
     */
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * Optional callback to notify external code about state changes
     */
    private final State stateFunction;

    /**
     * Primary constructor that initializes the SAC_N instance with an existing register interface
     * and a pre-existing state map. It also allows registering a notification callback
     * for state changes.
     *
     * @param registerInterface low-level interface used to read/write registers
     * @param state shared map containing the device state
     * @param notifyStateChange optional callback that will be notified on state changes
     */
    public SACN(RegisterInterface registerInterface, HashMap<String, Object> state, State notifyStateChange) {
        this.connection = registerInterface;
        this.state = state;
        this.stateFunction = notifyStateChange;
        initState();
    }

    /**
     * Constructor that initializes the instance with a register interface and a state map.
     * The notification callback is not set.
     *
     * @param registerInterface low-level interface used to read/write registers
     * @param state shared map containing the device state
     */
    public SACN(RegisterInterface registerInterface, HashMap<String, Object> state) {
        this(registerInterface, state, null);
    }

    /**
     * Constructor that initializes the instance only with the register interface.
     * A new empty state map is created internally.
     *
     * @param registerInterface low-level interface used to read/write registers
     */
    public SACN(RegisterInterface registerInterface) {
        this(registerInterface, new HashMap<>(), null);
    }

    /**
     * Initialize the internal state map by linking atomic objects and flags.
     * This routine keeps backward compatibility if the provided state map
     * already contains existing values.
     */
    void initState() {
        if (state.containsKey("current_position")) {
            currentPosition.set(StateUtils.getInt(state.get("current_position")));
        }
        state.put("current_position", currentPosition);
        if (state.containsKey("target_position")) {
            targetPosition.set(StateUtils.getInt(state.get("target_position")));
        }
        state.put("target_position", targetPosition);
        state.put("is_initialized", isInitialized);
        state.put("has_fault", hasError);
        state.put("fault", "");
        state.put("stopped", isStopped);
        state.put("is_moving", isMoving);
    }

    /**
     * Apply the provided SACNConfig configuration to the device by setting
     * acceleration and maximum speed.
     *
     * @param configuration the SACNConfig object containing desired parameters
     * @throws IOException if communication with the device fails
     */
    public void applyConfiguration(SACNConfig configuration) throws IOException {
        applyActuatorParameters(configuration.getTravelParameters());
    }

    /**
     * Apply the provided actuator parameters (acceleration and speed)
     * to the device by writing the appropriate registers.
     *
     * @param parameters the ActuatorParameters object containing desired values
     * @throws IOException if communication with the device fails
     */
    public void applyActuatorParameters(ActuatorParameters parameters) throws IOException {
        setAcceleration(parameters.getAcceleration());
        setSpeed(parameters.getSpeed());
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
     * Set up a periodic listener that checks the device for faults.
     * If a fault is detected it updates the state map and triggers the error callback.
     */
    private void setupErrorListener() {
        connection.logDebug("Setting up error listener");
        if (scheduledExecutorService != null) {
            connection.logDebug("Shutting down previous error listener");
            scheduledExecutorService.shutdownNow();
        }
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                if (scheduledExecutorService == null || scheduledExecutorService.isShutdown()) return;
                ErrorFlags errorFlags = getErrors();
                connection.logDebug("Checking for errors: " + errorFlags.toString());
                if (errorFlags.hasError()) {
                    state.put("fault", errorFlags.getErrorDescription());
                    hasError.set(true);
                    if (stateFunction != null) stateFunction.notifyError();
                } else {
                    state.put("fault", "");
                    hasError.set(false);
                }
            } catch (IOException e) {
                connection.logError("Error checking for errors: " + e.getMessage());
                state.put("fault", e.getMessage());
                hasError.set(true);
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * Immediately stops device movement and updates internal flags.
     * This call also sends the stop command to the device by writing the start/stop register.
     *
     * @throws IOException if communication with the device fails while sending the command
     */
    @Override
    public void stop() throws IOException {
        connection.logInfo("Stopping device");
        isMoving.set(false);
        isStopped.set(true);
        setEnabled(false);
    }

    /**
     * Perform a controlled shutdown of internal resources (executors) and stop the device.
     * It cancels scheduled tasks and sets the device into shutdown state.
     *
     * @throws IOException if an I/O error occurs during the shutdown procedure
     */
    @Override
    public void shutdown() throws IOException {
        connection.logDebug("Shutting down device");
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
        }
        executorService.shutdownNow();
        isShutdown.set(true);
        stop();
    }

    /**
     * Write the desired movement speed into the device register.
     *
     * @param speed movement speed (units and range depend on the device)
     * @throws IOException if the register write fails
     */
    public void setSpeed(int speed) throws IOException {
        connection.writeInteger(SPEED, speed);
    }

    /**
     * Read the currently set speed from the device.
     *
     * @return the speed read from the device register
     * @throws IOException if the register read fails
     */
    public int getSpeed() throws IOException {
        return connection.readShort(SPEED);
    }

    /**
     * Write the desired acceleration into the device register.
     *
     * @param velocity desired acceleration (units depend on the device)
     * @throws IOException if the register write fails
     */
    public void setAcceleration(int velocity) throws IOException {
        connection.writeInteger(ACCELERATION, velocity);
    }

    /**
     * Read the configured acceleration from the device.
     *
     * @return the acceleration read from the device register
     * @throws IOException if the register read fails
     */
    public int getAcceleration() throws IOException {
        return connection.readShort(ACCELERATION);
    }

    /**
     * Enable or disable the device by writing the appropriate register.
     *
     * @param enabled true to enable, false to disable
     * @throws IOException if the register write fails
     */
    public void setEnabled(boolean enabled) throws IOException {
        connection.writeInteger(START, enabled ? 1 : 0);
    }

    /**
     * Check whether the device is currently enabled by reading the status register.
     *
     * @return true if the device is enabled, false otherwise
     * @throws IOException if the register read fails
     */
    public boolean isEnabled() throws IOException {
        return connection.readShort(STATUS) == 1;
    }

    /**
     * Check whether the device has reached the target position by reading the motion feedback state.
     *
     * @return true if the motion has arrived at the target, false otherwise
     * @throws IOException if the register read fails
     */
    public boolean isArrived() throws IOException {
        return connection.readShort(FEEDBACK_MOTION_STATE) == 1;
    }

    /**
     * Retrieve the current status of the device as a Status object.
     *
     * @return an instance of {@link Status} containing current status flags
     * @throws IOException if the register read fails
     */
    public Status getStatus() throws IOException {
        return new Status(connection.readShort(STATUS));
    }

    /**
     * Read the code that indicates the "home" (initialization/reference) state.
     *
     * @return numeric code representing the home state
     * @throws IOException if the register read fails
     */
    public int isHome() throws IOException {
        return connection.readShort(FEEDBACK_INITIALIZATION_GRIP_STATE);
    }

    /**
     * Check if the device has completed initialization (homing).
     *
     * @return true if the device is back to "home" position, false otherwise
     * @throws IOException if the register read fails
     */
    public boolean hasInitialized() throws IOException {
        return getStatus().isBackHome();
    }

    /**
     * Synchronously wait until the device reaches the target position.
     * The method checks for timeout, device faults and movement state.
     *
     * @param timeout maximum time in milliseconds to wait; if <=0 waits indefinitely
     * @throws IOException if a timeout occurs, the device is in fault or the movement was stopped
     */
    public synchronized void waitReachedPosition(long timeout) throws IOException {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (timeout > 0 && System.currentTimeMillis() - startTime > timeout) {
                throw new IOException("Timeout waiting for position");
            }
            if (hasError.get()) {
                throw new IOException("Device has fault");
            }
            if (isStopped.get()){
                throw new IOException("Movement was stopped");
            }
            if (isArrived()) return;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new IOException("Thread interrupted while waiting for position");
            }
        }
    }

    /**
     * No-timeout variant of {@link #waitReachedPosition(long)} that waits indefinitely.
     *
     * @throws IOException in case of error or interruption
     */
    public void waitReachedPosition() throws IOException {
        waitReachedPosition(-1);
    }

    /**
     * Estimate arrival time (in milliseconds) given a motion profile with acceleration,
     * maximum speed and deceleration. The estimate assumes a simple trapezoidal profile.
     *
     * @param movement total distance to travel (device units)
     * @param speed maximum speed reached during the motion
     * @param acceleration initial acceleration
     * @param deceleation final deceleration
     * @return estimated arrival time in milliseconds
     */
    long estimateArrivalTime(int movement, int speed, int acceleration, int deceleation) {
        double timeToAccelerate = (double) speed / acceleration;
        double distanceDuringAcceleration = 0.5 * acceleration * Math.pow(timeToAccelerate, 2);

        double timeToDecelerate = (double) speed / deceleation;
        double distanceDuringDeceleration = 0.5 * deceleation * Math.pow(timeToDecelerate, 2);

        double remainingDistance = movement - (distanceDuringAcceleration + distanceDuringDeceleration);
        double timeAtConstantSpeed = remainingDistance / speed;

        double totalTime = timeToAccelerate + timeAtConstantSpeed + timeToDecelerate;

        return (long) (totalTime * 1000);
    }

    /**
     * Initialize the device by running the homing routine and enabling error checks.
     * Initialization is performed in a separate thread and returns a Future<Boolean>
     * that signals the success of the operation.
     *
     * @return Future returning true if initialization succeeded, false otherwise
     * @throws RejectedExecutionException if the task cannot be submitted to the executor
     */
    public Future<Boolean> initialize() throws RejectedExecutionException {
        return executorService.submit(() -> {
            try {
                if (isShutdown()) return false;
                connection.logInfo("Initializing device");
                setEnabled(false);
                clearErrors();
                setEnabled(true);
                isMoving.set(true);
                connection.writeInteger(INITIALIZATION, 1);
                long homingTimeout = 12000;
                do {
                    if (!isMoving.get()) return false;
                    Thread.sleep(300);
                    homingTimeout -= 300;
                    if (homingTimeout <= 0) return false;
                } while (!hasInitialized());
                isInitialized.set(true);
                isStopped.set(false);
                isMoving.set(false);
                targetPosition.set(0);
                currentPosition.set(0);
                notifyStateChange();
                setupErrorListener();
                return true;
            } catch (Exception e) {
                connection.logError("Error initializing device: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Set absolute target position by writing the dedicated register.
     *
     * @param position absolute target position (device units)
     * @throws IOException if the register write fails
     */
    public void moveAbsolutePosition(int position) throws IOException {
        if (!canMove()) throw new IOException("Device not initialized");
        connection.writeInteger(TARGET_POSITION, position);
    }

    /**
     * Perform a relative move from the current position by writing the appropriate register.
     *
     * @param position relative displacement to apply (positive or negative depending on direction)
     * @throws IOException if the register write fails
     */
    public void moveRelativePosition(int position) throws IOException {
        if (!canMove()) throw new IOException("Device not initialized");
        connection.writeInteger(RELATIVE_POSITION, position);
    }


    /**
     * Clear motor faults by sending the clear fault command to the device.
     *
     * @throws IOException if the register write fails
     */
    public void clearErrors() throws IOException {
        connection.writeInteger(CLEAR_FAULT, 1);
    }

    /**
     * Move the device to the specified absolute position and wait for completion.
     * The operation runs in a separate thread; the Future returns true if the movement succeeds.
     *
     * @param position absolute target position
     * @return Future returning true if the movement completed successfully, false on error
     */
    public Future<Boolean> moveAbsolutePositionAndWait(int position) {
        return executorService.submit(() -> {
            try {
                connection.logInfo("Moving to position: " + position);
                targetPosition.set(position);
                isMoving.set(true);
                notifyStateChange();
                moveAbsolutePosition(position);
                waitReachedPosition();
                currentPosition.set(position);
                isMoving.set(false);
                notifyStateChange();
                return true;
            } catch (Exception e) {
                connection.logError("Error moving to position: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Move the device by a relative displacement and wait for completion.
     * The operation runs in a separate thread; the Future returns true if the movement succeeds.
     *
     * @param position relative displacement
     * @return Future returning true if the movement completed successfully, false on error
     */
    public Future<Boolean> moveRelativePositionAndWait(int position) {
        return executorService.submit(() -> {
            try {
                connection.logInfo("Moving to relative position: " + position);
                moveRelativePosition(position);
                waitReachedPosition();
                return true;
            } catch (Exception e) {
                connection.logError("Error moving to relative position: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Read error codes from the device and construct an {@link ErrorFlags} instance.
     *
     * @return an {@link ErrorFlags} object containing error flags/description
     * @throws IOException if the register read fails
     */
    public ErrorFlags getErrors() throws IOException {
        return new ErrorFlags(connection.readShort(FEEDBACK_ERROR_CODE));
    }
}
