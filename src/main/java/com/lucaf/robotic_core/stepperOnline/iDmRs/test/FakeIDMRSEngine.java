package com.lucaf.robotic_core.stepperOnline.iDmRs.test;

import com.lucaf.robotic_core.dataInterfaces.test.MockedRegisterInterface;
import com.lucaf.robotic_core.stepperOnline.iDmRs.ControlMode;
import com.lucaf.robotic_core.stepperOnline.iDmRs.ControlType;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.lucaf.robotic_core.stepperOnline.iDmRs.Constants.*;

/**
 * Fake IDMRS engine that simulates motor responses for acceptance testing.
 * <p>
 * Intercepts writes to the STATUS_MODE register and simulates realistic motor
 * movement timing by scheduling a transition from the "running" state (0x0100)
 * to the "complete" state (0x0000) after a configurable or calculated delay.
 * <p>
 * Usage:
 * <pre>
 *     FakeIDMRSEngine engine = new FakeIDMRSEngine(1, "motor");
 *     engine.setMovementDurationMs(200); // optional: use fixed 200 ms per move
 *     IDMRS motor = new IDMRS(engine, new HashMap<>());
 *     motor.initialize().get();
 *     motor.setPositioningMode();
 *     motor.moveToPositionAndWait(5000, 5000).get();
 *     engine.dispose();
 * </pre>
 */
public class FakeIDMRSEngine extends MockedRegisterInterface {

    /** Scheduler used to simulate asynchronous movement completion. */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /** Currently active movement task, or null. */
    private volatile ScheduledFuture<?> movementTask;

    /** Tracks the simulated current position of the motor (in pulses). */
    private final AtomicLong simulatedCurrentPosition = new AtomicLong(0);

    /**
     * Fixed movement duration in milliseconds used for all positioning moves.
     * A value of {@code -1} means the duration is calculated from the current
     * speed, acceleration and deceleration register values.
     */
    private long movementDurationMs = -1;

    /** Duration of the simulated homing operation in milliseconds. */
    private long homingDurationMs = 500;

    public FakeIDMRSEngine(int unitId, String name) {
        super(unitId, name);
    }

    /**
     * Intercepts STATUS_MODE writes to simulate motor movement.
     * All other register writes are delegated to the parent implementation.
     */
    @Override
    public boolean writeInteger(byte[] register, int data) throws IOException {
        int statusModeAddr = registerToInt(STATUS_MODE);
        if (registerToInt(register) == statusModeAddr) {
            return handleStatusModeWrite(data);
        }
        return super.writeInteger(register, data);
    }

    private boolean handleStatusModeWrite(int data) throws IOException {
        // Emergency stop (0x40): cancel any in-progress movement and mark as complete.
        if (data == 0x40) {
            cancelMovement();
            return super.writeInteger(STATUS_MODE, 0x0000);
        }

        // Homing (0x20): simulate the homing sequence.
        if (data == 0x20) {
            startHoming();
            return true;
        }

        // Segment positioning command (0x10–0x1F): simulate a positioning move.
        if (data >= 0x10 && data <= 0x1F) {
            startPositioningMovement();
            return true;
        }

        return super.writeInteger(STATUS_MODE, data);
    }

    /**
     * Simulates the homing sequence.
     * Immediately marks the motor as running and completes after
     * {@link #homingDurationMs} milliseconds, resetting the position to zero.
     */
    private void startHoming() throws IOException {
        cancelMovement();
        super.writeInteger(STATUS_MODE, 0x0100); // running
        movementTask = scheduler.schedule(() -> {
            try {
                super.writeInteger(STATUS_MODE, 0x0000); // complete
                simulatedCurrentPosition.set(0);
            } catch (IOException ignored) {
            }
        }, homingDurationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Simulates a positioning or velocity-mode movement.
     *
     * <p>In <em>velocity mode</em> the motor is marked as running when the
     * velocity register is non-zero, and as complete (stopped) when it is zero.
     *
     * <p>In <em>position mode</em> the motor is marked as running and a
     * scheduled task transitions it to complete after the calculated (or fixed)
     * movement duration.
     */
    private void startPositioningMovement() throws IOException {
        int controlModeValue = 0;
        try {
            controlModeValue = super.readInteger(CONTROL_MODE);
        } catch (Exception ignored) {
        }
        ControlMode controlMode = new ControlMode(controlModeValue);

        if (controlMode.getCONTROL_MODE() == ControlType.VELOCITY_MODE.getValue()) {
            int velocity = 0;
            try {
                velocity = super.readInteger(VELOCITY);
            } catch (Exception ignored) {
            }
            super.writeInteger(STATUS_MODE, velocity != 0 ? 0x0100 : 0x0000);
            return;
        }

        // Position mode: schedule transition to complete after the movement time.
        long relativeOrAbsolutePos = 0;
        try {
            relativeOrAbsolutePos = super.readSignedLong(TARGET_POSITION_HIGH, false);
        } catch (Exception ignored) {
        }
        boolean isRelative = controlMode.isRELATIVE_POSITIONING();
        long targetPos = isRelative
                ? simulatedCurrentPosition.get() + relativeOrAbsolutePos
                : relativeOrAbsolutePos;
        long currentPos = simulatedCurrentPosition.get();
        int distance = (int) Math.abs(targetPos - currentPos);
        long durationMs = (movementDurationMs >= 0)
                ? movementDurationMs
                : calculateMovementTimeMs(distance);

        cancelMovement();
        super.writeInteger(STATUS_MODE, 0x0100); // running

        final long finalTargetPos = targetPos;
        movementTask = scheduler.schedule(() -> {
            try {
                super.writeInteger(STATUS_MODE, 0x0000); // complete
                simulatedCurrentPosition.set(finalTargetPos);
            } catch (IOException ignored) {
            }
        }, Math.max(durationMs, 50), TimeUnit.MILLISECONDS);
    }

    /**
     * Estimates the time required to travel {@code distance} pulses using the
     * current speed, acceleration and deceleration register values.
     * Uses the same trapezoidal profile algorithm as
     * {@code IDMRS#estimateArrivalTime}.
     *
     * @param distance absolute distance in pulses
     * @return estimated movement time in milliseconds
     */
    private long calculateMovementTimeMs(int distance) {
        int speed = 0, acceleration = 0, deceleration = 0;
        try {
            speed = super.readInteger(VELOCITY);
            acceleration = super.readInteger(ACCELERATION);
            deceleration = super.readInteger(DECELERATION);
        } catch (Exception ignored) {
        }
        if (speed == 0 || acceleration == 0 || deceleration == 0) {
            return 500;
        }
        int timeToReachSpeed = speed / acceleration;
        int timeToStop = speed / deceleration;
        int distanceToReachSpeed = (speed * timeToReachSpeed) / 2;
        int distanceToStop = (speed * timeToStop) / 2;
        int distanceToCover = distance - distanceToReachSpeed - distanceToStop;
        int seconds;
        if (distanceToCover < 0) {
            seconds = (int) Math.ceil(Math.sqrt((2.0 * distance) / acceleration));
        } else {
            // Integer division is intentional: mirrors the same approximation used in
            // IDMRS#estimateArrivalTime, which this method is designed to replicate.
            int timeAtConstantSpeed = distanceToCover / speed;
            seconds = timeToReachSpeed + timeAtConstantSpeed + timeToStop;
        }
        return (long) seconds * 1000;
    }

    private void cancelMovement() {
        if (movementTask != null && !movementTask.isDone()) {
            movementTask.cancel(false);
        }
    }

    /**
     * Sets a fixed movement duration for all positioning moves.
     * Pass {@code -1} to revert to the calculated duration based on register values.
     *
     * @param durationMs fixed movement duration in milliseconds, or {@code -1}
     */
    public void setMovementDurationMs(long durationMs) {
        this.movementDurationMs = durationMs;
    }

    /**
     * Sets the simulated homing duration.
     *
     * @param durationMs homing duration in milliseconds
     */
    public void setHomingDurationMs(long durationMs) {
        this.homingDurationMs = durationMs;
    }

    /**
     * Returns the simulated current position of the motor in pulses.
     * Updated automatically when a positioning move completes.
     *
     * @return current simulated position
     */
    public long getSimulatedCurrentPosition() {
        return simulatedCurrentPosition.get();
    }

    /**
     * Cancels any pending movement task and shuts down the internal scheduler.
     * Should be called in test tear-down to prevent thread leaks.
     */
    public void dispose() {
        cancelMovement();
        scheduler.shutdownNow();
    }
}
