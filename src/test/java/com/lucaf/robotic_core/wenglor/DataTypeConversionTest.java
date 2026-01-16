package com.lucaf.robotic_core.wenglor;

import com.lucaf.robotic_core.wenglor.impl.DataTypeConversion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataTypeConversionTest extends DataTypeConversion {
    @Test
    public void testBytesToString() {
        byte[] data = {119, 101, 110, 103, 108, 111, 114, 32, 115, 101, 110, 115, 111, 114, 105, 99, 32, 71, 109, 98, 72};
        String result = byteArrayToString(data);
        assertEquals("wenglor sensoric GmbH", result);
    }

    @Test
    public void testStringToBytes() {
        String value = "wenglor sensoric GmbH";
        byte[] expected = {119, 101, 110, 103, 108, 111, 114, 32, 115, 101, 110, 115, 111, 114, 105, 99, 32, 71, 109, 98, 72};
        byte[] result = stringToByteArray(value, 21);
        assertEquals(expected.length, result.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result[i]);
        }
    }

    @Test
    public void testIntConversion() {
        int original = 305419896; // 0x12345678
        byte[] byteArray = intToByteArray(original, 4);
        int result = byteArrayToInt(byteArray);
        assertEquals(original, result);
    }

    @Test
    public void testLongConversion() {
        long original = 1311768467463790320L; // 0x1234567890ABCDEF0
        byte[] byteArray = longToByteArray(original, 8);
        long result = byteArrayToLong(byteArray);
        assertEquals(original, result);
    }
}
