/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.nanotec.nanolib;

public class NanoLibAccessor {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected NanoLibAccessor(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(NanoLibAccessor obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(NanoLibAccessor obj) {
    long ptr = 0;
    if (obj != null) {
      if (!obj.swigCMemOwn)
        throw new RuntimeException("Cannot release ownership as memory is not owned");
      ptr = obj.swigCPtr;
      obj.swigCMemOwn = false;
      obj.delete();
    }
    return ptr;
  }

  @SuppressWarnings("deprecation")
  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        NanolibJNI.delete_NanoLibAccessor(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setLoggingLevel(LogLevel level) {
    NanolibJNI.NanoLibAccessor_setLoggingLevel(swigCPtr, this, level.swigValue());
  }

  public void setLoggingCallback(NlcLoggingCallback callback, LogModule logModule) {
    NanolibJNI.NanoLibAccessor_setLoggingCallback(swigCPtr, this, NlcLoggingCallback.getCPtr(callback), callback, logModule.swigValue());
  }

  public void unsetLoggingCallback() {
    NanolibJNI.NanoLibAccessor_unsetLoggingCallback(swigCPtr, this);
  }

  public ResultBusHwIds listAvailableBusHardware() {
    return new ResultBusHwIds(NanolibJNI.NanoLibAccessor_listAvailableBusHardware(swigCPtr, this), true);
  }

  public ResultVoid openBusHardwareWithProtocol(BusHardwareId busHwId, BusHardwareOptions busHwOpt) {
    return new ResultVoid(NanolibJNI.NanoLibAccessor_openBusHardwareWithProtocol(swigCPtr, this, BusHardwareId.getCPtr(busHwId), busHwId, BusHardwareOptions.getCPtr(busHwOpt), busHwOpt), true);
  }

  public ResultVoid closeBusHardware(BusHardwareId busHwId) {
    return new ResultVoid(NanolibJNI.NanoLibAccessor_closeBusHardware(swigCPtr, this, BusHardwareId.getCPtr(busHwId), busHwId), true);
  }

  public ResultVoid setBusState(BusHardwareId busHwId, String state) {
    return new ResultVoid(NanolibJNI.NanoLibAccessor_setBusState(swigCPtr, this, BusHardwareId.getCPtr(busHwId), busHwId, state), true);
  }

  public ResultDeviceHandle addDevice(DeviceId deviceId) {
    return new ResultDeviceHandle(NanolibJNI.NanoLibAccessor_addDevice(swigCPtr, this, DeviceId.getCPtr(deviceId), deviceId), true);
  }

  public ResultVoid removeDevice(DeviceHandle deviceHandle) {
    return new ResultVoid(NanolibJNI.NanoLibAccessor_removeDevice(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle), true);
  }

  public ResultDeviceId getDeviceId(DeviceHandle deviceHandle) {
    return new ResultDeviceId(NanolibJNI.NanoLibAccessor_getDeviceId(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle), true);
  }

  public ResultDeviceIds getDeviceIds() {
    return new ResultDeviceIds(NanolibJNI.NanoLibAccessor_getDeviceIds(swigCPtr, this), true);
  }

  public ResultVoid connectDevice(DeviceHandle deviceHandle) {
    return new ResultVoid(NanolibJNI.NanoLibAccessor_connectDevice(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle), true);
  }

  public ResultVoid disconnectDevice(DeviceHandle deviceHandle) {
    return new ResultVoid(NanolibJNI.NanoLibAccessor_disconnectDevice(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle), true);
  }

  public ResultVoid rebootDevice(DeviceHandle deviceHandle) {
    return new ResultVoid(NanolibJNI.NanoLibAccessor_rebootDevice(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle), true);
  }

  public ResultInt getDeviceVendorId(DeviceHandle deviceHandle) {
    return new ResultInt(NanolibJNI.NanoLibAccessor_getDeviceVendorId(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle), true);
  }

  public ResultInt getDeviceProductCode(DeviceHandle deviceHandle) {
    return new ResultInt(NanolibJNI.NanoLibAccessor_getDeviceProductCode(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle), true);
  }

  public ResultString getDeviceName(DeviceHandle deviceHandle) {
    return new ResultString(NanolibJNI.NanoLibAccessor_getDeviceName(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle), true);
  }

  public ResultString getDeviceHardwareVersion(DeviceHandle deviceHandle) {
    return new ResultString(NanolibJNI.NanoLibAccessor_getDeviceHardwareVersion(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle), true);
  }

  public ResultString getDeviceFirmwareBuildId(DeviceHandle deviceHandle) {
    return new ResultString(NanolibJNI.NanoLibAccessor_getDeviceFirmwareBuildId(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle), true);
  }

  public ResultString getDeviceBootloaderBuildId(DeviceHandle deviceHandle) {
    return new ResultString(NanolibJNI.NanoLibAccessor_getDeviceBootloaderBuildId(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle), true);
  }

  public ResultString getDeviceSerialNumber(DeviceHandle deviceHandle) {
    return new ResultString(NanolibJNI.NanoLibAccessor_getDeviceSerialNumber(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle), true);
  }

  public ResultArrayByte getDeviceUid(DeviceHandle deviceHandle) {
    return new ResultArrayByte(NanolibJNI.NanoLibAccessor_getDeviceUid(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle), true);
  }

  public ResultInt getDeviceBootloaderVersion(DeviceHandle deviceHandle) {
    return new ResultInt(NanolibJNI.NanoLibAccessor_getDeviceBootloaderVersion(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle), true);
  }

  public ResultInt getDeviceHardwareGroup(DeviceHandle deviceHandle) {
    return new ResultInt(NanolibJNI.NanoLibAccessor_getDeviceHardwareGroup(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle), true);
  }

  public ResultConnectionState getConnectionState(DeviceHandle deviceHandle) {
    return new ResultConnectionState(NanolibJNI.NanoLibAccessor_getConnectionState(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle), true);
  }

  public ResultConnectionState checkConnectionState(DeviceHandle deviceHandle) {
    return new ResultConnectionState(NanolibJNI.NanoLibAccessor_checkConnectionState(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle), true);
  }

  public ResultString getDeviceState(DeviceHandle deviceHandle) {
    return new ResultString(NanolibJNI.NanoLibAccessor_getDeviceState(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle), true);
  }

  public ResultVoid setDeviceState(DeviceHandle deviceHandle, String state) {
    return new ResultVoid(NanolibJNI.NanoLibAccessor_setDeviceState(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle, state), true);
  }

  public ResultDeviceIds scanDevices(BusHardwareId busHwId, NlcScanBusCallback callback) {
    return new ResultDeviceIds(NanolibJNI.NanoLibAccessor_scanDevices(swigCPtr, this, BusHardwareId.getCPtr(busHwId), busHwId, NlcScanBusCallback.getCPtr(callback), callback), true);
  }

  public ResultVoid getProtocolSpecificAccessor(BusHardwareId busHwId) {
    return new ResultVoid(NanolibJNI.NanoLibAccessor_getProtocolSpecificAccessor(swigCPtr, this, BusHardwareId.getCPtr(busHwId), busHwId), true);
  }

  public boolean isBusHardwareOpen(BusHardwareId busHardwareId) {
    return NanolibJNI.NanoLibAccessor_isBusHardwareOpen(swigCPtr, this, BusHardwareId.getCPtr(busHardwareId), busHardwareId);
  }

  public ResultInt readNumber(DeviceHandle deviceHandle, OdIndex odIndex) {
    return new ResultInt(NanolibJNI.NanoLibAccessor_readNumber(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle, OdIndex.getCPtr(odIndex), odIndex), true);
  }

  public ResultString readString(DeviceHandle deviceHandle, OdIndex odIndex) {
    return new ResultString(NanolibJNI.NanoLibAccessor_readString(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle, OdIndex.getCPtr(odIndex), odIndex), true);
  }

  public ResultArrayByte readBytes(DeviceHandle deviceHandle, OdIndex odIndex) {
    return new ResultArrayByte(NanolibJNI.NanoLibAccessor_readBytes(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle, OdIndex.getCPtr(odIndex), odIndex), true);
  }

  public ResultVoid writeNumber(DeviceHandle deviceHandle, long value, OdIndex odIndex, long bitLength) {
    return new ResultVoid(NanolibJNI.NanoLibAccessor_writeNumber(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle, value, OdIndex.getCPtr(odIndex), odIndex, bitLength), true);
  }

  public ResultVoid writeBytes(DeviceHandle deviceHandle, ByteVector data, OdIndex odIndex) {
    return new ResultVoid(NanolibJNI.NanoLibAccessor_writeBytes(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle, ByteVector.getCPtr(data), data, OdIndex.getCPtr(odIndex), odIndex), true);
  }

  public ResultArrayInt readNumberArray(DeviceHandle deviceHandle, int index) {
    return new ResultArrayInt(NanolibJNI.NanoLibAccessor_readNumberArray(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle, index), true);
  }

  public ResultVoid uploadFirmwareFromFile(DeviceHandle deviceHandle, String absoluteFilePath, NlcDataTransferCallback callback) {
    return new ResultVoid(NanolibJNI.NanoLibAccessor_uploadFirmwareFromFile(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle, absoluteFilePath, NlcDataTransferCallback.getCPtr(callback), callback), true);
  }

  public ResultVoid uploadFirmware(DeviceHandle deviceHandle, ByteVector fwData, NlcDataTransferCallback callback) {
    return new ResultVoid(NanolibJNI.NanoLibAccessor_uploadFirmware(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle, ByteVector.getCPtr(fwData), fwData, NlcDataTransferCallback.getCPtr(callback), callback), true);
  }

  public ResultVoid uploadBootloaderFromFile(DeviceHandle deviceHandle, String bootloaderAbsoluteFilePath, NlcDataTransferCallback callback) {
    return new ResultVoid(NanolibJNI.NanoLibAccessor_uploadBootloaderFromFile(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle, bootloaderAbsoluteFilePath, NlcDataTransferCallback.getCPtr(callback), callback), true);
  }

  public ResultVoid uploadBootloader(DeviceHandle deviceHandle, ByteVector btData, NlcDataTransferCallback callback) {
    return new ResultVoid(NanolibJNI.NanoLibAccessor_uploadBootloader(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle, ByteVector.getCPtr(btData), btData, NlcDataTransferCallback.getCPtr(callback), callback), true);
  }

  public ResultVoid uploadBootloaderFirmwareFromFile(DeviceHandle deviceHandle, String bootloaderAbsoluteFilePath, String absoluteFilePath, NlcDataTransferCallback callback) {
    return new ResultVoid(NanolibJNI.NanoLibAccessor_uploadBootloaderFirmwareFromFile(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle, bootloaderAbsoluteFilePath, absoluteFilePath, NlcDataTransferCallback.getCPtr(callback), callback), true);
  }

  public ResultVoid uploadBootloaderFirmware(DeviceHandle deviceHandle, ByteVector btData, ByteVector fwData, NlcDataTransferCallback callback) {
    return new ResultVoid(NanolibJNI.NanoLibAccessor_uploadBootloaderFirmware(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle, ByteVector.getCPtr(btData), btData, ByteVector.getCPtr(fwData), fwData, NlcDataTransferCallback.getCPtr(callback), callback), true);
  }

  public ResultVoid uploadNanoJFromFile(DeviceHandle deviceHandle, String absoluteFilePath, NlcDataTransferCallback callback) {
    return new ResultVoid(NanolibJNI.NanoLibAccessor_uploadNanoJFromFile(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle, absoluteFilePath, NlcDataTransferCallback.getCPtr(callback), callback), true);
  }

  public ResultVoid uploadNanoJ(DeviceHandle deviceHandle, ByteVector vmmData, NlcDataTransferCallback callback) {
    return new ResultVoid(NanolibJNI.NanoLibAccessor_uploadNanoJ(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle, ByteVector.getCPtr(vmmData), vmmData, NlcDataTransferCallback.getCPtr(callback), callback), true);
  }

  public OdLibrary getObjectDictionaryLibrary() {
    return new OdLibrary(NanolibJNI.NanoLibAccessor_getObjectDictionaryLibrary(swigCPtr, this), false);
  }

  public ResultObjectDictionary assignObjectDictionary(DeviceHandle deviceHandle, ObjectDictionary objectDictionary) {
    return new ResultObjectDictionary(NanolibJNI.NanoLibAccessor_assignObjectDictionary(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle, ObjectDictionary.getCPtr(objectDictionary), objectDictionary), true);
  }

  public ResultObjectDictionary autoAssignObjectDictionary(DeviceHandle deviceHandle, String dictionariesLocationPath) {
    return new ResultObjectDictionary(NanolibJNI.NanoLibAccessor_autoAssignObjectDictionary(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle, dictionariesLocationPath), true);
  }

  public ResultObjectDictionary getAssignedObjectDictionary(DeviceHandle deviceHandle) {
    return new ResultObjectDictionary(NanolibJNI.NanoLibAccessor_getAssignedObjectDictionary(swigCPtr, this, DeviceHandle.getCPtr(deviceHandle), deviceHandle), true);
  }

  public ProfinetDCP getProfinetDCP() {
    return new ProfinetDCP(NanolibJNI.NanoLibAccessor_getProfinetDCP(swigCPtr, this), false);
  }

  public SamplerInterface getSamplerInterface() {
    return new SamplerInterface(NanolibJNI.NanoLibAccessor_getSamplerInterface(swigCPtr, this), false);
  }

}