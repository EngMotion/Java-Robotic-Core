/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.nanotec.nanolib;

public class NlcScanBusCallback {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected NlcScanBusCallback(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(NlcScanBusCallback obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(NlcScanBusCallback obj) {
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
        NanolibJNI.delete_NlcScanBusCallback(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected void swigDirectorDisconnect() {
    swigCMemOwn = false;
    delete();
  }

  public void swigReleaseOwnership() {
    swigCMemOwn = false;
    NanolibJNI.NlcScanBusCallback_change_ownership(this, swigCPtr, false);
  }

  public void swigTakeOwnership() {
    swigCMemOwn = true;
    NanolibJNI.NlcScanBusCallback_change_ownership(this, swigCPtr, true);
  }

  public ResultVoid callback(BusScanInfo info, DeviceIdVector devicesFound, int data) {
    return new ResultVoid(NanolibJNI.NlcScanBusCallback_callback(swigCPtr, this, info.swigValue(), DeviceIdVector.getCPtr(devicesFound), devicesFound, data), true);
  }

  public NlcScanBusCallback() {
    this(NanolibJNI.new_NlcScanBusCallback(), true);
    NanolibJNI.NlcScanBusCallback_director_connect(this, swigCPtr, true, true);
  }

}
