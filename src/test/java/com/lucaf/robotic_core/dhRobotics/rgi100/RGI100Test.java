package com.lucaf.robotic_core.dhRobotics.rgi100;

import com.lucaf.robotic_core.dataInterfaces.impl.Register;
import com.lucaf.robotic_core.Pair;
import com.lucaf.robotic_core.mock.dataInterface.MockedRegisterInterface;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lucaf.robotic_core.dhRobotics.rgi100.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

public class RGI100Test {
    final HashMap<String, Object> state = new HashMap<>();
    final MockedRegisterInterface connection = new MockedRegisterInterface(1, "RGI100_22 Test Motor");
    final RGI100 motor = new RGI100(
            connection,
            state
    );

    @Test
    public void testState() {
        assertTrue(state.containsKey("is_moving_grip"));
        assertTrue(state.containsKey("is_moving_rotator"));
        assertTrue(state.containsKey("is_moving"));
        assertTrue(state.containsKey("current_position"));
        assertTrue(state.containsKey("target_position"));
        assertTrue(state.containsKey("current_angle"));
        assertTrue(state.containsKey("target_angle"));
        assertTrue(state.containsKey("is_initialized"));
        assertTrue(state.containsKey("has_fault"));
        assertTrue(state.containsKey("fault"));
    }

    void initializeMotor() throws Exception {
        connection.writeInteger(FEEDBACK_INITIALIZATION_GRIP_STATE, 1);
        connection.writeInteger(FEEDBACK_INITIALIZATION_ROTATION_STATE, 1);
        connection.writeInteger(FEEDBACK_GRIP_ERROR_CODE, 0);
        motor.initialize().get();
    }

    @Test
    public void testInitialization() throws Exception {
        assertThrows(IOException.class, () -> motor.moveToAbsoluteAngle(10));
        assertThrows(IOException.class, () -> motor.moveToRelativeAngle(10));
        assertThrows(IOException.class, () -> motor.setGripPosition(10));

        Pair<Boolean, PositionFeedback> feedback = motor.setGripPositionAndWait(10).get();
        assertFalse(feedback.first);

        feedback = motor.moveToRelativeAngleAndWait(10).get();
        assertFalse(feedback.first);

        feedback = motor.setGripPositionAndWait(10).get();
        assertFalse(feedback.first);

        //Post initialization
        initializeMotor();
        assertTrue(motor.isInitialized());
    }

    @Test
    public void testErrorHandling() throws Exception {
        initializeMotor();
        connection.writeInteger(FEEDBACK_GRIP_ERROR_CODE, 4); // Overheat
        Thread.sleep(2000);
        assertTrue(motor.hasError());
        assertEquals("Overheat", state.get("fault"));

        assertThrows(IOException.class, () -> motor.setGripPosition(10));

        connection.writeInteger(FEEDBACK_GRIP_ERROR_CODE, 0); // No error
        Thread.sleep(2000);
        assertFalse(motor.hasError());
        assertEquals("", state.get("fault"));
    }

    @Test
    public void testStop() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> motor.setGripPosition(50));
        motor.stop();
        assertTrue(motor.isStopped());
        assertThrows(IOException.class, () -> motor.setGripPosition(10));

        initializeMotor();
        assertDoesNotThrow(() -> motor.setGripPosition(10));
    }

    @Test
    public void testShutdown() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> motor.setGripPosition(50));
        motor.shutdown();
        assertTrue(motor.isShutdown());
        assertThrows(IOException.class, () -> motor.setGripPosition(10));
        assertThrows(RejectedExecutionException.class, () -> motor.initialize().get());
    }

    @Test
    public void testRotationPositionFeedback() throws Exception {
        initializeMotor();
        connection.writeInteger(FEEDBACK_ROTATION_STATE, 0);
        assertEquals(PositionFeedback.MOVING, motor.isRotationMoving());

        connection.writeInteger(FEEDBACK_ROTATION_STATE, 1);
        assertEquals(PositionFeedback.REACHED, motor.isRotationMoving());

        connection.writeInteger(FEEDBACK_ROTATION_STATE, 2);
        assertEquals(PositionFeedback.BLOCKED, motor.isRotationMoving());
    }

    @Test
    public void testGripPositionFeedback() throws Exception {
        initializeMotor();
        connection.writeInteger(FEEDBACK_GRIP_STATE, 0);
        assertEquals(PositionFeedback.MOVING, motor.isGripMoving());
        connection.writeInteger(FEEDBACK_GRIP_STATE, 1);
        assertEquals(PositionFeedback.REACHED_WITHOUT_OBJ, motor.isGripMoving());
        connection.writeInteger(FEEDBACK_GRIP_STATE, 2);
        assertEquals(PositionFeedback.REACHED_WITH_OBJ, motor.isGripMoving());
        connection.writeInteger(FEEDBACK_GRIP_STATE, 3);
        assertEquals(PositionFeedback.FALL, motor.isGripMoving());
    }

    @Test
    public void testWaitEndPosition() throws Exception {
        initializeMotor();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        connection.writeInteger(FEEDBACK_ROTATION_STATE, 0);
        executor.schedule(() -> {
            try {
                connection.writeInteger(FEEDBACK_ROTATION_STATE, 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 1, java.util.concurrent.TimeUnit.SECONDS);
        assert (motor.waitEndPosition().equals(PositionFeedback.REACHED));
    }

    @Test
    public void testWaitEndPositionFail() throws Exception {
        initializeMotor();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        connection.writeInteger(FEEDBACK_ROTATION_STATE, 0);
        executor.schedule(() -> {
            try {
                motor.stop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 1, java.util.concurrent.TimeUnit.SECONDS);
        assertThrows(IOException.class, motor::waitEndPosition);
    }

    @Test
    public void testWaitEndPositionTimeout() throws Exception {
        initializeMotor();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        connection.writeInteger(FEEDBACK_ROTATION_STATE, 0);
        executor.schedule(() -> {
            try {
                connection.writeInteger(FEEDBACK_ROTATION_STATE, 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 2, java.util.concurrent.TimeUnit.SECONDS);
        assertThrows(IOException.class, () -> {
            motor.waitEndPosition(1000);
        });
    }

    @Test
    public void testWaitEndGrip() throws Exception {
        initializeMotor();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        connection.writeInteger(FEEDBACK_GRIP_STATE, 0);
        executor.schedule(() -> {
            try {
                connection.writeInteger(FEEDBACK_GRIP_STATE, 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 1, java.util.concurrent.TimeUnit.SECONDS);
        assert (motor.waitEndGrip().equals(PositionFeedback.REACHED_WITHOUT_OBJ));
    }

    @Test
    public void testWaitEndGripFail() throws Exception {
        initializeMotor();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        connection.writeInteger(FEEDBACK_GRIP_STATE, 0);
        executor.schedule(() -> {
            try {
                motor.stop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 1, java.util.concurrent.TimeUnit.SECONDS);
        assertThrows(IOException.class, motor::waitEndGrip);
    }

    @Test
    public void testWaitEndGripTimeout() throws Exception {
        initializeMotor();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        connection.writeInteger(FEEDBACK_GRIP_STATE, 0);
        executor.schedule(() -> {
            try {
                connection.writeInteger(FEEDBACK_GRIP_STATE, 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 2, java.util.concurrent.TimeUnit.SECONDS);
        assertThrows(IOException.class, () -> {
            motor.waitEndGrip(1000);
        });
    }

    @Test
    public void testGripPosition() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> {
            motor.setGripPosition(50);
        });
        assert (connection.readShort(TARGET_POSITION) == 50);
        assertDoesNotThrow(() -> {
            motor.setGripPosition(-10);
        });
        assert (connection.readShort(TARGET_POSITION) == 0);
        assertDoesNotThrow(() -> {
            motor.setGripPosition(1100);
        });
        assert (connection.readShort(TARGET_POSITION) == 1000);
    }

    @Test
    public void testMoveToRelativeAngle() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> {
            motor.moveToRelativeAngle(90);
        });
        assertEquals(90, connection.readShort(RELATIVE_ROTATION));
        assertDoesNotThrow(() -> {
            motor.moveToRelativeAngle(-450);
        });
        assertEquals(-450, connection.readShort(RELATIVE_ROTATION));
        assertThrows(IOException.class, () -> {
            motor.moveToRelativeAngle(40000);
        });
    }

    @Test
    public void testMoveToAbsoluteAngle() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> {
            motor.moveToAbsoluteAngle(90);
        });
        assertEquals(90, connection.readSignedLong(ABSOLUTE_ROTATION, true));
        assertDoesNotThrow(() -> {
            motor.moveToAbsoluteAngle(-450);
        });
        assertEquals(-450, connection.readSignedLong(ABSOLUTE_ROTATION, true));
        assertDoesNotThrow(() -> {
            motor.moveToAbsoluteAngle(400000);
        });
        assertEquals(400000L, connection.readSignedLong(ABSOLUTE_ROTATION, true));
        Register[] regs = connection.readMultipleRegisters(ABSOLUTE_ROTATION[0] << 8 | ABSOLUTE_ROTATION[1], 2);
        int high = regs[0].getValue();
        int low = regs[1].toShort();
        assertEquals(6784, high);
        assertEquals(6, low);
    }

    @Test
    public void moveToAbsoluteAngleTest() throws Exception {
        initializeMotor();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        connection.writeInteger(FEEDBACK_ROTATION_STATE, 0);
        executor.schedule(() -> {
            try {
                connection.writeInteger(FEEDBACK_ROTATION_STATE, 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 1, java.util.concurrent.TimeUnit.SECONDS);
        Pair<Boolean, PositionFeedback> feedback = motor.moveToAbsoluteAngleAndWait(90).get();
        assertEquals(true, feedback.first);
        assertEquals(PositionFeedback.REACHED, feedback.second);
        assertEquals(90, ((AtomicInteger) state.get("current_angle")).get());
        assertEquals(false, motor.isMoving());
    }

    @Test
    public void moveToRelativeAngleTest() throws Exception {
        initializeMotor();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        connection.writeInteger(FEEDBACK_ROTATION_STATE, 0);
        executor.schedule(() -> {
            try {
                connection.writeInteger(FEEDBACK_ROTATION_STATE, 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 1, java.util.concurrent.TimeUnit.SECONDS);
        Pair<Boolean, PositionFeedback> feedback = motor.moveToAbsoluteAngleAndWait(90).get();
        assertEquals(true, feedback.first);
        assertEquals(PositionFeedback.REACHED, feedback.second);
        assertEquals(90, ((AtomicInteger) state.get("current_angle")).get());
        assertEquals(false, motor.isMoving());

        feedback = motor.moveToRelativeAngleAndWait(-45).get();
        assertEquals(true, feedback.first);
        assertEquals(PositionFeedback.REACHED, feedback.second);
        assertEquals(45, ((AtomicInteger) state.get("current_angle")).get());
        assertEquals(false, motor.isMoving());
    }

    @Test
    public void setGripPositionTest() throws Exception {
        initializeMotor();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        connection.writeInteger(FEEDBACK_GRIP_STATE, 0);
        executor.schedule(() -> {
            try {
                connection.writeInteger(FEEDBACK_GRIP_STATE, 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 1, java.util.concurrent.TimeUnit.SECONDS);
        Pair<Boolean, PositionFeedback> feedback = motor.setGripPositionAndWait(500).get();
        assertTrue(feedback.first);
        assertEquals(PositionFeedback.REACHED_WITHOUT_OBJ, feedback.second);
        assertEquals(500, ((AtomicInteger) state.get("current_position")).get());
        assertFalse(motor.isMoving());
    }


    @Test
    public void testRotationForce() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> motor.setRotationForce(50));
        assertEquals(50, motor.getRotationForce());
        assertDoesNotThrow(() -> motor.setRotationForce(-10));
        assertEquals(20, motor.getRotationForce());
        assertDoesNotThrow(() -> motor.setRotationForce(110));
        assertEquals(100, motor.getRotationForce());
    }

    @Test
    public void testRotationSpeed() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> motor.setRotationSpeed(50));
        assertEquals(50, motor.getRotationSpeed());
        assertDoesNotThrow(() -> motor.setRotationSpeed(-10));
        assertEquals(1, motor.getRotationSpeed());
        assertDoesNotThrow(() -> motor.setRotationSpeed(110));
        assertEquals(100, motor.getRotationSpeed());
    }

    @Test
    public void testGripForce() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> motor.setGripForce(50));
        assertEquals(50, motor.getGripForce());
        assertDoesNotThrow(() -> motor.setGripForce(-10));
        assertEquals(20, motor.getGripForce());
        assertDoesNotThrow(() -> motor.setGripForce(110));
        assertEquals(100, motor.getGripForce());
    }

    @Test
    public void testGripSpeed() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> motor.setGripSpeed(50));
        assertEquals(50, motor.getGripSpeed());
        assertDoesNotThrow(() -> motor.setGripSpeed(-10));
        assertEquals(1, motor.getGripSpeed());
        assertDoesNotThrow(() -> motor.setGripSpeed(110));
        assertEquals(100, motor.getGripSpeed());
    }

    @Test
    public void testChangeAddress() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> assertTrue(motor.changeAddress(2)));
        assertEquals(2, motor.getAddress());
    }
}
