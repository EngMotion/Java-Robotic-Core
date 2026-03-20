package com.lucaf.robotic_core.wenglor.impl;

import com.lucaf.robotic_core.dataInterfaces.impl.RoutableInterface;
import com.lucaf.robotic_core.wenglor.ep8e001.data.IOLinkData;
import com.lucaf.robotic_core.wenglor.ep8e001.data.IOLinkParameter;

import java.io.IOException;

public interface IndexInterface {
    RoutableInterface getClient();
    int    getMasterID() throws IOException;
    IOLinkData getProcessGetData(String device) throws IOException;
    IOLinkData getProcessSetData(String device) throws IOException;
    IOLinkParameter getParameterValue(String device, int index) throws IOException;
    IOLinkParameter getParameterValue(String device, int index, int subIndex) throws IOException;
    void   setParameterValue(String device, int index, byte[] value) throws IOException;
    void   setParameterValue(String device, int index, int subIndex, byte[] value) throws IOException;
}
