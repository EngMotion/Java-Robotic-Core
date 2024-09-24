package com.nanotec.nanolib.helper;

import java.io.IOException;

import com.nanotec.nanolib.BusHWIdVector;
import com.nanotec.nanolib.BusHardwareId;
import com.nanotec.nanolib.BusHardwareOptions;
import com.nanotec.nanolib.BusHwOptionsDefault;
import com.nanotec.nanolib.BusScanInfo;
import com.nanotec.nanolib.DeviceHandle;
import com.nanotec.nanolib.DeviceId;
import com.nanotec.nanolib.DeviceIdVector;
import com.nanotec.nanolib.IntVector;
import com.nanotec.nanolib.LogLevel;
import com.nanotec.nanolib.NanoLibAccessor;
import com.nanotec.nanolib.Nanolib;
import com.nanotec.nanolib.NlcErrorCode;
import com.nanotec.nanolib.NlcScanBusCallback;
import com.nanotec.nanolib.OdIndex;
import com.nanotec.nanolib.ProfinetDCP;
import com.nanotec.nanolib.Result;
import com.nanotec.nanolib.ResultBusHwIds;
import com.nanotec.nanolib.ResultDeviceHandle;
import com.nanotec.nanolib.ResultDeviceId;
import com.nanotec.nanolib.ResultDeviceIds;
import com.nanotec.nanolib.ResultInt;
import com.nanotec.nanolib.ResultSampleDataArray;
import com.nanotec.nanolib.ResultSamplerState;
import com.nanotec.nanolib.ResultString;
import com.nanotec.nanolib.ResultVoid;
import com.nanotec.nanolib.SampleDataVector;
import com.nanotec.nanolib.SamplerConfiguration;
import com.nanotec.nanolib.SamplerNotify;
import com.nanotec.nanolib.SamplerState;
import com.nanotec.nanolib.helper.LibraryLoader;

// <summary>
// Helper class used just to wrap around Nanolib.
// Of course, Nanolib can be used directly in the code.
// </summary>
public class NanolibHelper {
    private NanoLibAccessor nanolibAccessor;

    public void setup() throws NanolibException {
        if (nanolibAccessor == null) {
            try {
                nanolibAccessor = LibraryLoader.setup();
            } catch (IOException e) {
                throw new NanolibException(e.getMessage(), e);
            }
        } else {
            System.out.println("Nanolib accessor already initialized");
        }

    }

    /// <summary>
    /// Returns the nanolib accessor
    /// </summary>
    public NanoLibAccessor getAccessor() {
        return nanolibAccessor;
    }

    /// <summary>
    /// NanolibException class derived from Exception
    /// </summary>
    public class NanolibException extends Exception {
        private static final long serialVersionUID = 1744986371431153535L;

        public NanolibException(String message) {
            super(message);
        }

        public NanolibException(String message, Throwable inner) {
            super(message, inner);
        }

        public NanolibException() {
            super();
        }
    }

    /// <summary>
    /// Callback class derived from Nlc.NlcScanBusCallback.
    /// </summary>
    private class ScanBusCallback extends NlcScanBusCallback // override
    {
        /// <summary>
        /// Callback used in bus scanning.
        /// </summary>
        /// <param name="info">Scan process state</param>
        /// <param name="devicesFound">Devices found so far</param>
        /// <param name="data">Optional data, meaning depends on info.</param>
        /// <returns></returns>
        @Override
        public ResultVoid callback(BusScanInfo info, DeviceIdVector devicesFound, int data) {
            if (BusScanInfo.Start == info) {
                System.out.println("Scan started.");
            } else if (BusScanInfo.Progress == info) {
                if ((data & 1) == 0) // data holds scan progress
                {
                    System.out.print(".");
                }
            } else if (BusScanInfo.Finished == info) {
                System.out.println("");
                System.out.println("Scan finished.");
            }

            return new ResultVoid();
        }
    }

    /// <summary>
    /// Get the hardware objects from given accessor
    /// </summary>
    /// <returns>Array of HardwareIds</returns>
    public BusHWIdVector getBusHardware() throws NanolibException {
        ResultBusHwIds result = nanolibAccessor.listAvailableBusHardware();

        if (result.hasError()) {
            String errorMessage = String.format("Error: listAvailableBusHardware() - %s", result.getError());
            throw new NanolibException(errorMessage);
        }
        return result.getResult();
    }

    /// <summary>
    /// Create bus hardware options object from given bus hardware id
    /// </summary>
    /// <param name="busHwId">The id of the bus hardware taken from
    /// GetHardware()</param>
    /// <returns>A set of options for opening the bus hardware</returns>
    public BusHardwareOptions createBusHardwareOptions(BusHardwareId busHwId) {
        // create new bus hardware options
        BusHardwareOptions busHwOptions = new BusHardwareOptions();

        // now add all options necessary for opening the bus hardware
        // in case of CAN bus it is the baud rate
        BusHwOptionsDefault busHwOptionsDefaults = new BusHwOptionsDefault();

        // now add all options necessary for opening the bus hardware
        if (busHwId.getProtocol().equals(Nanolib.getBUS_HARDWARE_ID_PROTOCOL_CANOPEN())) {
            // in case of CAN bus it is the baud rate
            busHwOptions.addOption(busHwOptionsDefaults.getCanBus().getBAUD_RATE_OPTIONS_NAME(),
                    busHwOptionsDefaults.getCanBus().getBaudRate().getBAUD_RATE_1000K());

            if (busHwId.getBusHardware().equals(Nanolib.getBUS_HARDWARE_ID_IXXAT())) {
                // in case of HMS IXXAT we need also bus number
                busHwOptions.addOption(busHwOptionsDefaults.getCanBus().getIxxat().getADAPTER_BUS_NUMBER_OPTIONS_NAME(),
                        busHwOptionsDefaults.getCanBus().getIxxat().getAdapterBusNumber().getBUS_NUMBER_0_DEFAULT());
            }
        } else if (busHwId.getProtocol().equals(Nanolib.getBUS_HARDWARE_ID_PROTOCOL_MODBUS_RTU())) {
            // in case of Modbus RTU it is the serial baud rate
            busHwOptions.addOption(busHwOptionsDefaults.getSerial().getBAUD_RATE_OPTIONS_NAME(),
                    busHwOptionsDefaults.getSerial().getBaudRate().getBAUD_RATE_19200());
            // and serial parity
            busHwOptions.addOption(busHwOptionsDefaults.getSerial().getPARITY_OPTIONS_NAME(),
                    busHwOptionsDefaults.getSerial().getParity().getEVEN());
        }

        return busHwOptions;
    }

    /// <summary>
    /// Opens the bus hardware with given id and options
    /// </summary>
    /// <param name="busHwId">The id of the bus hardware taken from
    /// GetHardware()</param>
    /// <param name="busHwOptions">The hardware options taken from
    /// Create.....BusHardwareOptions()</param>
    public void openBusHardware(BusHardwareId busHwId, BusHardwareOptions busHwOptions) throws NanolibException {
        Result result = nanolibAccessor.openBusHardwareWithProtocol(busHwId, busHwOptions);

        if (result.hasError()) {
            String errorMsg = String.format("Error: openBusHardwareWithProtocol() - %s", result.getError());
            throw new NanolibException(errorMsg);
        }
    }

    /// <summary>
    /// Closes the bus hardware (access no longer possible after that)
    /// Note: the call of the function is optional because the nanolib will cleanup
    /// the
    /// bus hardware itself on closing.
    /// </summary>
    /// <param name="busHwId">The bus hardware id to close</param>
    public void closeBusHardware(BusHardwareId busHwId) throws NanolibException {
        Result result = nanolibAccessor.closeBusHardware(busHwId);

        if (result.hasError()) {
            String errorMsg = String.format("Error: closeBusHardware() - %s", result.getError());
            throw new NanolibException(errorMsg);
        }
    }

    /// <summary>
    /// Scans bus and returns all found device ids.
    ///
    /// CAUTION: open bus hardware first with NanoLibHelper.OpenBusHardware()
    ///
    /// </summary>
    /// <param name="busHwId">The bus hardware to scan</param>
    /// <returns>Vector with found devices</returns>
    public DeviceIdVector scanBus(BusHardwareId busHwId) throws NanolibException {
        ScanBusCallback scanCallback = new ScanBusCallback();

        ResultDeviceIds result = nanolibAccessor.scanDevices(busHwId, scanCallback);

        if (result.hasError()) {
            String errorMsg = String.format("Error: scanDevices() - %s", result.getError());
            throw new NanolibException(errorMsg);
        }

        return result.getResult();
    }

    /// <summary>
    /// Create a Device and return DeviceHandle
    /// </summary>
    /// <param name="deviceId">The device id</param>
    /// <returns>The DeviceHandle used to access all device related
    /// functions</returns>
    public DeviceHandle createDevice(DeviceId deviceId) throws NanolibException {
        ResultDeviceHandle result = nanolibAccessor.addDevice(deviceId);

        if (result.hasError()) {
            String errorMsg = String.format("Error: CreateDevice() - %s", result.getError());
            throw new NanolibException(errorMsg);
        }

        return result.getResult();
    }

    /// <summary>
    /// Connects to given DeviceHandle
    /// </summary>
    /// <param name="deviceHandle"></param>
    public void connectDevice(DeviceHandle deviceHandle) throws NanolibException {
        ResultVoid result = nanolibAccessor.connectDevice(deviceHandle);

        if (result.hasError()) {
            throw new NanolibException("Error: ConnectDevice() - " + result.getError());
        }
    }

    /// <summary>
    /// Disconnects given device
    ///
    /// Note: the call of the function is optional because the nanolib will cleanup
    /// the
    /// devices on bus itself on closing.
    /// </summary>
    /// <param name="deviceHandle">DeviceHandle of the device</param>
    public void disconnectDevice(DeviceHandle deviceHandle) throws NanolibException {
        ResultVoid result = nanolibAccessor.disconnectDevice(deviceHandle);

        if (result.hasError()) {
            throw new NanolibException("Error: DisconnectDevice() - " + result.getError());
        }
    }

    /// <summary>
    /// Reads out a number of given device
    /// </summary>
    /// <param name="deviceHandle">The handle of the device to read from</param>
    /// <param name="odIndex">The index and sub-index of the object dictionary to
    /// read from</param>
    /// <returns>A 64 bit number. The interpretation of the data type is up to the
    /// user. </returns>
    public long readNumber(DeviceHandle deviceHandle, OdIndex odIndex) throws NanolibException {
        ResultInt result = nanolibAccessor.readNumber(deviceHandle, odIndex);

        if (result.hasError()) {
            String errorMsg = createErrorMessage("Reading number", deviceHandle, odIndex, result.getError());
            throw new NanolibException(errorMsg);
        }
        return result.getResult();
    }

    /// <summary>
    /// Writes given number to the device
    /// </summary>
    /// <param name="deviceHandle">The id of the device to write to</param>
    /// <param name="value">The value to write to the device</param>
    /// <param name="odIndex">The index and sub-index of the object dictionary to
    /// write to</param>
    /// <param name="bitLength">The bit length of the object to write to, either 8,
    /// 16 or 32 (see manual for all the bit lengths of all objects)</param>
    public void writeNumber(DeviceHandle deviceHandle, long value, OdIndex odIndex, int bitLength)
            throws NanolibException {
        ResultVoid result = nanolibAccessor.writeNumber(deviceHandle, value, odIndex, bitLength);

        if (result.hasError()) {
            String errorMsg = createErrorMessage("Writing number", deviceHandle, odIndex, result.getError());
            throw new NanolibException(errorMsg);
        }
    }

    /// <summary>
    /// Reads array object from a device
    /// </summary>
    /// <param name="deviceHandle">The handle of the device to read from</param>
    /// <param name="index">The index of the object</param>
    /// <returns>Vector (array) of numbers (the interpretation of the data type is
    /// up to the user).</returns>
    public IntVector readArray(DeviceHandle deviceHandle, int index) throws NanolibException {
        var result = nanolibAccessor.readNumberArray(deviceHandle, index);

        if (result.hasError()) {
            String errorMsg = createErrorMessage("Reading array", deviceHandle, new OdIndex(index, (short) 0),
                    result.getError());
            throw new NanolibException(errorMsg);
        }
        return result.getResult();
    }

    /// <summary>
    /// Reads a string object from a devicew
    /// </summary>
    /// <param name="deviceHandle"></param>
    /// <param name="odIndex">The index and sub-index of the object dictionary to
    /// read from</param>
    /// <returns>string</returns>
    public String readString(DeviceHandle deviceHandle, OdIndex odIndex) throws NanolibException {
        ResultString result = nanolibAccessor.readString(deviceHandle, odIndex);

        if (result.hasError()) {
            String errorMsg = createErrorMessage("Reading string", deviceHandle, odIndex, result.getError());
            throw new NanolibException(errorMsg);
        }
        return result.getResult();
    }

    /// <summary>
    /// Set the logging level
    /// </summary>
    /// <param name="logLevel">Nanolib log Level</param>
    public void setLoggingLevel(LogLevel logLevel) throws NanolibException {
        if (nanolibAccessor == null) {
            throw new NanolibException("Error: NanolibHelper().setup() is required");
        }
        nanolibAccessor.setLoggingLevel(logLevel);
    }

    /// <summary>
    /// Helper function for creating an error message from given objects
    /// </summary>
    /// <param name="function">The name of the function the error ocurred</param>
    /// <param name="deviceHandle">The handle of the device to read from</param>
    /// <param name="odIndex">The index and sub-index of the object
    /// dictionary</param>
    /// <param name="resultError">The error text of the result</param>
    /// <returns></returns>
    private String createErrorMessage(String function, DeviceHandle deviceHandle, OdIndex odIndex, String resultError) {
        String deviceIdstring;
        ResultDeviceId resultDeviceId = nanolibAccessor.getDeviceId(deviceHandle);
        if (resultDeviceId.hasError()) {
            deviceIdstring = "invalid handle";
        } else {
            deviceIdstring = resultDeviceId.getResult().toString();
        }

        return String.format("%s of device %s at od index %s resulted in an error: %s", function, deviceIdstring,
                Integer.toString(odIndex.getIndex()), resultError);
    }

    /// <summary>
    /// Gets the Profinet DCP interface from accessor
    /// </summary>
    /// <returns>ProfinetDCP</returns>
    public ProfinetDCP getProfinetDcpInterface()
    {
        return nanolibAccessor.getProfinetDCP();
    }

    /// <summary>
    /// Stops the sampling process
    /// </summary>
    /// <param name="deviceHandle">The handle of the device</param>
    /// <returns>None</returns>
    public void stopSampler(DeviceHandle deviceHandle) throws NanolibException {
        ResultVoid result = nanolibAccessor.getSamplerInterface().stop(deviceHandle);
        if (result.hasError() && (result.getErrorCode() != NlcErrorCode.InvalidOperation)) {
            String errorMsg = createErrorMessage("SamplerInterface::stop", deviceHandle, new OdIndex(0, (short) 0), result.getError());
            throw new NanolibException(errorMsg);
        }
    }

    /// <summary>
    /// Configures the sampling process
    /// </summary>
    /// <param name="deviceHandle">The handle of the device</param>
    /// <param name="samplerConfiguration">The sampler configuration</param>
    /// <returns>None</returns>
    public void configureSampler(DeviceHandle deviceHandle, SamplerConfiguration samplerConfiguration) throws NanolibException {
        ResultVoid result = nanolibAccessor.getSamplerInterface().configure(deviceHandle, samplerConfiguration);
        if (result.hasError() && (result.getErrorCode() != NlcErrorCode.InvalidOperation)) {
            String errorMsg = createErrorMessage("SamplerInterface::configure", deviceHandle, new OdIndex(0, (short) 0), result.getError());
            throw new NanolibException(errorMsg);
        }
    }

    /// <summary>
    /// Starts the sampling process
    /// </summary>
    /// <param name="deviceHandle">The handle of the device</param>
    /// <param name="samplerNotify">notify callback</param>
    /// <param name="applicationData">apllication specific data</param>
    /// <returns>None</returns>
    public void startSampler(DeviceHandle deviceHandle, SamplerNotify samplerNotify, long applicationData) throws NanolibException {
        ResultVoid result = nanolibAccessor.getSamplerInterface().start(deviceHandle, samplerNotify, applicationData);
        if (result.hasError() && (result.getErrorCode() != NlcErrorCode.InvalidOperation)) {
            String errorMsg = createErrorMessage("SamplerInterface::start", deviceHandle, new OdIndex(0, (short) 0), result.getError());
            throw new NanolibException(errorMsg);
        }
    }

    /// <summary>
    /// Returns the sampler state for the device
    /// </summary>
    /// <param name="deviceHandle">The handle of the device</param>
    /// <returns>SamplerState</returns>
    public SamplerState getSamplerState(DeviceHandle deviceHandle) throws NanolibException {
        ResultSamplerState result = nanolibAccessor.getSamplerInterface().getState(deviceHandle);
        if (result.hasError()) {
            String errorMsg = createErrorMessage("SamplerInterface::getState", deviceHandle, new OdIndex(0, (short) 0), result.getError());
            throw new NanolibException(errorMsg);
        }

        return result.getResult();
    }

    /// <summary>
    /// Returns the sampled datas as a vector for a device
    /// </summary>
    /// <param name="deviceHandle">The handle of the device</param>
    /// <returns>SampleDataVector</returns>
    public SampleDataVector getSamplerData(DeviceHandle deviceHandle) throws NanolibException {
        ResultSampleDataArray result = nanolibAccessor.getSamplerInterface().getData(deviceHandle);
        if (result.hasError()) {
            String errorMsg = createErrorMessage("SamplerInterface::getData", deviceHandle, null, result.getError());
            throw new NanolibException(errorMsg);
        }

        return result.getResult();
    }

    /// <summary>
    /// Returns the last occured error for a device
    /// </summary>
    /// <param name="deviceHandle">The handle of the device</param>
    /// <returns>ResultVoid</returns>
    public ResultVoid getSamplerLastError(DeviceHandle deviceHandle) {
        return nanolibAccessor.getSamplerInterface().getLastError(deviceHandle);
    }
}
