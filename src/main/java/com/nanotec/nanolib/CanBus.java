/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.nanotec.nanolib;

public class CanBus {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CanBus(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CanBus obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(CanBus obj) {
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
        NanolibJNI.delete_CanBus(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public String getBAUD_RATE_OPTIONS_NAME() {
    return NanolibJNI.CanBus_BAUD_RATE_OPTIONS_NAME_get(swigCPtr, this);
  }

  public CanBaudRate getBaudRate() {
    long cPtr = NanolibJNI.CanBus_baudRate_get(swigCPtr, this);
    return (cPtr == 0) ? null : new CanBaudRate(cPtr, false);
  }

  public Ixxat getIxxat() {
    long cPtr = NanolibJNI.CanBus_ixxat_get(swigCPtr, this);
    return (cPtr == 0) ? null : new Ixxat(cPtr, false);
  }

  public CanBus() {
    this(NanolibJNI.new_CanBus(), true);
  }

}