package com.lucaf.robotic_core.NANOTEC.PD4E_RTU;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Class that contains the unit control of the PD4E.
 */
@Getter
@Setter
@NoArgsConstructor
public class UnitControl {
    /**
     * Class that contains the units of the PD4E.
     */
    public static class Units {
        public static final byte METER = (byte) 0x01;
        public static final byte INCH = (byte) 0xC1;
        public static final byte FOOT = (byte) 0xC2;
        public static final byte GRADE = (byte) 0x40;
        public static final byte RADIAN = (byte) 0x10;
        public static final byte DEGREE = (byte) 0x41;
        public static final byte ARCMINUTE = (byte) 0x42;
        public static final byte ARCSECOND = (byte) 0x43;
        public static final byte MECHANICAL_REVOLUTION = (byte) 0xB4;
        public static final byte ENCODER = (byte) 0xB5;
        public static final byte STEP = (byte) 0xAC;
        public static final byte ELECTRICAL_POLE = (byte) 0xC0;
        public static final byte DIMENSIONS = (byte) 0x00;
    }

    /**
     * Function that gets the factor 10^n for the unit.
     *
     * @param n The power of 10.
     * @return The factor 10^n in byte unit
     */
    public static byte FACTOR(int n) {
        n = Math.min(6, n);
        n = Math.max(-6, n);
        if (n >= 0) {
            return (byte) n;
        } else {
            return (byte) (256 + n);
        }
    }

    /**
     * Byte 31-24, factor
     */
    private byte factor;

    /**
     * Byte 23-16, unit
     */
    private byte unit;

    /**
     * Byte 15-18, reserved
     */
    private short reserved1;

    /**
     * Byte 7-0, reserved
     */
    private short reserved2;

    /**
     * Constructor of the class.
     *
     * @param register The register of 32 bit.
     */
    public UnitControl(int register) {
        this.factor = (byte) ((register >> 24) & 0xFF);
        this.unit = (byte) ((register >> 16) & 0xFF);
        this.reserved1 = (short) ((register >> 8) & 0xFF);
        this.reserved2 = (short) (register & 0xFF);
    }

    /**
     * Method that converts the unit control to a 32 bit register.
     *
     * @return The register of 32 bit.
     */
    public int toInt() {
        return ((factor & 0xFF) << 24) | ((unit & 0xFF) << 16) | ((reserved1 & 0xFF) << 8) | (reserved2 & 0xFF);
    }

    @Override
    public String toString() {
        return "UnitControl{" +
                "factor=" + Integer.toHexString(factor) + " (10^" + factor + ")" +
                ", unit=" + Integer.toHexString(unit) +
                ", reserved1=" + reserved1 +
                ", reserved2=" + reserved2 +
                '}';
    }
}
