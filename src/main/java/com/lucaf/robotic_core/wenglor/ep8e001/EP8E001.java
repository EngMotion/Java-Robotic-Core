package com.lucaf.robotic_core.wenglor.ep8e001;

import com.google.gson.Gson;
import com.lucaf.robotic_core.dataInterfaces.impl.RoutableInterface;
import com.lucaf.robotic_core.impl.MasterInterface;
import com.lucaf.robotic_core.wenglor.ep8e001.data.IOLinkData;
import com.lucaf.robotic_core.wenglor.ep8e001.data.IOLinkParameter;
import com.lucaf.robotic_core.wenglor.impl.IndexInterface;
import lombok.Getter;

import java.io.IOException;

public class EP8E001 extends MasterInterface implements IndexInterface  {

    final static String GET_PROCESS_DATA = "/iolink/v1/devices/%s/processdata/getdata/value?format=byteArray";
    final static String SET_PROCESS_DATA = "/iolink/v1/devices/%s/processdata/setdata/value?format=byteArray";
    final static String GET_PARAMETER_VALUE = "/iolink/v1/devices/%s/parameters/%s/value/?format=byteArray";
    final static String GET_PARAMETER_VALUE_SUBINDEX = "/iolink/v1/devices/%s/parameters/%s/subindices/%s/value/?format=byteArray";

    @Getter
    final RoutableInterface client;
    final Gson gson = new Gson();

    public EP8E001(RoutableInterface client) {
        this.client = client;
    }

    <T> T convertByteArrayToType(byte[] data, Class<T> type) {
        String json = new String(data);
        return gson.fromJson(json, type);
    }

    byte[] convertTypeToByteArray(Object obj) {
        String json = gson.toJson(obj);
        return json.getBytes();
    }

    @Override
    public int getMasterID() throws IOException {
        return client.getMasterID();
    }

    @Override
    public byte[] getProcessGetData(String device) throws IOException {
        IOLinkData data = convertByteArrayToType(
                client.get(String.format(GET_PROCESS_DATA, device)),
                IOLinkData.class
        );
        return data.getIolink().getValue();
    }

    @Override
    public byte[] getProcessSetData(String device) throws IOException {
        IOLinkData data = convertByteArrayToType(
                client.get(String.format(SET_PROCESS_DATA, device)),
                IOLinkData.class
        );
        return data.getIolink().getValue();
    }

    @Override
    public byte[] getParameterValue(String device, int index) throws IOException {
        IOLinkParameter data = convertByteArrayToType(
                client.get(String.format(GET_PARAMETER_VALUE, device, String.valueOf(index))),
                IOLinkParameter.class
        );
        return data.getValue();
    }

    @Override
    public byte[] getParameterValue(String device, int index, int subIndex) throws IOException {
        IOLinkParameter data = convertByteArrayToType(
                client.get(String.format(GET_PARAMETER_VALUE_SUBINDEX, device, String.valueOf(index), String.valueOf(subIndex))),
                IOLinkParameter.class
        );
        return data.getValue();
    }

    @Override
    public void setParameterValue(String device, int index, byte[] value) throws IOException {

    }

    @Override
    public void setParameterValue(String device, int index, int subIndex, byte[] value) throws IOException {

    }
}
