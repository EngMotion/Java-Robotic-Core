/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.nanotec.nanolib;

public class BusHardwareOptions {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected BusHardwareOptions(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(BusHardwareOptions obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(BusHardwareOptions obj) {
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
        NanolibJNI.delete_BusHardwareOptions(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public BusHardwareOptions() {
    this(NanolibJNI.new_BusHardwareOptions__SWIG_0(), true);
  }

  public BusHardwareOptions(StringStringMap options) {
    this(NanolibJNI.new_BusHardwareOptions__SWIG_1(StringStringMap.getCPtr(options), options), true);
  }

  public void addOption(String key, String value) {
    NanolibJNI.BusHardwareOptions_addOption(swigCPtr, this, key, value);
  }

  public StringStringMap getOptions() {
    return new StringStringMap(NanolibJNI.BusHardwareOptions_getOptions(swigCPtr, this), true);
  }

  public boolean equals(BusHardwareOptions other) {
    return NanolibJNI.BusHardwareOptions_equals(swigCPtr, this, BusHardwareOptions.getCPtr(other), other);
  }

  public String toString() {
    return NanolibJNI.BusHardwareOptions_toString(swigCPtr, this);
  }

}
