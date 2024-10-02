package com.lucaf.robotic_core.NANOTEC.PD4E_RTU;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DigitalOutputs {

    /**
     * Bit 0: Brake
     */
    private boolean brake = false;

    /**
     * Bit 16: Output 1
     */
    private boolean output1 = false;

    /**
     * Bit 17: Output 2
     */
    private boolean output2 = false;

    /**
     * Bit 18: Output 3
     */
    private boolean output3 = false;

    /**
     * Bit 19: Output 4
     */
    private boolean output4 = false;

    public DigitalOutputs(int n){
        brake = (n & 1) == 1;
        output1 = (n & (1 << 16)) == (1 << 16);
        output2 = (n & (1 << 17)) == (1 << 17);
        output3 = (n & (1 << 18)) == (1 << 18);
        output4 = (n & (1 << 19)) == (1 << 19);
    }

    public DigitalOutputs(){
    }

    public int toInt(){
        int n = 0;
        if(brake) n |= 1;
        if(output1) n |= (1 << 16);
        if(output2) n |= (1 << 17);
        if(output3) n |= (1 << 18);
        if(output4) n |= (1 << 19);
        return n;
    }

    @Override
    public String toString(){
        return "Brake: " + brake + ", Output1: " + output1 + ", Output2: " + output2 + ", Output3: " + output3 + ", Output4: " + output4;
    }

}
