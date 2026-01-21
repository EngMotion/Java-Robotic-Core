package com.lucaf.robotic_core.wenglor.impl;

import com.lucaf.robotic_core.dataInterfaces.impl.RoutableInterface;

import java.io.IOException;

public interface IndexInterface {
    RoutableInterface getClient();
    int    getMasterID() throws IOException;
    byte[] getProcessGetData(String device) throws IOException;
    byte[] getProcessSetData(String device) throws IOException;
    byte[] getParameterValue(String device, int index) throws IOException;
    byte[] getParameterValue(String device, int index, int subIndex) throws IOException;
    void   setParameterValue(String device, int index, byte[] value) throws IOException;
    void   setParameterValue(String device, int index, int subIndex, byte[] value) throws IOException;
}
