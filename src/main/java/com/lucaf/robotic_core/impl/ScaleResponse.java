package com.lucaf.robotic_core.impl;

import lombok.Getter;

/**
 * A decoded response frame: either a weight (stable or not) or the system error report.
 */
@Getter
public class ScaleResponse {

    /**
     * The weight carried by the frame, or {@code null} for an error frame.
     */
    private final Double weight;

    /**
     * The unit reported by the frame, empty when the frame carries none.
     */
    private final String unit;

    /**
     * Whether the weight is settled, which the scale signals by filling in the unit field.
     */
    private final boolean stable;

    /**
     * Whether the frame is the {@code "Error"} report rather than a weight.
     */
    private final boolean error;

    private ScaleResponse(Double weight, String unit, boolean stable, boolean error) {
        this.weight = weight;
        this.unit = unit;
        this.stable = stable;
        this.error = error;
    }

    /**
     * Builds a weight frame.
     * <p>
     * Stability is passed in rather than derived, because every scale family signals it differently:
     * the KERN PCB blanks out the unit field while the weight is moving, whereas the KERN PLJ carries
     * a dedicated stability character at the end of the frame.
     *
     * @param weight the decoded weight
     * @param unit   the unit, empty when the scale left the field blank
     * @param stable whether the device reported the weight as settled
     * @return a weight frame
     */
    public static ScaleResponse weight(double weight, String unit, boolean stable) {
        return new ScaleResponse(weight, unit, stable, false);
    }

    /**
     * @return the frame reporting a scale system error
     */
    public static ScaleResponse error() {
        return new ScaleResponse(null, "", false, true);
    }
}
