package com.lucaf.robotic_core.dhRobotics.sacN;

import com.lucaf.robotic_core.mock.dataInterface.MockedRegisterInterface;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;

import static com.lucaf.robotic_core.dhRobotics.sacN.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

public class SACNTest {
    final HashMap<String, Object> state = new HashMap<>();
    final MockedRegisterInterface connection = new MockedRegisterInterface(1, "SAC_N Test Motor");
    final SACN motor = new SACN(
            connection,
            state
    );

    @Test
    public void testState() {
        assertTrue(state.containsKey("is_moving"));
        assertTrue(state.containsKey("current_position"));
        assertTrue(state.containsKey("target_position"));
        assertTrue(state.containsKey("is_initialized"));
        assertTrue(state.containsKey("has_fault"));
        assertTrue(state.containsKey("fault"));
    }

    void initializeMotor() throws Exception {
        Status status = new Status();
        status.setBackHome(true);
        connection.writeInteger(STATUS, status.toCode());

        motor.initialize().get();
    }

    @Test
    public void testInitialization() throws Exception {
        assertThrows(IOException.class, () -> motor.moveRelativePosition(10));
        initializeMotor();
        assertTrue(motor.isInitialized());
        assertDoesNotThrow(() -> motor.moveRelativePosition(10));
    }

    @Test
    public void testErrorHandling() throws Exception {
        initializeMotor();
        connection.writeInteger(FEEDBACK_ERROR_CODE, 4); // Example error code
        Thread.sleep(2000);
        assertTrue(motor.hasError());
        assertEquals("Unknown error: 4", state.get("fault"));

        connection.writeInteger(FEEDBACK_ERROR_CODE, 0); // Clear error
        Thread.sleep(2000);
        assertFalse(motor.hasError());
        assertEquals("", state.get("fault"));
    }

    @Test
    public void testStop() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> motor.moveRelativePosition(50));
        motor.stop();
        assertTrue(motor.isStopped());
        assertThrows(IOException.class, () -> motor.moveRelativePosition(10));

        initializeMotor();
        assertDoesNotThrow(() -> motor.moveRelativePosition(10));
    }

    @Test
    public void testShutdown() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> motor.moveRelativePosition(50));
        motor.shutdown();
        assertTrue(motor.isShutdown());
        assertThrows(IOException.class, () -> motor.moveRelativePosition(10));
        assertThrows(RejectedExecutionException.class, () -> motor.initialize().get());
    }

    @Test
    public void testWaitReachPosition() throws Exception {
        initializeMotor();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        connection.writeInteger(FEEDBACK_MOTION_STATE, 0);
        executor.schedule(() -> {
            try {
                connection.writeInteger(FEEDBACK_MOTION_STATE, 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 2, java.util.concurrent.TimeUnit.SECONDS);
        assertDoesNotThrow(() -> motor.waitReachedPosition());
    }

    @Test
    public void testWaitReachPositionFail() throws Exception {
        initializeMotor();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        connection.writeInteger(FEEDBACK_MOTION_STATE, 0);
        executor.schedule(() -> {
            try {
                motor.stop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 2, java.util.concurrent.TimeUnit.SECONDS);
        assertThrows(IOException.class, motor::waitReachedPosition);
    }
    @Test
    public void testWaitReachPositionTimeout() throws Exception {
        initializeMotor();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        connection.writeInteger(FEEDBACK_MOTION_STATE, 0);
        executor.schedule(() -> {
            try {
                motor.stop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 2, java.util.concurrent.TimeUnit.SECONDS);
        assertThrows(IOException.class, () -> motor.waitReachedPosition(1000) );
    }

    @Test
    public void testMoveRelativePosition() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> {
            motor.moveRelativePosition(90);
        });
        assertEquals(90, connection.readShort(RELATIVE_POSITION));
        assertDoesNotThrow(() -> {
            motor.moveRelativePosition(-450);
        });
        assertEquals(-450, connection.readShort(RELATIVE_POSITION));
    }

    @Test
    public void testMoveAbsolutePosition() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> {
            motor.moveAbsolutePosition(180);
        });
        assertEquals(180, connection.readShort(TARGET_POSITION));
        assertDoesNotThrow(() -> {
            motor.moveAbsolutePosition(0);
        });
        assertEquals(0, connection.readShort(TARGET_POSITION));
    }

    @Test
    public void testMoveAbsolutePositionAndWait() throws Exception {
        initializeMotor();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        connection.writeInteger(FEEDBACK_MOTION_STATE, 0);
        executor.schedule(() -> {
            try {
                connection.writeInteger(FEEDBACK_MOTION_STATE, 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 2, java.util.concurrent.TimeUnit.SECONDS);
        assertTrue(motor.moveAbsolutePositionAndWait(180).get());
        assertEquals(180, connection.readShort(TARGET_POSITION));
    }

    @Test
    public void testMoveRelativePositionAndWait() throws Exception {
        initializeMotor();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        connection.writeInteger(FEEDBACK_MOTION_STATE, 0);
        executor.schedule(() -> {
            try {
                connection.writeInteger(FEEDBACK_MOTION_STATE, 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 2, java.util.concurrent.TimeUnit.SECONDS);
        assertTrue(motor.moveRelativePositionAndWait(90).get());
        assertEquals(90, connection.readShort(RELATIVE_POSITION));
    }
}
