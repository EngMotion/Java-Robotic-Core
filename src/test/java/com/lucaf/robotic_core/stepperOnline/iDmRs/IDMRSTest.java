package com.lucaf.robotic_core.stepperOnline.iDmRs;

import com.lucaf.robotic_core.dataInterfaces.impl.Register;
import com.lucaf.robotic_core.mock.dataInterface.MockedRegisterInterface;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.lucaf.robotic_core.stepperOnline.iDmRs.Constants.*;
import static org.junit.jupiter.api.Assertions.*;


public class IDMRSTest {
    final HashMap<String, Object> state = new HashMap<>();
    final MockedRegisterInterface connection = new MockedRegisterInterface(1, "SAC_N Test Motor");

    final IDMRS motor = new IDMRS(
            connection,
            state
    );

    void initializeMotor() throws Exception {
        motor.initialize().get();
        motor.setPositioningMode();
    }

    @Test
    public void testInitialization() throws Exception {
        assertThrows(IOException.class, () -> motor.setPosition(10));
        initializeMotor();
        assertTrue(motor.isInitialized());
        assertDoesNotThrow(() -> motor.setPosition(10));
    }

    @Test
    public void testStop() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> motor.setPosition(50));
        motor.stop();
        assertTrue(motor.isStopped());
        assertThrows(IOException.class, () -> motor.setPosition(10));
        initializeMotor();
        assertDoesNotThrow(() -> motor.setPosition(10));
    }

    @Test
    public void testShutdown() throws Exception {
        initializeMotor();
        assertDoesNotThrow(() -> motor.setPosition(50));
        motor.shutdown();
        assertTrue(motor.isShutdown());
        assertThrows(IOException.class, () -> motor.setPosition(10));
        assertThrows(RejectedExecutionException.class, () -> motor.initialize().get());
    }

    @Test
    public void testWaitReachPosition() throws Exception {
        initializeMotor();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        connection.writeInteger(STATUS_MODE, 0x0100);
        motor.setMoving(true);
        executor.schedule(() -> {
            try {
                connection.writeInteger(STATUS_MODE, 0x0000);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 2, TimeUnit.SECONDS);
        assertDoesNotThrow(() -> motor.waitReachedPosition());
    }

    @Test
    public void testWaitReachPositionFail() throws Exception {
        initializeMotor();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        connection.writeInteger(STATUS_MODE, 0x0100);
        motor.setMoving(true);
        executor.schedule(() -> {
            try {
                motor.stop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 2, TimeUnit.SECONDS);
        assertThrows(IOException.class, motor::waitReachedPosition);
    }

    @Test
    public void testWaitReachPositionTimeout() throws Exception {
        initializeMotor();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        connection.writeInteger(STATUS_MODE, 0x0100);
        motor.setMoving(true);
        executor.schedule(() -> {
            try {
                connection.writeInteger(STATUS_MODE, 0x0000);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 2, TimeUnit.SECONDS);
        assertThrows(IOException.class, () -> motor.waitReachedPosition(1000));
    }


    @Test
    void testLongPosition() throws Exception {
        initializeMotor();
        motor.setPosition(200000);
        assertEquals(200000, connection.readSignedLong(TARGET_POSITION_HIGH, false));
        Register[] regs = connection.readMultipleRegisters(TARGET_POSITION_HIGH[0] << 8 | TARGET_POSITION_HIGH[1], 2);
        int high = regs[0].getValue();
        int low = regs[1].getValue();
        assertEquals(0x0003, high);
        assertEquals(0x0D40, low);
    }

    @Test
    void testNegativeLongPosition() throws Exception {
        initializeMotor();
        motor.setPosition(-200000);
        assertEquals(-200000, connection.readSignedLong(TARGET_POSITION_HIGH, false));
        Register[] regs = connection.readMultipleRegisters(TARGET_POSITION_HIGH[0] << 8 | TARGET_POSITION_HIGH[1], 2);
        int high = regs[0].getValue();
        int low = regs[1].getValue();
        assertEquals(0xFFFC, high);
        assertEquals(0xF2C0, low);
    }

    @Test
    void testRelativePositioning() throws Exception {
        initializeMotor();
        motor.setRelativePositioning(true);
        motor.setPosition(1000);
        assertEquals(1000, connection.readSignedLong(TARGET_POSITION_HIGH, false));
        motor.setPosition(1000);
        assertEquals(2000, ((AtomicLong)state.get("target_position")).get());
    }

    @Test
    void testDigitalInputs() throws Exception {
        initializeMotor();
        int inputs = 0b10101010;
        connection.writeInteger(DIGITAL_INPUTS_STATUS, inputs);
        DigitalInputs di = motor.getDigitalInputs();
        assertFalse(di.isDI1());
        assertTrue(di.isDI2());
        assertFalse(di.isDI3());
        assertTrue(di.isDI4());
        assertFalse(di.isDI5());
        assertTrue(di.isDI6());
        assertFalse(di.isDI7());
    }

    @Test
    void testDigitalOutputs() throws Exception {
        initializeMotor();
        DigitalOutput digitalOutput = new DigitalOutput(0);
        motor.setDigitalOutput(1, digitalOutput);
        motor.setDigitalOutput(2, digitalOutput);
        DigitalOutput digitalOutput2 = new DigitalOutput(0);
        digitalOutput2.setNormally_closed(true);
        motor.setDigitalOutput(3, digitalOutput2);

        DigitalOutput do1 = motor.getDigitalOutput(1);
        DigitalOutput do2 = motor.getDigitalOutput(2);
        DigitalOutput do3 = motor.getDigitalOutput(3);

        assertFalse(do1.isNormally_closed());
        assertFalse(do2.isNormally_closed());
        assertTrue(do3.isNormally_closed());
    }

    @Test
    void testSpeed() throws Exception {
        initializeMotor();
        motor.setSpeed(1500);
        assertEquals(1500, connection.readInteger(VELOCITY));
    }

    @Test
    void testAcceleration() throws Exception {
        initializeMotor();
        motor.setAcceleration(5000);
        assertEquals(5000, connection.readInteger(ACCELERATION));
    }

    @Test
    void testDeceleration() throws Exception {
        initializeMotor();
        motor.setDeceleration(4000);
        assertEquals(4000, connection.readInteger(DECELERATION));
    }

    @Test
    void testEstimatedArrivalTime() throws Exception {
        initializeMotor();
        motor.setPosition(3000);
        motor.setSpeed(1000);
        motor.setAcceleration(2000);
        motor.setDeceleration(2000);
        long eta = motor.estimateArrivalTime(10000);
        assertTrue(Math.abs(eta-10) < 2); // 10 seconds +/- 2 seconds
    }

    @Test
    public void testMoveToPositionAndWait() throws Exception {
        initializeMotor();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        connection.writeInteger(STATUS_MODE, 0x0100);
        motor.setMoving(true);
        executor.schedule(() -> {
            try {
                connection.writeInteger(STATUS_MODE, 0x0000);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 2, TimeUnit.SECONDS);
        assertTrue(motor.moveToPositionAndWait(1000).get());
        assertEquals(1000, connection.readSignedLong(TARGET_POSITION_HIGH, false));
    }


}
