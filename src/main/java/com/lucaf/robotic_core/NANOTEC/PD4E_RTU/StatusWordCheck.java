package com.lucaf.robotic_core.NANOTEC.PD4E_RTU;

/**
 * Interface that represents the status word check
 */
public interface StatusWordCheck {

    /**
     * Method that checks the status word for acknowledge
     * @param statusWord the status word to check
     * @return true if the control word is ready, false otherwise
     */
    boolean checkStatusWord(StatusWord statusWord);
}
