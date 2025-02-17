/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.nanotec.nanolib;

public class ResultArrayByte extends Result {
  private transient long swigCPtr;

  protected ResultArrayByte(long cPtr, boolean cMemoryOwn) {
    super(NanolibJNI.ResultArrayByte_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ResultArrayByte obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(ResultArrayByte obj) {
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
        NanolibJNI.delete_ResultArrayByte(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public ResultArrayByte(ByteVector result_) {
    this(NanolibJNI.new_ResultArrayByte__SWIG_0(ByteVector.getCPtr(result_), result_), true);
  }

  public ResultArrayByte(String errorString_) {
    this(NanolibJNI.new_ResultArrayByte__SWIG_1(errorString_), true);
  }

  public ResultArrayByte(NlcErrorCode errCode, String errorString_) {
    this(NanolibJNI.new_ResultArrayByte__SWIG_2(errCode.swigValue(), errorString_), true);
  }

  public ResultArrayByte(NlcErrorCode errCode, long exErrCode, String errorString_) {
    this(NanolibJNI.new_ResultArrayByte__SWIG_3(errCode.swigValue(), exErrCode, errorString_), true);
  }

  public ResultArrayByte(Result result) {
    this(NanolibJNI.new_ResultArrayByte__SWIG_4(Result.getCPtr(result), result), true);
  }

  public ByteVector getResult() {
    return new ByteVector(NanolibJNI.ResultArrayByte_getResult(swigCPtr, this), true);
  }

}
