package com.lucaf.robotic_core.KERN.PCB;

import com.lucaf.robotic_core.dataInterfaces.test.FakeScaleSerialInterface;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class PCB_3Test {

    /**
     * Frame terminator used by the scale.
     */
    private static final String CRLF = "\r\n";

    // Frames are 16 characters wide: M S N1..N10 B U1 U2 U3.
    //                                          0123456789012345
    private static final String STABLE_12_345 = "      12.345 g  ";
    private static final String STABLE_9_000 = "       9.000 g  ";
    private static final String STABLE_0_05 = "        0.05 g  ";
    private static final String STABLE_3_14 = "        3.14 g  ";
    private static final String STABLE_11_203 = "      11.203 g  ";
    private static final String STABLE_NEGATIVE = " -      5.25 g  ";
    private static final String STABLE_KILOGRAMS = "         1.5 kg ";
    private static final String STABLE_PIECES = "         100 pcs";
    private static final String STABLE_MODE_MARKER = "M     12.345 g  ";
    private static final String UNSTABLE_12_345 = "      12.345    ";
    private static final String ERROR_FRAME = "           Error";

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

    /**
     * Pushes incoming data after a short delay, so that a blocking read is already waiting for it.
     */
    private void simulateLater(long delayMs, String... chunks) {
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(delayMs);
                for (String chunk : chunks) {
                    serial.simulateData(chunk);
                    Thread.sleep(10);
                }
            } catch (InterruptedException ignored) {
            }
        });
        t.setDaemon(true);
        t.start();
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

    // ------------------------------------------------------------------ frame decoding

    @Test
    void testParseStableFrame() {
        PCB_3.Response response = PCB_3.parse(STABLE_12_345);
        assertNotNull(response);
        assertFalse(response.isError());
        assertTrue(response.isStable());
        assertEquals(12.345, response.getWeight(), 0.0001);
        assertEquals("g", response.getUnit());
    }

    @Test
    void testParseUnstableFrameHasNoUnit() {
        PCB_3.Response response = PCB_3.parse(UNSTABLE_12_345);
        assertNotNull(response);
        assertFalse(response.isError());
        assertFalse(response.isStable());
        assertEquals(12.345, response.getWeight(), 0.0001);
        assertEquals("", response.getUnit());
    }

    @Test
    void testParseErrorFrame() {
        PCB_3.Response response = PCB_3.parse(ERROR_FRAME);
        assertNotNull(response);
        assertTrue(response.isError());
        assertNull(response.getWeight());
    }

    @Test
    void testParseNegativeWeight() {
        PCB_3.Response response = PCB_3.parse(STABLE_NEGATIVE);
        assertNotNull(response);
        assertEquals(-5.25, response.getWeight(), 0.0001);
    }

    @Test
    void testParseUnits() {
        assertEquals("kg", PCB_3.parse(STABLE_KILOGRAMS).getUnit());
        assertEquals(1.5, PCB_3.parse(STABLE_KILOGRAMS).getWeight(), 0.0001);
        assertEquals("pcs", PCB_3.parse(STABLE_PIECES).getUnit());
        assertEquals(100, PCB_3.parse(STABLE_PIECES).getWeight(), 0.0001);
    }

    @Test
    void testParseAcceptsModeMarker() {
        PCB_3.Response response = PCB_3.parse(STABLE_MODE_MARKER);
        assertNotNull(response);
        assertEquals(12.345, response.getWeight(), 0.0001);
    }

    @Test
    void testParseRejectsMalformedFrames() {
        assertNull(PCB_3.parse(null));
        assertNull(PCB_3.parse(""));
        // A truncated frame: the right characters, but not sixteen of them.
        assertNull(PCB_3.parse("      12.345 g"));
        // A fragment of a frame that has not been fully received yet.
        assertNull(PCB_3.parse("      11."));
        // Free text of the right length.
        assertNull(PCB_3.parse("no weight here !"));
        // The weight field must be a bare decimal number, not everything parseDouble tolerates.
        assertNull(PCB_3.parse("       1e5   g  "));
        assertNull(PCB_3.parse("         NaN g  "));
        // The separator between weight and unit is always a space.
        assertNull(PCB_3.parse("      12.345_g  "));
        // The sign field only ever holds a space or a minus sign.
        assertNull(PCB_3.parse(" +     12.345 g "));
    }

    // ------------------------------------------------------------------ commands

    @Test
    void testReadSendsWCommand() throws Exception {
        serial.scriptResponse("w", STABLE_12_345 + CRLF);
        assertEquals(12.345, scale.read(), 0.001);
        assertEquals("w", serial.getSentCommands().get(0));
        assertTrue(scale.isStable());
        assertEquals("g", scale.getUnit());
    }

    @Test
    void testReadStableSendsSCommand() throws Exception {
        serial.scriptResponse("s", STABLE_9_000 + CRLF);
        assertEquals(9.0, scale.readStable(), 0.001);
        assertEquals("s", serial.getSentCommands().get(0));
    }

    @Test
    void testReadStableReturnsMinusOneWhenScaleStaysSilent() throws Exception {
        // The scale answers "s" only while the weight is settled: silence means "not stable".
        long start = System.currentTimeMillis();
        assertEquals(-1, scale.readStable(), 0.001);
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 400, "readStable() returned after only " + elapsed + " ms");
        assertTrue(elapsed < 2000, "readStable() waited " + elapsed + " ms, expected roughly 500");
    }

    @Test
    void testReadNegativeWeight() throws Exception {
        serial.scriptResponse("w", STABLE_NEGATIVE + CRLF);
        assertEquals(-5.25, scale.read(), 0.001);
    }

    @Test
    void testReadUnstableFrameStillReturnsTheWeight() throws Exception {
        serial.scriptResponse("w", UNSTABLE_12_345 + CRLF);
        assertEquals(12.345, scale.read(), 0.001);
        assertFalse(scale.isStable());
    }

    @Test
    void testReadErrorFrameReturnsMinusOne() throws Exception {
        serial.scriptResponse("w", ERROR_FRAME + CRLF);
        assertEquals(-1, scale.read(), 0.001);
        assertTrue(scale.hasError());
        assertFalse(scale.isStable());
        // An error frame carries no weight, so the last known reading must not be touched.
        assertNull(scale.getLastReading());
    }

    @Test
    void testErrorFlagClearedByTheNextValidFrame() {
        serial.simulateData(ERROR_FRAME + CRLF);
        assertTrue(scale.hasError());
        serial.simulateData(STABLE_12_345 + CRLF);
        assertFalse(scale.hasError());
    }

    @Test
    void testReadMalformedFrameReturnsMinusOne() throws Exception {
        serial.scriptResponse("w", "no weight here" + CRLF);
        assertEquals(-1, scale.read(), 0.001);
    }

    @Test
    void testTareSendsTCommand() throws Exception {
        serial.scriptResponse("w", STABLE_0_05 + CRLF);
        Future<Boolean> tare = scale.tare();
        assertTrue(tare.get(10, TimeUnit.SECONDS));
        assertEquals("t", serial.getSentCommands().get(0));
    }

    // ------------------------------------------------------------------ framing

    @Test
    void testFragmentedFrameIsAssembled() throws Exception {
        // The scale regularly splits a reply across two serial events; the first fragment must not be
        // mistaken for a complete weight (this used to report 11.0 instead of 11.203).
        simulateLater(30, "      11.", "203 g  " + CRLF);
        assertEquals(11.203, scale.read(), 0.0001);
    }

    @Test
    void testPartialFrameIsNotReportedUntilTerminated() {
        serial.simulateData("      11.");
        assertNull(scale.getLastReading());
        serial.simulateData("203 g  " + CRLF);
        assertEquals(11.203, scale.getLastReading(), 0.0001);
    }

    @Test
    void testSeveralFramesInASingleChunk() {
        serial.simulateData(STABLE_12_345 + CRLF + STABLE_11_203 + CRLF);
        assertEquals(11.203, scale.getLastReading(), 0.0001);
    }

    @Test
    void testAbandonedFragmentDoesNotCorruptTheNextFrame() throws Exception {
        // A truncated reply left in the buffer would otherwise be glued to the following frame.
        serial.simulateData("      11.");
        Thread.sleep(300);
        serial.simulateData(STABLE_12_345 + CRLF);
        assertEquals(12.345, scale.getLastReading(), 0.0001);
    }

    @Test
    void testUnitIsUpdatedFromTheFrame() {
        serial.simulateData(STABLE_KILOGRAMS + CRLF);
        assertEquals("kg", scale.getUnit());
        assertEquals(1.5, scale.getLastReading(), 0.0001);
    }

    @Test
    void testUnstableFrameDoesNotClearTheKnownUnit() {
        serial.simulateData(STABLE_KILOGRAMS + CRLF);
        serial.simulateData(UNSTABLE_12_345 + CRLF);
        assertEquals("kg", scale.getUnit());
        assertFalse(scale.isStable());
    }

    // ------------------------------------------------------------------ streaming

    @Test
    void testEventReading() {
        double[] received = new double[1];
        scale.addReadingListener(w -> received[0] = w);
        scale.enableEventReading();
        assertTrue(scale.isEventReadingEnabled());

        serial.simulateData(STABLE_3_14 + CRLF);
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
        serial.simulateData(STABLE_3_14 + CRLF);
        assertEquals(-1, received[0], 0.001);

        // ...but the last reading is still tracked internally.
        assertEquals(3.14, scale.getLastReading(), 0.001);
    }
}
