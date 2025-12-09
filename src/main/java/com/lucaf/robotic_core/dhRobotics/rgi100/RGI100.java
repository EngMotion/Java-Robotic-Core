package com.lucaf.robotic_core.dhRobotics.rgi100;

import com.lucaf.robotic_core.dataInterfaces.impl.RegisterInterface;
import com.lucaf.robotic_core.motors.impl.MotorInterface;
import com.lucaf.robotic_core.Pair;
import com.lucaf.robotic_core.State;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import com.lucaf.robotic_core.utils.StateUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lucaf.robotic_core.dhRobotics.rgi100.Constants.*;

/**
 * Class that manages the RGI100_22 device (Gripper and Rotation)
 */
public class RGI100 extends MotorInterface {

    /**
     * The low-level register/communication interface used to talk with the device.
     */
    final RegisterInterface connection;

    /**
     * The initialization mode used when starting the device.
     */
    @Getter
    @Setter
    InitializationMode initializationMode = InitializationMode.CLAMPING_UNIDIRECTIONAL;

    /**
     * The executor service used to run grip-related asynchronous tasks.
     */
    private final ExecutorService executorServiceGrip = Executors.newFixedThreadPool(1);

    /**
     * The executor service used to run rotation-related asynchronous tasks.
     */
    private final ExecutorService executorServiceRotator = Executors.newFixedThreadPool(1);

    /**
     * The scheduled executor service used for periodic error/polling checks.
     */
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * Shared state map exposed to callers (may contain Atomic* values).
     */
    private final HashMap<String, Object> state;

    /**
     * Optional callback object used to notify state changes and errors.
     */
    private final State stateFunction;

    /**
     * Internal flag indicating whether the grip is performing a movement.
     */
    private final AtomicBoolean isMovingGrip = new AtomicBoolean(false);

    /**
     * Internal flag indicating whether the rotator is performing a movement.
     */
    private final AtomicBoolean isMovingRotator = new AtomicBoolean(false);

    /**
     * Current grip position (0 - 1000).
     */
    private final AtomicInteger currentPosition = new AtomicInteger(0);

    /**
     * Target grip position (0 - 1000).
     */
    private final AtomicInteger targetPosition = new AtomicInteger(0);

    /**
     * Target rotation angle (device-specific units).
     */
    private final AtomicInteger targetAngle = new AtomicInteger(0);

    /**
     * Current rotation angle (device-specific units).
     */
    private final AtomicInteger currentAngle = new AtomicInteger(0);

    /**
     * Constructor that initializes the RGI100_22 object with an existing RS485 connection
     * and optional external state map and state callback.
     *
     * @param registerInterface the low-level register/communication interface used to talk with the device
     * @param state             a shared state map that will be updated by this class (may be null or empty)
     * @param notifyStateChange optional State instance used to receive callbacks when state changes or errors occur
     */
    public RGI100(RegisterInterface registerInterface, HashMap<String, Object> state, State notifyStateChange) {
        this.connection = registerInterface;
        this.state = state;
        this.stateFunction = notifyStateChange;
        initState();
    }


    /**
     * Convenience constructor that accepts an existing state map but no callback.
     *
     * @param registerInterface the communication interface
     * @param state             the state map to use
     */
    public RGI100(RegisterInterface registerInterface, HashMap<String, Object> state) {
        this(registerInterface, state, null);
    }

    /**
     * Convenience constructor that creates an empty state map and no callback.
     *
     * @param registerInterface the communication interface
     */
    public RGI100(RegisterInterface registerInterface) {
        this(registerInterface, new HashMap<>(), null);
    }

    /**
     * Notifies the registered State callback (if any) about a state change.
     */
    void notifyStateChange() {
        if (stateFunction != null)
            notifyStateChange();
    }

    /**
     * Initializes and populates the internal and shared state map.
     * Existing values present in the provided state map are respected when possible.
     */
    void initState() {
        // Check if the state already contains the moving grip and rotator states
        if (state.containsKey("is_moving_grip")) {
            isMovingGrip.set(StateUtils.getBoolean(state.get("is_moving_grip")));
        }
        state.put("is_moving_grip", isMovingGrip);
        if (state.containsKey("is_moving_rotator")) {
            isMovingRotator.set(StateUtils.getBoolean(state.get("is_moving_rotator")));
        }
        state.put("is_moving_rotator", isMovingRotator);
        if (state.containsKey("is_moving")) {
            isMoving.set(StateUtils.getBoolean(state.get("is_moving")));
        }
        state.put("is_moving", isMoving);
        if (state.containsKey("current_position")) {
            currentPosition.set(StateUtils.getInt(state.get("current_position")));
        }
        state.put("current_position", currentPosition);
        if (state.containsKey("target_position")) {
            targetPosition.set(StateUtils.getInt(state.get("target_position")));
        }
        state.put("target_position", targetPosition);
        if (state.containsKey("current_angle")) {
            currentAngle.set(StateUtils.getInt(state.get("current_angle")));
        }
        state.put("current_angle", currentAngle);
        if (state.containsKey("target_angle")) {
            targetAngle.set(StateUtils.getInt(state.get("target_angle")));
        }
        state.put("target_angle", targetAngle);
        // Create the initialized state
        state.put("is_initialized", isInitialized);
        state.put("has_fault", hasError);
        state.put("fault", "");

        notifyStateChange();
    }

    /**
     * Internal method that periodically polls the device for error flags and notifies the
     * provided State callback when an error is detected.
     * <p>
     * This method starts/resets the internal scheduled executor used for polling.
     */
    private void setupErrorListener() {
        connection.logDebug("Setting up error listener");
        if (scheduledExecutorService != null) {
            connection.logDebug("Shutting down previous error listener");
            scheduledExecutorService.shutdownNow();
        }
        connection.logDebug("Setting up new error listener");
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (scheduledExecutorService == null || scheduledExecutorService.isShutdown()) return;
            try {
                connection.logDebug("Checking for errors");
                ErrorFlags errorFlags = getErrorFlags();
                connection.logDebug("Error flags: " + errorFlags.getErrorDescription());
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
     * Stops device movement and signals the hardware to stop.
     * This method will also update internal state flags.
     *
     * @throws IOException if communication with the device fails while sending the stop command
     */
    @Override
    public void stop() throws IOException {
        connection.logInfo("Stopping device");
        isMovingGrip.set(false);
        isMovingRotator.set(false);
        isStopped.set(true);
        connection.writeInteger(STOP, 1);
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
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
        }
        executorServiceGrip.shutdownNow();
        executorServiceRotator.shutdownNow();
        isShutdown.set(true);
        stop();
    }

    /**
     * Checks whether the grip subsystem completed its initialization routine on the device.
     *
     * @return true if the grip reports initialization complete; false otherwise.
     * @throws IOException if there is a communication error while reading the feedback register
     */
    public boolean hasGripInitialized() throws IOException {
        int response = connection.readShort(FEEDBACK_INITIALIZATION_GRIP_STATE);
        if (response == -1) {
            return false;
        }
        return response == 1;
    }

    /**
     * Checks whether the rotation subsystem completed its initialization routine on the device.
     *
     * @return true if the rotation reports initialization complete; false otherwise.
     * @throws IOException if there is a communication error while reading the feedback register
     */
    public boolean hasRotationInitialized() throws IOException {
        int response = connection.readShort(FEEDBACK_INITIALIZATION_ROTATION_STATE);
        if (response == -1) {
            return false;
        }
        return response == 1;
    }

    /**
     * Starts both grip and rotation initialization on the device and waits asynchronously
     * until both subsystems report ready.
     *
     * @return a Future completed with true if initialization succeeded, false otherwise
     */
    public Future<Boolean> initialize() throws RejectedExecutionException {
        return executorServiceGrip.submit(() -> {
            if (isShutdown()) return false;
            connection.logInfo("Initializing device");
            isMovingGrip.set(true);
            isMovingRotator.set(true);
            try {
                connection.writeInteger(INITIALIZATION, initializationMode.getCode());
                long homingTimeout = 12000;
                do {
                    if (!isMovingGrip.get() || !isMovingRotator.get()) return false;
                    Thread.sleep(300);
                    homingTimeout -= 300;
                    if (homingTimeout <= 0) return false;
                } while (!hasGripInitialized() || !hasRotationInitialized());
                connection.logDebug("Device initialized");
                isInitialized.set(true);
                targetAngle.set(0);
                targetPosition.set(0);
                currentAngle.set(0);
                currentPosition.set(0);
                isMovingGrip.set(false);
                isMovingRotator.set(false);
                isStopped.set(false);
                notifyStateChange();
                setupErrorListener();
                return true;
            } catch (Exception e) {
                connection.logError("[RGI100_22] Error initializing device: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Move the rotator to an absolute angle value.
     *
     * @param angle absolute angle target (device-specific units)
     * @throws IOException if the device is not initialized or communication fails
     */
    public void moveToAbsoluteAngle(long angle) throws IOException {
        if (!canMove()) throw new IOException("Device not initialized");
        connection.writeSignedLong(ABSOLUTE_ROTATION, angle, true);
    }

    /**
     * Move the rotator by a relative offset from its current position.
     *
     * @param angle relative angle offset (positive or negative), device-specific units
     * @throws IOException if the device is not initialized or communication fails
     */
    public void moveToRelativeAngle(int angle) throws IOException {
        if (!canMove()) throw new IOException("Device not initialized");
        if (Math.abs(angle) > 32767) {
            throw new IOException("Relative angle out of range (-32767 to 32767)");
        }
        connection.writeInteger(RELATIVE_ROTATION, angle);
    }

    /**
     * Reads the rotation movement feedback register and translates it to a {@link PositionFeedback}.
     *
     * @return the current rotation feedback state
     * @throws IOException if reading the feedback register fails
     */
    public PositionFeedback isRotationMoving() throws IOException {
        int response = connection.readShort(FEEDBACK_ROTATION_STATE);
        if (response == -1) {
            return PositionFeedback.MOVING;
        }
        return switch (response) {
            case 0 -> PositionFeedback.MOVING;
            case 1 -> PositionFeedback.REACHED;
            case 2 -> PositionFeedback.BLOCKED;
            default -> PositionFeedback.MOVING;
        };
    }

    /**
     * Reads the grip movement feedback register and translates it to a {@link PositionFeedback}.
     *
     * @return the current grip feedback state
     * @throws IOException if reading the feedback register fails
     */
    public PositionFeedback isGripMoving() throws IOException {
        int response = connection.readShort(FEEDBACK_GRIP_STATE);
        if (response == -1) {
            return PositionFeedback.MOVING;
        }
        return switch (response) {
            case 0 -> PositionFeedback.MOVING;
            case 1 -> PositionFeedback.REACHED_WITHOUT_OBJ;
            case 2 -> PositionFeedback.REACHED_WITH_OBJ;
            case 3 -> PositionFeedback.FALL;
            default -> PositionFeedback.MOVING;
        };
    }

    /**
     * Returns the current raw grip position read from the device.
     *
     * @return grip position (0..1000), or device-specific sentinel values if reported by hardware
     * @throws IOException if reading the register fails
     */
    public int getGripPosition() throws IOException {
        return connection.readShort(FEEDBACK_GRIP_POSITION);
    }

    /**
     * Blocks the calling thread until the rotator reaches its target or an error/blocked state occurs.
     * This method periodically polls the device and will throw an IOException when the device reports
     * that it stopped moving unexpectedly or when a hardware fault has been detected.
     *
     * @param timeout maximum time to wait in milliseconds (negative value means infinite)
     * @return the final {@link PositionFeedback} state (REACHED or BLOCKED)
     * @throws IOException if the device is not moving, a fault occurred, or communication fails
     */
    public PositionFeedback waitEndPosition(long timeout) throws IOException {
        isMovingRotator.set(true);
        long startTime = System.currentTimeMillis();
        PositionFeedback feedback;
        do {
            if (timeout > 0 && (System.currentTimeMillis() - startTime) > timeout) {
                connection.logWarning("waitEndPosition timed out");
                throw new IOException("Timeout waiting for end position");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (!isMovingRotator.get()) {
                connection.logWarning("Device is not moving but waitEndPosition was called");
                throw new IOException("Device is not moving");
            }
            if (hasError.get()) {
                connection.logWarning("Device has fault but waitEndPosition was called");
                throw new IOException("Device has fault");
            }
            feedback = isRotationMoving();
        } while (feedback != PositionFeedback.REACHED && feedback != PositionFeedback.BLOCKED);
        isMovingRotator.set(false);
        return feedback;
    }

    /**
     * Blocks the calling thread until the rotator reaches its target or an error/blocked state occurs.
     * This method periodically polls the device and will throw an IOException when the device reports
     * that it stopped moving unexpectedly or when a hardware fault has been detected.
     *
     * @return the final {@link PositionFeedback} state (REACHED or BLOCKED)
     * @throws IOException if the device is not moving, a fault occurred, or communication fails
     */
    public PositionFeedback waitEndPosition() throws IOException {
        return waitEndPosition(-1);
    }

    /**
     * Blocks the calling thread until the grip movement completes. This method polls the hardware
     * and will throw a {@link DeviceCommunicationException} when the device stops unexpectedly or a fault
     * is detected.
     *
     * @param timeout maximum time to wait in milliseconds (negative value means infinite)
     * @return the final {@link PositionFeedback} state (REACHED_WITH_OBJ, REACHED_WITHOUT_OBJ or FALL)
     * @throws IOException                  if a low-level I/O error occurs while communicating with the device
     * @throws DeviceCommunicationException if the device reports it is not moving or a fault occurred
     */
    public PositionFeedback waitEndGrip(long timeout) throws IOException {
        isMovingGrip.set(true);
        long startTime = System.currentTimeMillis();
        PositionFeedback feedback;
        do {
            if (timeout > 0 && (System.currentTimeMillis() - startTime) > timeout) {
                connection.logWarning("waitEndGrip timed out");
                throw new DeviceCommunicationException("Timeout waiting for end grip");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (!isMovingGrip.get()) {
                connection.logWarning("Device is not moving but waitEndGrip was called");
                throw new DeviceCommunicationException("Device is not moving");
            }
            if (hasError.get()) {
                connection.logWarning("Device has fault but waitEndGrip was called");
                throw new DeviceCommunicationException("Device has fault");
            }
            feedback = isGripMoving();
        } while (feedback != PositionFeedback.REACHED_WITH_OBJ && feedback != PositionFeedback.REACHED_WITHOUT_OBJ && feedback != PositionFeedback.FALL);
        isMovingGrip.set(false);
        return feedback;
    }

    /**
     * Blocks the calling thread until the grip movement completes. This method polls the hardware
     * and will throw a {@link DeviceCommunicationException} when the device stops unexpectedly or a fault
     * is detected.
     *
     * @return the final {@link PositionFeedback} state (REACHED_WITH_OBJ, REACHED_WITHOUT_OBJ or FALL)
     * @throws IOException                  if a low-level I/O error occurs while communicating with the device
     * @throws DeviceCommunicationException if the device reports it is not moving or a fault occurred
     */
    public PositionFeedback waitEndGrip() throws IOException {
        return waitEndGrip(-1);
    }

    /**
     * Moves the rotator by a relative angle and waits for completion asynchronously.
     *
     * @param angle relative angle offset (device-specific units)
     * @return a Future containing a Pair where the first element indicates success, and the second the final feedback state
     */
    public Future<Pair<Boolean, PositionFeedback>> moveToRelativeAngleAndWait(int angle) {
        return executorServiceRotator.submit(() -> {
            try {
                if (!canMove()) return new Pair<>(false, PositionFeedback.ERROR);
                connection.logDebug("Moving to relative angle: " + angle);
                int newAngle = angle + currentAngle.get();
                targetAngle.set(newAngle);
                isMovingRotator.set(true);
                isMoving.set(true);
                notifyStateChange();
                moveToRelativeAngle(angle);
                PositionFeedback feedback = waitEndPosition();
                currentAngle.set(newAngle);
                connection.logDebug("Moved to relative angle: " + angle);
                return new Pair<>(true, feedback);
            } catch (Exception e) {
                connection.logError("Error moving to relative angle: " + e.getMessage());
                return new Pair<>(false, PositionFeedback.ERROR);
            } finally {
                isMovingRotator.set(false);
                isMoving.set(false);
                notifyStateChange();
            }
        });
    }

    /**
     * Moves the rotator to an absolute angle and waits for completion asynchronously.
     *
     * @param angle absolute target angle (device-specific units)
     * @return a Future containing a Pair where the first element indicates success, and the second the final feedback state
     */
    public Future<Pair<Boolean, PositionFeedback>> moveToAbsoluteAngleAndWait(int angle) {
        return executorServiceRotator.submit(() -> {
            if (!canMove()) return new Pair<>(false, PositionFeedback.ERROR);
            try {
                connection.logDebug("Moving to absolute angle: " + angle);
                targetAngle.set(angle);
                isMovingRotator.set(true);
                isMoving.set(true);
                notifyStateChange();
                moveToAbsoluteAngle(angle);
                PositionFeedback feedback = waitEndPosition();
                currentAngle.set(angle);
                connection.logDebug("Moved to absolute angle: " + angle);
                return new Pair<>(true, feedback);
            } catch (Exception e) {
                connection.logError("Error moving to absolute angle: " + e.getMessage());
                return new Pair<>(false, PositionFeedback.ERROR);
            } finally {
                isMoving.set(false);
                isMovingRotator.set(false);
                notifyStateChange();
            }
        });
    }

    /**
     * Sets the grip target position. Accepted range is clamped to [0, 1000].
     *
     * @param position target grip position (0 = fully open, 1000 = fully closed; device-specific mapping)
     * @throws IOException if the device is not initialized or writing the register fails
     */
    public void setGripPosition(int position) throws IOException {
        if (!canMove()) throw new IOException("Device not initialized");
        position = Math.max(0, position);
        position = Math.min(1000, position);
        connection.writeInteger(TARGET_POSITION, position);
    }

    /**
     * Sets the grip target position and waits for movement completion asynchronously.
     *
     * @param position target grip position (0..1000)
     * @return a Future containing a Pair where the first element indicates success, and the second the final feedback state
     */
    public Future<Pair<Boolean, PositionFeedback>> setGripPositionAndWait(int position) {
        return executorServiceGrip.submit(() -> {
            try {
                if (!canMove()) return new Pair<>(false, PositionFeedback.ERROR);
                connection.logDebug("Setting grip position: " + position);
                targetPosition.set(position);
                isMoving.set(true);
                isMovingGrip.set(true);
                notifyStateChange();
                setGripPosition(position);
                PositionFeedback feedback = waitEndGrip();
                currentPosition.set(position);
                connection.logDebug("Set grip position: " + position);
                return new Pair<>(true, feedback);
            } catch (Exception e) {
                connection.logError("Error setting grip position: " + e.getMessage());
                return new Pair<>(false, PositionFeedback.ERROR);
            } finally {
                isMoving.set(false);
                isMovingGrip.set(false);
                notifyStateChange();
            }
        });
    }

    /**
     * Reads the configured grip force percentage from the device.
     *
     * @return grip force value in the range 0..100
     * @throws IOException if reading the register fails
     */
    public int getGripForce() throws IOException {
        return connection.readShort(FORCE);
    }

    /**
     * Sets the grip force percentage. Values are clamped to the range 0..100.
     *
     * @param force desired grip force (0..100)
     * @throws IOException if writing the register fails
     */
    public void setGripForce(int force) throws IOException {
        force = Math.max(20, force);
        force = Math.min(100, force);
        connection.writeInteger(FORCE, force);
    }

    /**
     * Reads the configured grip speed percentage from the device.
     *
     * @return grip speed value in the range 0..100
     * @throws IOException if reading the register fails
     */
    public int getGripSpeed() throws IOException {
        return connection.readShort(SPEED);
    }

    /**
     * Sets the grip speed percentage. Values are clamped to the range 0..100.
     *
     * @param speed desired grip speed (0..100)
     * @throws IOException if writing the register fails
     */
    public void setGripSpeed(int speed) throws IOException {
        speed = Math.min(100, speed);
        speed = Math.max(1, speed);
        connection.writeInteger(SPEED, speed);
    }

    /**
     * Changes the device Modbus/communication address (slave id).
     *
     * @param id new device address (slave id)
     * @return true if the write succeeded, false otherwise
     * @throws IOException if writing the register fails
     */
    public boolean changeAddress(int id) throws IOException {
        return connection.writeInteger(SLAVE_ADDRESS, id);
    }

    /**
     * Reads the current device Modbus/communication address (slave id).
     *
     * @return current device address (slave id)
     * @throws IOException if reading the register fails
     */
    public int getAddress() throws IOException {
        return connection.readShort(SLAVE_ADDRESS);
    }

    /**
     * Gets the configured rotation speed percentage from the device.
     *
     * @return rotation speed value in the range 0..100
     * @throws IOException if reading the register fails
     */
    public int getRotationSpeed() throws IOException {
        return connection.readShort(ROTATION_SPEED);
    }

    /**
     * Sets the rotation speed percentage. Values are clamped to the range 0..100.
     *
     * @param speed desired rotation speed (0..100)
     * @throws IOException if writing the register fails
     */
    public void setRotationSpeed(int speed) throws IOException {
        speed = Math.max(1, speed);
        speed = Math.min(100, speed);
        connection.writeInteger(ROTATION_SPEED, speed);
    }

    /**
     * Reads the configured rotation force percentage from the device.
     *
     * @return rotation force value in the range 0..100
     * @throws IOException if reading the register fails
     */
    public int getRotationForce() throws IOException {
        return connection.readShort(ROTATION_FORCE);
    }

    /**
     * Sets the rotation force percentage. Values are clamped to the range 0..100.
     *
     * @param force desired rotation force (0..100)
     * @throws IOException if writing the register fails
     */
    public void setRotationForce(int force) throws IOException {
        force = Math.max(20, force);
        force = Math.min(100, force);
        connection.writeInteger(ROTATION_FORCE, force);
    }

    /**
     * Persists the current configuration into device non-volatile memory.
     * Note: some transient parameters (speed, force, position) may not be preserved on power-cycle depending on device firmware.
     *
     * @param defaults if true, restore factory defaults; if false, save current configuration
     * @return true if the operation was accepted by the device, false otherwise
     * @throws IOException if writing the register fails
     */
    public boolean saveConfig(boolean defaults) throws IOException {
        return connection.writeInteger(SAVE_CONFIG, defaults ? 0 : 1);
    }

    /**
     * Persists the current configuration into device non-volatile memory (no defaults).
     *
     * @return true if the operation was accepted by the device, false otherwise
     * @throws IOException if writing the register fails
     */
    public boolean saveConfig() throws IOException {
        return saveConfig(false);
    }

    /**
     * Reads and returns the device error flags wrapper.
     *
     * @return an {@link ErrorFlags} instance representing current device errors
     * @throws IOException if reading the error register fails
     */
    public ErrorFlags getErrorFlags() throws IOException {
        int response = connection.readShort(FEEDBACK_GRIP_ERROR_CODE);
        return new ErrorFlags(response);
    }

    /**
     * Clears the device error state. Implementation is currently not provided; this method logs a warning.
     *
     * @throws IOException if clearing errors requires I/O and fails (not implemented)
     */
    public void clearError() throws IOException {
        connection.logWarning("Clearing error not implemented");
    }
}
