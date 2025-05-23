/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.nanotec.nanolib;

public class DeviceId {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected DeviceId(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(DeviceId obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(DeviceId obj) {
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
        NanolibJNI.delete_DeviceId(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public DeviceId(BusHardwareId busHardwareId_, long deviceId_, String description_) {
    this(NanolibJNI.new_DeviceId__SWIG_0(BusHardwareId.getCPtr(busHardwareId_), busHardwareId_, deviceId_, description_), true);
  }

  public DeviceId(BusHardwareId busHardwareId_, long deviceId_, String description_, ByteVector extraId_, String extraStringId_) {
    this(NanolibJNI.new_DeviceId__SWIG_1(BusHardwareId.getCPtr(busHardwareId_), busHardwareId_, deviceId_, description_, ByteVector.getCPtr(extraId_), extraId_, extraStringId_), true);
  }

  public DeviceId(DeviceId arg0) {
    this(NanolibJNI.new_DeviceId__SWIG_2(DeviceId.getCPtr(arg0), arg0), true);
  }

  public DeviceId() {
    this(NanolibJNI.new_DeviceId__SWIG_3(), true);
  }

  public BusHardwareId getBusHardwareId() {
    return new BusHardwareId(NanolibJNI.DeviceId_getBusHardwareId(swigCPtr, this), true);
  }

  public long getDeviceId() {
    return NanolibJNI.DeviceId_getDeviceId(swigCPtr, this);
  }

  public String getDescription() {
    return NanolibJNI.DeviceId_getDescription(swigCPtr, this);
  }

  public ByteVector getExtraId() {
    return new ByteVector(NanolibJNI.DeviceId_getExtraId(swigCPtr, this), false);
  }

  public String getExtraStringId() {
    return NanolibJNI.DeviceId_getExtraStringId(swigCPtr, this);
  }

  public String toString() {
    return NanolibJNI.DeviceId_toString(swigCPtr, this);
  }

  public boolean equals(DeviceId other) {
    return NanolibJNI.DeviceId_equals(swigCPtr, this, DeviceId.getCPtr(other), other);
  }

}
