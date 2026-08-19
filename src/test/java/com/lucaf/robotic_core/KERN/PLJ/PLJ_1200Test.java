package com.lucaf.robotic_core.KERN.PLJ;

import com.lucaf.robotic_core.dataInterfaces.test.FakeScaleSerialInterface;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class PLJ_1200Test {

    FakeScaleSerialInterface serial;
    PLJ_1200 scale;

    @BeforeEach
    void setUp() {
        serial = new FakeScaleSerialInterface("PLJ_1200");
        scale = new PLJ_1200(serial);
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
    }

    @Test
    void testReadReturnsLastStreamedValue() {
        serial.simulateData("12.345 g");
        assertEquals(12.345, scale.read(), 0.001);
    }

    @Test
    void testReadBeforeAnyDataReturnsMinusOne() {
        assertEquals(-1, scale.read(), 0.001);
    }

    @Test
    void testReadStableDefaultsToRead() throws Exception {
        serial.simulateData("7.25");
        assertEquals(scale.read(), scale.readStable(), 0.001);
    }

    @Test
    void testTareSendsTCommand() throws Exception {
        // After taring, the scale streams a near-zero weight.
        serial.scriptResponse("T", "0.001");
        Future<Boolean> tare = scale.tare();
        assertTrue(tare.get(10, TimeUnit.SECONDS));
        assertEquals("T", serial.getSentCommands().get(0));
    }

    @Test
    void testEventReading() throws Exception {
        double[] received = new double[1];
        scale.addReadingListener(w -> received[0] = w);
        scale.enableEventReading();
        assertTrue(scale.isEventReadingEnabled());

        serial.simulateData("5.5");
        assertEquals(5.5, received[0], 0.001);
        assertEquals(5.5, scale.getLastReading(), 0.001);
    }

    @Test
    void testEventReadingDisabledDoesNotEmit() {
        double[] received = new double[]{-1};
        scale.addReadingListener(w -> received[0] = w);

        serial.simulateData("5.5");
        assertEquals(-1, received[0], 0.001);
        assertEquals(5.5, scale.getLastReading(), 0.001);
    }
}
