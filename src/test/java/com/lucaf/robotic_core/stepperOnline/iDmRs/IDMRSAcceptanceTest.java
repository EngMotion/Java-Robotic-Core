package com.lucaf.robotic_core.stepperOnline.iDmRs;

import com.lucaf.robotic_core.stepperOnline.iDmRs.test.FakeIDMRSEngine;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Acceptance tests for IDMRS using {@link FakeIDMRSEngine}.
 * <p>
 * Each test exercises a realistic end-to-end scenario: the motor is driven
 * through the same API that real production code would use, while the
 * {@link FakeIDMRSEngine} simulates the timing responses of the physical
 * device.
 */
class IDMRSAcceptanceTest {

    /** Fixed movement duration used across most tests so they run quickly. */
    private static final long MOVE_DURATION_MS = 200;
    /** Fixed homing duration used across most tests. */
    private static final long HOMING_DURATION_MS = 300;

    private FakeIDMRSEngine engine;
    private IDMRS motor;
    private HashMap<String, Object> state;

    @BeforeEach
    void setUp() {
        engine = new FakeIDMRSEngine(1, "FakeMotor");
        engine.setMovementDurationMs(MOVE_DURATION_MS);
        engine.setHomingDurationMs(HOMING_DURATION_MS);
        state = new HashMap<>();
        motor = new IDMRS(engine, state);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (!motor.isShutdown()) {
            motor.shutdown();
        }
        engine.dispose();
    }

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Motor initialises successfully without homing")
    void testInitializeWithoutHoming() throws Exception {
        assertTrue(motor.initialize().get(), "initialize() should return true");
        assertTrue(motor.isInitialized(), "motor should be marked as initialized");
        assertFalse(motor.isMoving(), "motor should not be moving after initialization");
    }

    @Test
    @DisplayName("Motor initialises successfully with homing")
    void testInitializeWithHoming() throws Exception {
        HomingControl homing = new HomingControl();
        homing.setHomingMethod(HomingMethod.HOMING_SWITCH);
        homing.setHomingTimeout(5000);
        motor.setHomingControl(homing);

        assertTrue(motor.initialize().get(), "initialize() with homing should return true");
        assertTrue(motor.isInitialized(), "motor should be marked as initialized after homing");
        assertEquals(0, engine.getSimulatedCurrentPosition(),
                "position should be reset to zero after homing");
    }

    @Test
    @DisplayName("Motor honours homing timeout when engine is unresponsive")
    void testHomingTimeout() throws Exception {
        engine.setHomingDurationMs(3000); // longer than the motor timeout
        HomingControl homing = new HomingControl();
        homing.setHomingMethod(HomingMethod.HOMING_SWITCH);
        homing.setHomingTimeout(500); // 500 ms → will fire before 3 s homing completes

        motor.setHomingControl(homing);
        assertFalse(motor.initialize().get(),
                "initialize() should return false when homing times out");
    }

    // -------------------------------------------------------------------------
    // Positioning mode
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Motor moves to an absolute position and waits for completion")
    void testMoveToAbsolutePosition() throws Exception {
        motor.initialize().get();
        motor.setPositioningMode();
        motor.setSpeed(1000);
        motor.setAcceleration(500);
        motor.setDeceleration(500);

        boolean result = motor.moveToPositionAndWait(10000, 5000).get();

        assertTrue(result, "moveToPositionAndWait should return true on success");
        assertEquals(10000, engine.getSimulatedCurrentPosition(),
                "simulated position should match the commanded target");
    }

    @Test
    @DisplayName("Motor executes multiple sequential absolute moves")
    void testSequentialAbsoluteMoves() throws Exception {
        motor.initialize().get();
        motor.setPositioningMode();
        motor.setSpeed(1000);
        motor.setAcceleration(500);
        motor.setDeceleration(500);

        int[] targets = {5000, 12000, 3000, 20000};
        for (int target : targets) {
            boolean result = motor.moveToPositionAndWait(target, 5000).get();
            assertTrue(result, "move to " + target + " should succeed");
            assertEquals(target, engine.getSimulatedCurrentPosition(),
                    "simulated position should be " + target + " after the move");
        }
    }

    @Test
    @DisplayName("Motor moves correctly in relative positioning mode")
    void testRelativePositioningMode() throws Exception {
        motor.initialize().get();
        motor.setPositioningMode();
        motor.setRelativePositioning(true);
        motor.setSpeed(1000);
        motor.setAcceleration(500);
        motor.setDeceleration(500);

        // First relative move: +5000
        assertTrue(motor.moveToPositionAndWait(5000, 5000).get());
        assertEquals(5000, engine.getSimulatedCurrentPosition());

        // Second relative move: +3000 → cumulative 8000
        assertTrue(motor.moveToPositionAndWait(3000, 5000).get());
        assertEquals(8000, engine.getSimulatedCurrentPosition());
    }

    @Test
    @DisplayName("Movement to a position times out when the engine is too slow")
    void testMoveToPositionTimeout() throws Exception {
        engine.setMovementDurationMs(2000); // 2 s – longer than the 500 ms timeout below
        motor.initialize().get();
        motor.setPositioningMode();

        boolean result = motor.moveToPositionAndWait(10000, 500).get();
        assertFalse(result, "moveToPositionAndWait should return false on timeout");
    }

    // -------------------------------------------------------------------------
    // Velocity mode
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Motor runs in velocity mode and stops when speed is set to zero")
    void testVelocityModeStartAndStop() throws Exception {
        motor.initialize().get();
        motor.setVelocityMode();

        motor.setSpeed(2000);
        assertTrue(motor.isMoving(), "motor should be moving after setSpeed in velocity mode");

        motor.setSpeed(0);
        assertFalse(motor.isMoving(), "motor should stop when speed is set to 0 in velocity mode");
    }

    // -------------------------------------------------------------------------
    // Emergency stop
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Emergency stop interrupts a running position move")
    void testEmergencyStopDuringMove() throws Exception {
        engine.setMovementDurationMs(3000); // 3 s move – long enough to stop mid-move
        motor.initialize().get();
        motor.setPositioningMode();
        motor.setSpeed(1000);
        motor.setAcceleration(500);
        motor.setDeceleration(500);

        // Start the move asynchronously and stop after 200 ms.
        // The 5000 ms timeout acts as a safety net in case the stop mechanism fails.
        var moveFuture = motor.moveToPositionAndWait(50000, 5000);
        Thread.sleep(200);
        motor.stop();

        // The move should fail (stop() sets isMoving = false → waitReachedPosition throws)
        assertFalse(moveFuture.get(), "move should fail after emergency stop");
    }

    // -------------------------------------------------------------------------
    // Full configuration workflow
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Motor can be configured via IDMRSConfig and then performs a move")
    void testApplyConfigAndMove() throws Exception {
        TravelParameters tp = new TravelParameters(1000, 500, 500, 0);
        HomingConfig homing = new HomingConfig();
        homing.setEnabled(false);

        IDMRSConfig config = new IDMRSConfig();
        config.setTravelParameters(tp);
        config.setHoming(homing);
        config.setPositioningMode(true);
        config.setRelativePositioning(false);

        motor.applyConfig(config);
        motor.initialize().get();

        boolean result = motor.moveToPositionAndWait(8000, 5000).get();
        assertTrue(result, "move after applying config should succeed");
        assertEquals(8000, engine.getSimulatedCurrentPosition());
    }

    @Test
    @DisplayName("Full workflow: homing calibration followed by a production move")
    void testHomingCalibrationThenProductionMove() throws Exception {
        // Step 1: configure homing
        HomingControl homing = new HomingControl();
        homing.setHomingMethod(HomingMethod.HOMING_SWITCH);
        homing.setHomingTimeout(5000);
        motor.setHomingControl(homing);

        // Step 2: initialise (includes homing)
        assertTrue(motor.initialize().get(), "initialization with homing should succeed");
        assertEquals(0, engine.getSimulatedCurrentPosition(),
                "position should be zero after homing");

        // Step 3: configure travel parameters and perform a production move
        motor.setPositioningMode();
        motor.setSpeed(1000);
        motor.setAcceleration(500);
        motor.setDeceleration(500);

        assertTrue(motor.moveToPositionAndWait(15000, 5000).get(),
                "production move after homing should succeed");
        assertEquals(15000, engine.getSimulatedCurrentPosition());
    }

    @Test
    @DisplayName("Motor shuts down gracefully after completing a move")
    void testGracefulShutdown() throws Exception {
        motor.initialize().get();
        motor.setPositioningMode();
        motor.setSpeed(1000);
        motor.setAcceleration(500);
        motor.setDeceleration(500);

        assertTrue(motor.moveToPositionAndWait(5000, 5000).get());
        motor.shutdown();

        assertTrue(motor.isShutdown(), "motor should be marked as shut down");
        assertThrows(IOException.class, () -> motor.setPosition(100),
                "any command after shutdown should throw IOException");
    }
}
