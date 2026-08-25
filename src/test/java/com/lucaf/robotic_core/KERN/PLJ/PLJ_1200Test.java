package com.lucaf.robotic_core.KERN.PLJ;

import com.lucaf.robotic_core.dataInterfaces.test.FakeScaleSerialInterface;
import com.lucaf.robotic_core.impl.ScaleResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class PLJ_1200Test {

    /**
     * Frame terminator used by the balance.
     */
    private static final String CRLF = "\r\n";

    // Frames are 13 characters wide: S N1..N8 U1 U2 U3 T.
    //                                          0123456789012
    private static final String STABLE_12_345 = "   12.345g  S";
    private static final String UNSTABLE_12_345 = "   12.345g   ";
    private static final String STABLE_11_203 = "   11.203g  S";
    private static final String STABLE_7_25 = "     7.25g  S";
    private static final String STABLE_5_5 = "      5.5g  S";
    private static final String STABLE_0_001 = "    0.001g  S";
    private static final String STABLE_NEGATIVE = "-    5.25g  S";
    private static final String STABLE_KILOGRAMS = "      1.5kg S";

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

    /**
     * Asserts that a response carries the expected weight rather than an error.
     */
    private static void assertWeight(double expected, ScaleResponse actual) {
        assertNotNull(actual);
        assertFalse(actual.isError(), "expected a weight, got an error response");
        assertEquals(expected, actual.getWeight(), 0.0001);
    }

    @Test
    void testInitializeConnected() throws Exception {
        assertTrue(scale.initialize().get());
        assertTrue(scale.isInitialized());
        assertTrue(scale.isConnected());
    }

    // ------------------------------------------------------------------ frame decoding

    @Test
    void testParseStableFrame() {
        ScaleResponse response = PLJ_1200.parse(STABLE_12_345);
        assertNotNull(response);
        assertFalse(response.isError());
        assertTrue(response.isStable());
        assertEquals(12.345, response.getWeight(), 0.0001);
        assertEquals("g", response.getUnit());
    }

    @Test
    void testParseUnstableFrameKeepsTheUnit() {
        // Unlike the PCB, the PLJ still reports the unit while the weight is moving: stability lives
        // in its own trailing character.
        ScaleResponse response = PLJ_1200.parse(UNSTABLE_12_345);
        assertNotNull(response);
        assertFalse(response.isStable());
        assertEquals(12.345, response.getWeight(), 0.0001);
        assertEquals("g", response.getUnit());
    }

    @Test
    void testParseNegativeWeight() {
        assertEquals(-5.25, PLJ_1200.parse(STABLE_NEGATIVE).getWeight(), 0.0001);
    }

    @Test
    void testParseKilograms() {
        assertEquals("kg", PLJ_1200.parse(STABLE_KILOGRAMS).getUnit());
        assertEquals(1.5, PLJ_1200.parse(STABLE_KILOGRAMS).getWeight(), 0.0001);
    }

    @Test
    void testParseRejectsMalformedFrames() {
        assertNull(PLJ_1200.parse(null));
        assertNull(PLJ_1200.parse(""));
        // A PCB frame is sixteen characters wide, not thirteen.
        assertNull(PLJ_1200.parse("      12.345 g  "));
        // A fragment of a frame that has not been fully received yet.
        assertNull(PLJ_1200.parse("   11."));
        // Free text of the right length.
        assertNull(PLJ_1200.parse("no weight her"));
        // The mass field must be a bare decimal number, not everything parseDouble tolerates.
        assertNull(PLJ_1200.parse("     1e5 g  S"));
        assertNull(PLJ_1200.parse("     NaN g  S"));
        // The sign field only ever holds a space or a minus sign.
        assertNull(PLJ_1200.parse("+   12.345g S"));
    }

    // ------------------------------------------------------------------ commands

    @Test
    void testReadReturnsLastStreamedValue() {
        serial.simulateData(STABLE_12_345 + CRLF);
        assertWeight(12.345, scale.read());
        // The PLJ transmits on its own: reading must not send anything.
        assertTrue(serial.getSentCommands().isEmpty());
    }

    @Test
    void testReadBeforeAnyDataReturnsErrorResponse() {
        assertTrue(scale.read().isError());
    }

    @Test
    void testReadStableSendsECommand() throws Exception {
        serial.scriptResponse("E", STABLE_7_25 + CRLF);
        assertWeight(7.25, scale.readStable());
        assertEquals("E", serial.getSentCommands().get(0));
    }

    @Test
    void testReadStableReturnsErrorWhenBalanceStaysSilent() throws Exception {
        assertTrue(scale.readStable().isError());
        assertEquals("E", serial.getSentCommands().get(0));
    }

    @Test
    void testTareSendsTCommand() throws Exception {
        // After taring, the balance transmits a near-zero weight.
        serial.scriptResponse("T", STABLE_0_001 + CRLF);
        Future<Boolean> tare = scale.tare();
        assertTrue(tare.get(15, TimeUnit.SECONDS));
        assertEquals("T", serial.getSentCommands().get(0));
    }

    @Test
    void testRemoteCommands() throws Exception {
        scale.record();
        scale.menu();
        scale.togglePower();
        assertEquals("C", serial.getSentCommands().get(0));
        assertEquals("M", serial.getSentCommands().get(1));
        assertEquals("O", serial.getSentCommands().get(2));
    }

    // ------------------------------------------------------------------ framing

    @Test
    void testFragmentedFrameIsAssembled() {
        serial.simulateData("   11.");
        assertTrue(scale.read().isError(), "a partial frame must not be reported as a weight");
        serial.simulateData("203g  S" + CRLF);
        assertWeight(11.203, scale.read());
    }

    @Test
    void testSeveralFramesInASingleChunk() {
        serial.simulateData(STABLE_12_345 + CRLF + STABLE_11_203 + CRLF);
        assertWeight(11.203, scale.read());
    }

    @Test
    void testAbandonedFragmentDoesNotCorruptTheNextFrame() throws Exception {
        serial.simulateData("   11.");
        Thread.sleep(300);
        serial.simulateData(STABLE_12_345 + CRLF);
        assertWeight(12.345, scale.read());
    }

    // ------------------------------------------------------------------ state

    @Test
    void testStabilityFollowsTheFrame() {
        serial.simulateData(UNSTABLE_12_345 + CRLF);
        assertFalse(scale.isStable());
        serial.simulateData(STABLE_12_345 + CRLF);
        assertTrue(scale.isStable());
    }

    @Test
    void testUnitIsUpdatedFromTheFrame() {
        serial.simulateData(STABLE_KILOGRAMS + CRLF);
        assertEquals("kg", scale.getUnit());
    }

    // ------------------------------------------------------------------ streaming

    @Test
    void testEventReading() {
        AtomicReference<ScaleResponse> received = new AtomicReference<>();
        scale.addReadingListener(received::set);
        scale.enableEventReading();
        assertTrue(scale.isEventReadingEnabled());

        serial.simulateData(STABLE_5_5 + CRLF);
        assertWeight(5.5, received.get());
        assertWeight(5.5, scale.getLastReading());
    }

    @Test
    void testEventReadingDisabledDoesNotEmit() {
        AtomicReference<ScaleResponse> received = new AtomicReference<>();
        scale.addReadingListener(received::set);

        serial.simulateData(STABLE_5_5 + CRLF);
        assertNull(received.get());
        assertWeight(5.5, scale.getLastReading());
    }
}
