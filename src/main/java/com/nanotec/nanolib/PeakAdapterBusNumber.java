/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.nanotec.nanolib;

public class PeakAdapterBusNumber {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected PeakAdapterBusNumber(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(PeakAdapterBusNumber obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(PeakAdapterBusNumber obj) {
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
        NanolibJNI.delete_PeakAdapterBusNumber(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public String getBUS_NUMBER_1_DEFAULT() {
    return NanolibJNI.PeakAdapterBusNumber_BUS_NUMBER_1_DEFAULT_get(swigCPtr, this);
  }

  public String getBUS_NUMBER_2() {
    return NanolibJNI.PeakAdapterBusNumber_BUS_NUMBER_2_get(swigCPtr, this);
  }

  public String getBUS_NUMBER_3() {
    return NanolibJNI.PeakAdapterBusNumber_BUS_NUMBER_3_get(swigCPtr, this);
  }

  public String getBUS_NUMBER_4() {
    return NanolibJNI.PeakAdapterBusNumber_BUS_NUMBER_4_get(swigCPtr, this);
  }

  public String getBUS_NUMBER_5() {
    return NanolibJNI.PeakAdapterBusNumber_BUS_NUMBER_5_get(swigCPtr, this);
  }

  public String getBUS_NUMBER_6() {
    return NanolibJNI.PeakAdapterBusNumber_BUS_NUMBER_6_get(swigCPtr, this);
  }

  public String getBUS_NUMBER_7() {
    return NanolibJNI.PeakAdapterBusNumber_BUS_NUMBER_7_get(swigCPtr, this);
  }

  public String getBUS_NUMBER_8() {
    return NanolibJNI.PeakAdapterBusNumber_BUS_NUMBER_8_get(swigCPtr, this);
  }

  public String getBUS_NUMBER_9() {
    return NanolibJNI.PeakAdapterBusNumber_BUS_NUMBER_9_get(swigCPtr, this);
  }

  public String getBUS_NUMBER_10() {
    return NanolibJNI.PeakAdapterBusNumber_BUS_NUMBER_10_get(swigCPtr, this);
  }

  public String getBUS_NUMBER_11() {
    return NanolibJNI.PeakAdapterBusNumber_BUS_NUMBER_11_get(swigCPtr, this);
  }

  public String getBUS_NUMBER_12() {
    return NanolibJNI.PeakAdapterBusNumber_BUS_NUMBER_12_get(swigCPtr, this);
  }

  public String getBUS_NUMBER_13() {
    return NanolibJNI.PeakAdapterBusNumber_BUS_NUMBER_13_get(swigCPtr, this);
  }

  public String getBUS_NUMBER_14() {
    return NanolibJNI.PeakAdapterBusNumber_BUS_NUMBER_14_get(swigCPtr, this);
  }

  public String getBUS_NUMBER_15() {
    return NanolibJNI.PeakAdapterBusNumber_BUS_NUMBER_15_get(swigCPtr, this);
  }

  public String getBUS_NUMBER_16() {
    return NanolibJNI.PeakAdapterBusNumber_BUS_NUMBER_16_get(swigCPtr, this);
  }

  public PeakAdapterBusNumber() {
    this(NanolibJNI.new_PeakAdapterBusNumber(), true);
  }

}
