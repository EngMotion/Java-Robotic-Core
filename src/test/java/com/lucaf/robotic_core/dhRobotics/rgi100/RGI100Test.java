package com.lucaf.robotic_core.dhRobotics.rgi100;

import com.lucaf.robotic_core.dataInterfaces.impl.Register;
import com.lucaf.robotic_core.Pair;
import com.lucaf.robotic_core.mock.dataInterface.impl.MockedRegisterInterface;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lucaf.robotic_core.dhRobotics.rgi100.Constants.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RGI100Test {
    final HashMap<String, Object> state = new HashMap<>();
    final MockedRegisterInterface connection = new MockedRegisterInterface(1, "RGI100_22 Test Motor");
    final RGI100 motor = new RGI100(
            connection,
            state
    );

    @Test
    public void testState() {
        assert (state.containsKey("is_moving_grip"));
        assert (state.containsKey("is_moving_rotator"));
        assert (state.containsKey("is_moving"));
        assert (state.containsKey("is_moving"));
        assert (state.containsKey("current_position"));
        assert (state.containsKey("target_position"));
        assert (state.containsKey("current_angle"));
        assert (state.containsKey("target_angle"));
        assert (state.containsKey("is_initialized"));
        assert (state.containsKey("has_fault"));
        assert (state.containsKey("fault"));
    }

    void initializeMotor() throws Exception {
        connection.writeInteger(FEEDBACK_INITIALIZATION_GRIP_STATE, 1);
        connection.writeInteger(FEEDBACK_INITIALIZATION_ROTATION_STATE, 1);
        connection.writeInteger(FEEDBACK_GRIP_ERROR_CODE, 0);
        motor.initialize().get();
    }

    @Test
    public void testInitialization() throws Exception {
        assertThrows(IOException.class, () -> {
            motor.moveToAbsoluteAngle(10);
        });
        assertThrows(IOException.class, () -> {
            motor.moveToRelativeAngle(10);
        });
        assertThrows(IOException.class, () -> {
            motor.setGripPosition(10);
        });
        Pair<Boolean, PositionFeedback> feedback = motor.setGripPositionAndWait(10).get();
        assert (!feedback.first);
        feedback = motor.moveToRelativeAngleAndWait(10).get();
        assert (!feedback.first);
        feedback = motor.setGripPositionAndWait(10).get();
        assert (!feedback.first);
        //Post initialization
        initializeMotor();
        assert (motor.isInitialized());
    }

    @Test
    public void testErrorHandling() throws Exception {
        initializeMotor();
        connection.writeInteger(FEEDBACK_GRIP_ERROR_CODE, 4); //Overheat
        Thread.sleep(2000);
        assert (motor.hasError());
        assert (state.get("fault").equals("Overheat"));
        assertThrows(IOException.class, () -> {
            motor.setGripPosition(10);
        });
        connection.writeInteger(FEEDBACK_GRIP_ERROR_CODE, 0); //No error
        Thread.sleep(2000);
        assert (!motor.hasError());
        assert (state.get("fault").equals(""));
    }

    @Test
    public void testStop() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> {
            motor.setGripPosition(50);
        });
        motor.stop();
        assert (motor.isStopped());
        assertThrows(IOException.class, () -> {
            motor.setGripPosition(10);
        });
        initializeMotor();
        assertDoesNotThrow(() -> {
            motor.setGripPosition(10);
        });
    }

    @Test
    public void testShutdown() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> {
            motor.setGripPosition(50);
        });
        motor.shutdown();
        assert (motor.isShutdown());
        assertThrows(IOException.class, () -> {
            motor.setGripPosition(10);
        });
        assertThrows(RejectedExecutionException.class, () -> {
            motor.initialize().get();
        });
    }

    @Test
    public void testRotationPositionFeedback() throws Exception {
        initializeMotor();
        connection.writeInteger(FEEDBACK_ROTATION_STATE, 0);
        assert (motor.isRotationMoving().equals(PositionFeedback.MOVING));
        connection.writeInteger(FEEDBACK_ROTATION_STATE, 1);
        assert (motor.isRotationMoving().equals(PositionFeedback.REACHED));
        connection.writeInteger(FEEDBACK_ROTATION_STATE, 2);
        assert (motor.isRotationMoving().equals(PositionFeedback.BLOCKED));
    }

    @Test
    public void testGripPositionFeedback() throws Exception {
        initializeMotor();
        connection.writeInteger(FEEDBACK_GRIP_STATE, 0);
        assert (motor.isGripMoving().equals(PositionFeedback.MOVING));
        connection.writeInteger(FEEDBACK_GRIP_STATE, 1);
        assert (motor.isGripMoving().equals(PositionFeedback.REACHED_WITHOUT_OBJ));
        connection.writeInteger(FEEDBACK_GRIP_STATE, 2);
        assert (motor.isGripMoving().equals(PositionFeedback.REACHED_WITH_OBJ));
        connection.writeInteger(FEEDBACK_GRIP_STATE, 3);
        assert (motor.isGripMoving().equals(PositionFeedback.FALL));
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
        assert (connection.readShort(RELATIVE_ROTATION) == 90);
        assertDoesNotThrow(() -> {
            motor.moveToRelativeAngle(-450);
        });
        assert (connection.readShort(RELATIVE_ROTATION) == -450);
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
        assert (connection.readSignedLong(ABSOLUTE_ROTATION, true) == 90);
        assertDoesNotThrow(() -> {
            motor.moveToAbsoluteAngle(-450);
        });
        assert (connection.readSignedLong(ABSOLUTE_ROTATION, true) == -450);
        assertDoesNotThrow(() -> {
            motor.moveToAbsoluteAngle(400000);
        });
        assert (connection.readSignedLong(ABSOLUTE_ROTATION, true) == 400000);
        Register[] regs = connection.readMultipleRegisters(ABSOLUTE_ROTATION[0] << 8 | ABSOLUTE_ROTATION[1], 2);
        int high = regs[0].getValue();
        int low = regs[1].toShort();
        assert (high == 6784);
        assert (low == 12);
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
        } , 1, java.util.concurrent.TimeUnit.SECONDS);
        Pair<Boolean, PositionFeedback> feedback = motor.moveToAbsoluteAngleAndWait(90).get();
        assert (feedback.first);
        assert (feedback.second == PositionFeedback.REACHED);
        assert (((AtomicInteger)state.get("current_angle")).get() == 90);
        assert (!motor.isMoving());
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
        } , 1, java.util.concurrent.TimeUnit.SECONDS);
        Pair<Boolean, PositionFeedback> feedback = motor.moveToRelativeAngleAndWait(90).get();
        assert (feedback.first);
        assert (feedback.second == PositionFeedback.REACHED);
        assert (((AtomicInteger)state.get("current_angle")).get() == 90);
        assert (!motor.isMoving());
        feedback = motor.moveToRelativeAngleAndWait(-45).get();
        assert (feedback.first);
        assert (feedback.second == PositionFeedback.REACHED);
        assert (((AtomicInteger)state.get("current_angle")).get() == 45);
        assert (!motor.isMoving());
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
        } , 1, java.util.concurrent.TimeUnit.SECONDS);
        Pair<Boolean, PositionFeedback> feedback = motor.setGripPositionAndWait(500).get();
        assert (feedback.first);
        assert (feedback.second == PositionFeedback.REACHED_WITHOUT_OBJ);
        assert (((AtomicInteger)state.get("current_position")).get() == 500);
        assert (!motor.isMoving());
    }


    @Test
    public void testRotationForce() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> {
            motor.setRotationForce(50);
        });
        assert (motor.getRotationForce() == 50);
        assertDoesNotThrow(() -> {
            motor.setRotationForce(-10);
        });
        assert (motor.getRotationForce() == 20);
        assertDoesNotThrow(() -> {
            motor.setRotationForce(110);
        });
        assert (motor.getRotationForce() == 100);
    }

    @Test
    public void testRotationSpeed() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> {
            motor.setRotationSpeed(50);
        });
        assert (motor.getRotationSpeed() == 50);
        assertDoesNotThrow(() -> {
            motor.setRotationSpeed(-10);
        });
        assert (motor.getRotationSpeed() == 1);
        assertDoesNotThrow(() -> {
            motor.setRotationSpeed(110);
        });
        assert (motor.getRotationSpeed() == 100);
    }

    @Test
    public void testGripForce() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> {
            motor.setGripForce(50);
        });
        assert (motor.getGripForce() == 50);
        assertDoesNotThrow(() -> {
            motor.setGripForce(-10);
        });
        assert (motor.getGripForce() == 20);
        assertDoesNotThrow(() -> {
            motor.setGripForce(110);
        });
        assert (motor.getGripForce() == 100);
    }

    @Test
    public void testGripSpeed() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> {
            motor.setGripSpeed(50);
        });
        assert (motor.getGripSpeed() == 50);
        assertDoesNotThrow(() -> {
            motor.setGripSpeed(-10);
        });
        assert (motor.getGripSpeed() == 1);
        assertDoesNotThrow(() -> {
            motor.setGripSpeed(110);
        });
        assert (motor.getGripSpeed() == 100);
    }

    @Test
    public void testChangeAddress() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> {
            assert (motor.changeAddress(2));
        });
        assert (motor.getAddress() == 2);
    }
}
