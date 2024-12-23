/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.nanotec.nanolib;

public class ResultSampleDataArray extends Result {
  private transient long swigCPtr;

  protected ResultSampleDataArray(long cPtr, boolean cMemoryOwn) {
    super(NanolibJNI.ResultSampleDataArray_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ResultSampleDataArray obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(ResultSampleDataArray obj) {
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
        NanolibJNI.delete_ResultSampleDataArray(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public ResultSampleDataArray() {
    this(NanolibJNI.new_ResultSampleDataArray__SWIG_0(), true);
  }

  public ResultSampleDataArray(SampleDataVector dataArray) {
    this(NanolibJNI.new_ResultSampleDataArray__SWIG_1(SampleDataVector.getCPtr(dataArray), dataArray), true);
  }

  public ResultSampleDataArray(String errorDesc, NlcErrorCode errorCode, long extendedErrorCode) {
    this(NanolibJNI.new_ResultSampleDataArray__SWIG_2(errorDesc, errorCode.swigValue(), extendedErrorCode), true);
  }

  public ResultSampleDataArray(String errorDesc, NlcErrorCode errorCode) {
    this(NanolibJNI.new_ResultSampleDataArray__SWIG_3(errorDesc, errorCode.swigValue()), true);
  }

  public ResultSampleDataArray(String errorDesc) {
    this(NanolibJNI.new_ResultSampleDataArray__SWIG_4(errorDesc), true);
  }

  public ResultSampleDataArray(ResultSampleDataArray other) {
    this(NanolibJNI.new_ResultSampleDataArray__SWIG_5(ResultSampleDataArray.getCPtr(other), other), true);
  }

  public ResultSampleDataArray(Result result) {
    this(NanolibJNI.new_ResultSampleDataArray__SWIG_6(Result.getCPtr(result), result), true);
  }

  public SampleDataVector getResult() {
    return new SampleDataVector(NanolibJNI.ResultSampleDataArray_getResult(swigCPtr, this), false);
  }

}
