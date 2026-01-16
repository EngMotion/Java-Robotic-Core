package com.lucaf.robotic_core.wenglor.ep8e001;

import com.lucaf.robotic_core.impl.MasterInterface;
import com.lucaf.robotic_core.wenglor.impl.IndexInterface;

import java.io.IOException;

public class EP8E001 extends MasterInterface implements IndexInterface  {

    @Override
    public int getMasterID() throws IOException {
        return 0;
    }

    @Override
    public byte[] getProcessGetData(String device) throws IOException {
        return new byte[0];
    }

    @Override
    public byte[] getProcessSetData(String device) throws IOException {
        return new byte[0];
    }

    @Override
    public byte[] getParameterValue(String device, int index) throws IOException {
        return new byte[0];
    }

    @Override
    public byte[] getParameterValue(String device, int index, int subIndex) throws IOException {
        return new byte[0];
    }

    @Override
    public void setParameterValue(String device, int index, byte[] value) throws IOException {

    }

    @Override
    public void setParameterValue(String device, int index, int subIndex, byte[] value) throws IOException {

    }
}
