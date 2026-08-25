package com.lucaf.robotic_core.KERN.ui;

import com.lucaf.robotic_core.KERN.PCB.PCB_3;
import com.lucaf.robotic_core.KERN.PLJ.PLJ_1200;
import com.lucaf.robotic_core.dataInterfaces.impl.SerialInterface;
import com.lucaf.robotic_core.impl.ScaleInterface;
import com.lucaf.robotic_core.impl.ScaleResponse;

import java.util.function.Consumer;

/**
 * The KERN scale families supported by {@link ScaleTesterUI}, together with the hints the operator
 * needs to put the device in the right mode and the factory that builds the matching driver.
 */
public enum KernScaleType {

    /**
     * KERN PCB series: polled scale, answers {@code "w"} / {@code "s"} / {@code "t"}.
     */
    PCB("KERN PCB",
            "Polling: 'w' = peso, 's' = peso stabile, 't' = tara.",
            "Imposta la bilancia in modalità di trasmissione \"rE CR\" (menu → Pr → rE CR), "
                    + "altrimenti ignora i comandi remoti.") {
        @Override
        public ScaleInterface create(SerialInterface serial, Consumer<ScaleResponse> readingConsumer) {
            return new PCB_3(serial, readingConsumer);
        }
    },

    /**
     * KERN PLJ precision balance: transmits continuously, answers {@code "E"} with a stable weight.
     */
    PLJ("KERN PLJ",
            "Trasmissione continua: 'E' = peso stabile, 'T' = tara, 'C' = registra, 'M' = menu, "
                    + "'O' = on/off.",
            "La bilancia deve essere in trasmissione continua: il pulsante Leggi non invia nulla e "
                    + "restituisce l'ultimo valore arrivato, mentre Leggi stabile invia 'E'.") {
        @Override
        public ScaleInterface create(SerialInterface serial, Consumer<ScaleResponse> readingConsumer) {
            return new PLJ_1200(serial, readingConsumer);
        }
    };

    /**
     * Human readable name shown in the UI.
     */
    private final String label;

    /**
     * One-line summary of the command set.
     */
    private final String protocolSummary;

    /**
     * Setup requirement the operator has to satisfy on the device itself.
     */
    private final String setupHint;

    /**
     * @param label           human readable name shown in the UI
     * @param protocolSummary one-line summary of the command set
     * @param setupHint       setup requirement the operator has to satisfy on the device
     */
    KernScaleType(String label, String protocolSummary, String setupHint) {
        this.label = label;
        this.protocolSummary = protocolSummary;
        this.setupHint = setupHint;
    }

    /**
     * Builds the driver for this scale family.
     *
     * @param serial          the serial interface connected to the scale
     * @param readingConsumer consumer notified with every streamed reading (may be {@code null})
     * @return the scale driver
     */
    public abstract ScaleInterface create(SerialInterface serial, Consumer<ScaleResponse> readingConsumer);

    /**
     * Returns the one-line summary of the command set.
     *
     * @return the protocol summary
     */
    public String getProtocolSummary() {
        return protocolSummary;
    }

    /**
     * Returns the setup requirement the operator has to satisfy on the device itself.
     *
     * @return the setup hint
     */
    public String getSetupHint() {
        return setupHint;
    }

    /**
     * Returns the human readable name, so that the enum can be dropped straight into a combo box.
     *
     * @return the label shown in the UI
     */
    @Override
    public String toString() {
        return label;
    }
}
