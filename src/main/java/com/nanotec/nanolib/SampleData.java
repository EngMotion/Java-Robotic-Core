/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.nanotec.nanolib;

public class SampleData {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected SampleData(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(SampleData obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(SampleData obj) {
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
        NanolibJNI.delete_SampleData(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setIterationNumber(java.math.BigInteger value) {
    NanolibJNI.SampleData_iterationNumber_set(swigCPtr, this, value);
  }

  public java.math.BigInteger getIterationNumber() {
    return NanolibJNI.SampleData_iterationNumber_get(swigCPtr, this);
  }

  public void setSampledValues(SampledValueVector value) {
    NanolibJNI.SampleData_sampledValues_set(swigCPtr, this, SampledValueVector.getCPtr(value), value);
  }

  public SampledValueVector getSampledValues() {
    long cPtr = NanolibJNI.SampleData_sampledValues_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SampledValueVector(cPtr, false);
  }

  public SampleData() {
    this(NanolibJNI.new_SampleData(), true);
  }

}