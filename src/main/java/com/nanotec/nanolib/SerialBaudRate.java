/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.nanotec.nanolib;

public class SerialBaudRate {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected SerialBaudRate(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(SerialBaudRate obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(SerialBaudRate obj) {
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
        NanolibJNI.delete_SerialBaudRate(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public String getBAUD_RATE_7200() {
    return NanolibJNI.SerialBaudRate_BAUD_RATE_7200_get(swigCPtr, this);
  }

  public String getBAUD_RATE_9600() {
    return NanolibJNI.SerialBaudRate_BAUD_RATE_9600_get(swigCPtr, this);
  }

  public String getBAUD_RATE_14400() {
    return NanolibJNI.SerialBaudRate_BAUD_RATE_14400_get(swigCPtr, this);
  }

  public String getBAUD_RATE_19200() {
    return NanolibJNI.SerialBaudRate_BAUD_RATE_19200_get(swigCPtr, this);
  }

  public String getBAUD_RATE_38400() {
    return NanolibJNI.SerialBaudRate_BAUD_RATE_38400_get(swigCPtr, this);
  }

  public String getBAUD_RATE_56000() {
    return NanolibJNI.SerialBaudRate_BAUD_RATE_56000_get(swigCPtr, this);
  }

  public String getBAUD_RATE_57600() {
    return NanolibJNI.SerialBaudRate_BAUD_RATE_57600_get(swigCPtr, this);
  }

  public String getBAUD_RATE_115200() {
    return NanolibJNI.SerialBaudRate_BAUD_RATE_115200_get(swigCPtr, this);
  }

  public String getBAUD_RATE_128000() {
    return NanolibJNI.SerialBaudRate_BAUD_RATE_128000_get(swigCPtr, this);
  }

  public String getBAUD_RATE_256000() {
    return NanolibJNI.SerialBaudRate_BAUD_RATE_256000_get(swigCPtr, this);
  }

  public SerialBaudRate() {
    this(NanolibJNI.new_SerialBaudRate(), true);
  }

}