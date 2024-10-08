/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.nanotec.nanolib;

public class IxxatAdapterBusNumber {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected IxxatAdapterBusNumber(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(IxxatAdapterBusNumber obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(IxxatAdapterBusNumber obj) {
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
        NanolibJNI.delete_IxxatAdapterBusNumber(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public String getBUS_NUMBER_0_DEFAULT() {
    return NanolibJNI.IxxatAdapterBusNumber_BUS_NUMBER_0_DEFAULT_get(swigCPtr, this);
  }

  public String getBUS_NUMBER_1() {
    return NanolibJNI.IxxatAdapterBusNumber_BUS_NUMBER_1_get(swigCPtr, this);
  }

  public String getBUS_NUMBER_2() {
    return NanolibJNI.IxxatAdapterBusNumber_BUS_NUMBER_2_get(swigCPtr, this);
  }

  public String getBUS_NUMBER_3() {
    return NanolibJNI.IxxatAdapterBusNumber_BUS_NUMBER_3_get(swigCPtr, this);
  }

  public IxxatAdapterBusNumber() {
    this(NanolibJNI.new_IxxatAdapterBusNumber(), true);
  }

}
