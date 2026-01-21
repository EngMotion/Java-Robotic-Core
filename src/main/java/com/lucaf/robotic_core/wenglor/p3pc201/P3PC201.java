package com.lucaf.robotic_core.wenglor.p3pc201;

import com.lucaf.robotic_core.impl.SensorInterface;
import com.lucaf.robotic_core.wenglor.impl.DataTypeConversion;
import com.lucaf.robotic_core.wenglor.impl.IndexInterface;
import com.lucaf.robotic_core.wenglor.p3pcxxx.data.Measurement;

import java.io.IOException;

import static com.lucaf.robotic_core.wenglor.p3pcxxx.Indexes.*;

public class P3PC201 extends SensorInterface {
    final IndexInterface master;
    final String deviceName;
    final DataTypeConversion conversion = new DataTypeConversion();

    public P3PC201(IndexInterface master, String deviceName) {
        this.master = master;
        this.deviceName = deviceName;
    }

    public String getVendorName() throws IOException {
        byte[] data = master.getParameterValue(deviceName, VENDOR_NAME);
        return conversion.byteArrayToString(data);
    }

    public String getVendorText() throws IOException {
        byte[] data = master.getParameterValue(deviceName, VENDOR_TEXT);
        return conversion.byteArrayToString(data);
    }

    public String getProductName() throws IOException {
        byte[] data = master.getParameterValue(deviceName, PRODUCT_NAME);
        return conversion.byteArrayToString(data);
    }

    public String getProductId() throws IOException {
        byte[] data = master.getParameterValue(deviceName, PRODUCT_ID);
        return conversion.byteArrayToString(data);
    }

    public String getProductText() throws IOException {
        byte[] data = master.getParameterValue(deviceName, PRODUCT_TEXT);
        return conversion.byteArrayToString(data);
    }

    public String getSerialNumber() throws IOException {
        byte[] data = master.getParameterValue(deviceName, SERIAL_NUMBER);
        return conversion.byteArrayToString(data);
    }

    public String getHardwareVersion() throws IOException {
        byte[] data = master.getParameterValue(deviceName, HARDWARE_VERSION);
        return conversion.byteArrayToString(data);
    }

    public String getFirmwareVersion() throws IOException {
        byte[] data = master.getParameterValue(deviceName, FIRMWARE_VERSION);
        return conversion.byteArrayToString(data);
    }

    public String getApplicationSpecificTag() throws IOException {
        byte[] data = master.getParameterValue(deviceName, APPLICATION_SPECIFIC_TAG);
        return conversion.byteArrayToString(data);
    }

    public String getFunctionTag() throws IOException {
        byte[] data = master.getParameterValue(deviceName, FUNCTION_TAG);
        return conversion.byteArrayToString(data);
    }

    public String getLocationTag() throws IOException {
        byte[] data = master.getParameterValue(deviceName, LOCATION_TAG);
        return conversion.byteArrayToString(data);
    }


    public Measurement getMeasurement() throws IOException {
        byte[] data = master.getProcessGetData(deviceName);
        byte[] value = new byte[4];
        System.arraycopy(data, 0, value, 0, 4);
        int readValue = conversion.byteArrayToInt(value);
        return Measurement.builder()
                .status(
                        readValue >= 2147483644 ? Measurement.Status.NO_SIGNAL :
                                readValue >= 2147483640 ? Measurement.Status.TOO_FAR :
                                        readValue <= -2147483640 ? Measurement.Status.TOO_CLOSE :
                                                Measurement.Status.OK
                )
                .rawValue(readValue)
                .scale(data[4])
                .warning1((data[5] & (1 << 0)) != 0)
                .warning2((data[5] & (1 << 1)) != 0)
                .warning3((data[5] & (1 << 2)) != 0)
                .warning4((data[5] & (1 << 3)) != 0)
                .error((data[5] & (1 << 4)) != 0)
                .warning((data[5] & (1 << 5)) != 0)
                .ssc1((data[5] & (1 << 6)) != 0)
                .ssc2((data[5] & (1 << 7)) != 0)
                .build();
    }

    public void dumpToConsole() {
        master.getClient().logInfo("--- P3PC201 Device Dump ---");
        try {
            master.getClient().logInfo("Vendor Name: " + getVendorName());
            master.getClient().logInfo("Vendor Text: " + getVendorText());
            master.getClient().logInfo("Product Name: " + getProductName());
            master.getClient().logInfo("Product ID: " + getProductId());
            master.getClient().logInfo("Product Text: " + getProductText());
            master.getClient().logInfo("Serial Number: " + getSerialNumber());
            master.getClient().logInfo("Hardware Version: " + getHardwareVersion());
            master.getClient().logInfo("Firmware Version: " + getFirmwareVersion());
            master.getClient().logInfo("Application Tag: " + getApplicationSpecificTag());
            master.getClient().logInfo("Function Tag: " + getFunctionTag());
            master.getClient().logInfo("Location Tag: " + getLocationTag());
            master.getClient().logInfo("--- Measure Data ---");
            Measurement measurement = getMeasurement();
            master.getClient().logInfo("Measurement distance: " + measurement.getRawValue());
            master.getClient().logInfo("Measurement distance: " + measurement.getScaledValue() + "m");
            master.getClient().logInfo("Measurement scale: " + measurement.getScale());
            master.getClient().logInfo("Measurement status: " + measurement.getStatus());
            master.getClient().logInfo("Measurement warning: " + measurement.isWarning());
            master.getClient().logInfo("Measurement warning1: " + measurement.isWarning1());
            master.getClient().logInfo("Measurement warning2: " + measurement.isWarning2());
            master.getClient().logInfo("Measurement warning3: " + measurement.isWarning3());
            master.getClient().logInfo("Measurement warning4: " + measurement.isWarning4());
            master.getClient().logInfo("Measurement error: " + measurement.isError());
            master.getClient().logInfo("Measurement ssc1: " + measurement.isSsc1());
            master.getClient().logInfo("Measurement ssc2: " + measurement.isSsc2());

        } catch (IOException e) {
            master.getClient().logError("Error during dump: " + e.getMessage());
        }
    }
}
