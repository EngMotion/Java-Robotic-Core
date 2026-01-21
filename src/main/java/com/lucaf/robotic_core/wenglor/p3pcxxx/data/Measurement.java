package com.lucaf.robotic_core.wenglor.p3pcxxx.data;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Measurement {
    public static enum Status {
        NO_SIGNAL,
        TOO_FAR,
        OK,
        TOO_CLOSE
    }
    Status status;
    int rawValue;
    int scale;
    int value;
    boolean warning;
    boolean warning1;
    boolean warning2;
    boolean warning3;
    boolean warning4;
    boolean error;
    boolean ssc1;
    boolean ssc2;

    public double getScaledValue() {
        return rawValue * Math.pow(10, scale);
    }

    @Override
    public String toString() {
        return "Measurement{" +
                "status=" + status +
                ", rawValue=" + rawValue +
                ", scale=" + scale +
                ", value=" + value +
                ", warning=" + warning +
                ", warning1=" + warning1 +
                ", warning2=" + warning2 +
                ", warning3=" + warning3 +
                ", warning4=" + warning4 +
                ", error=" + error +
                ", ssc1=" + ssc1 +
                ", ssc2=" + ssc2 +
                '}';
    }
}
