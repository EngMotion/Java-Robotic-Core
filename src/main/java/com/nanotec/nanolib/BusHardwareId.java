/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.nanotec.nanolib;

public class BusHardwareId {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected BusHardwareId(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(BusHardwareId obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(BusHardwareId obj) {
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
        NanolibJNI.delete_BusHardwareId(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public BusHardwareId(String busHardware_, String protocol_, String hardwareSpecifier_, String name_) {
    this(NanolibJNI.new_BusHardwareId__SWIG_0(busHardware_, protocol_, hardwareSpecifier_, name_), true);
  }

  public BusHardwareId(String busHardware_, String protocol_, String hardwareSpecifier_, String extraHardwareSpecifier_, String name_) {
    this(NanolibJNI.new_BusHardwareId__SWIG_1(busHardware_, protocol_, hardwareSpecifier_, extraHardwareSpecifier_, name_), true);
  }

  public BusHardwareId(BusHardwareId arg0) {
    this(NanolibJNI.new_BusHardwareId__SWIG_2(BusHardwareId.getCPtr(arg0), arg0), true);
  }

  public BusHardwareId() {
    this(NanolibJNI.new_BusHardwareId__SWIG_3(), true);
  }

  public String getBusHardware() {
    return NanolibJNI.BusHardwareId_getBusHardware(swigCPtr, this);
  }

  public String getProtocol() {
    return NanolibJNI.BusHardwareId_getProtocol(swigCPtr, this);
  }

  public String getHardwareSpecifier() {
    return NanolibJNI.BusHardwareId_getHardwareSpecifier(swigCPtr, this);
  }

  public String getExtraHardwareSpecifier() {
    return NanolibJNI.BusHardwareId_getExtraHardwareSpecifier(swigCPtr, this);
  }

  public String getName() {
    return NanolibJNI.BusHardwareId_getName(swigCPtr, this);
  }

  public boolean equals(BusHardwareId other) {
    return NanolibJNI.BusHardwareId_equals(swigCPtr, this, BusHardwareId.getCPtr(other), other);
  }

  public String toString() {
    return NanolibJNI.BusHardwareId_toString(swigCPtr, this);
  }

}