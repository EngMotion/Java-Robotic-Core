package com.lucaf.robotic_core.KERN.PCB;

import com.lucaf.robotic_core.dataInterfaces.test.FakeScaleSerialInterface;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class PCB_3Test {

    FakeScaleSerialInterface serial;
    PCB_3 scale;

    @BeforeEach
    void setUp() {
        serial = new FakeScaleSerialInterface("PCB_3");
        scale = new PCB_3(serial);
    }

    @AfterEach
    void tearDown() throws Exception {
        scale.shutdown();
        serial.dispose();
    }

    @Test
    void testInitializeConnected() throws Exception {
        assertTrue(scale.initialize().get());
        assertTrue(scale.isInitialized());
        assertTrue(scale.isConnected());
        assertFalse(scale.hasError());
    }

    @Test
    void testInitializeDisconnected() throws Exception {
        serial.setConnected(false);
        assertFalse(scale.initialize().get());
        assertFalse(scale.isInitialized());
    }

    @Test
    void testReadSendsWCommand() throws Exception {
        serial.scriptResponse("w", "12.345 g");
        assertEquals(12.345, scale.read(), 0.001);
        assertEquals("w", serial.getSentCommands().get(0));
    }

    @Test
    void testReadStableSendsSCommand() throws Exception {
        serial.scriptResponse("s", "9.000");
        assertEquals(9.0, scale.readStable(), 0.001);
        assertEquals("s", serial.getSentCommands().get(0));
    }

    @Test
    void testReadNegativeWeight() throws Exception {
        serial.scriptResponse("w", "-5.25");
        assertEquals(-5.25, scale.read(), 0.001);
    }

    @Test
    void testReadNonNumericDataReturnsMinusOne() throws Exception {
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(50);
                serial.simulateData("no weight here");
            } catch (InterruptedException ignored) {
            }
        });
        t.start();
        // No valid number arrives, so the read times out and returns -1.
        assertEquals(-1, scale.read(), 0.001);
    }

    @Test
    void testTareSendsTCommand() throws Exception {
        serial.scriptResponse("w", "0.05");
        Future<Boolean> tare = scale.tare();
        assertTrue(tare.get(10, TimeUnit.SECONDS));
        assertEquals("t", serial.getSentCommands().get(0));
    }

    @Test
    void testEventReading() throws Exception {
        double[] received = new double[1];
        scale.addReadingListener(w -> received[0] = w);
        scale.enableEventReading();
        assertTrue(scale.isEventReadingEnabled());

        serial.simulateData("3.14");
        assertEquals(3.14, received[0], 0.001);
        assertEquals(3.14, scale.getLastReading(), 0.001);

        scale.disableEventReading();
        assertFalse(scale.isEventReadingEnabled());
    }

    @Test
    void testEventReadingDisabledDoesNotEmit() {
        double[] received = new double[]{-1};
        scale.addReadingListener(w -> received[0] = w);

        // Event reading is disabled by default, so consumers must not be notified...
        serial.simulateData("3.14");
        assertEquals(-1, received[0], 0.001);

        // ...but the last reading is still tracked internally.
        assertEquals(3.14, scale.getLastReading(), 0.001);
    }
}
