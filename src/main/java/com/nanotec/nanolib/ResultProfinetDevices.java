/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.nanotec.nanolib;

public class ResultProfinetDevices extends Result {
  private transient long swigCPtr;

  protected ResultProfinetDevices(long cPtr, boolean cMemoryOwn) {
    super(NanolibJNI.ResultProfinetDevices_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ResultProfinetDevices obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(ResultProfinetDevices obj) {
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
        NanolibJNI.delete_ResultProfinetDevices(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public ResultProfinetDevices(ProfinetDeviceVector profinetDevices) {
    this(NanolibJNI.new_ResultProfinetDevices__SWIG_0(ProfinetDeviceVector.getCPtr(profinetDevices), profinetDevices), true);
  }

  public ResultProfinetDevices(Result result) {
    this(NanolibJNI.new_ResultProfinetDevices__SWIG_1(Result.getCPtr(result), result), true);
  }

  public ResultProfinetDevices(String errorText, NlcErrorCode errorCode, long extendedErrorCode) {
    this(NanolibJNI.new_ResultProfinetDevices__SWIG_2(errorText, errorCode.swigValue(), extendedErrorCode), true);
  }

  public ResultProfinetDevices(String errorText, NlcErrorCode errorCode) {
    this(NanolibJNI.new_ResultProfinetDevices__SWIG_3(errorText, errorCode.swigValue()), true);
  }

  public ResultProfinetDevices(String errorText) {
    this(NanolibJNI.new_ResultProfinetDevices__SWIG_4(errorText), true);
  }

  public ProfinetDeviceVector getResult() {
    return new ProfinetDeviceVector(NanolibJNI.ResultProfinetDevices_getResult(swigCPtr, this), false);
  }

}