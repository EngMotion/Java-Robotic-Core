/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.nanotec.nanolib;

public class MacAddress {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected MacAddress(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(MacAddress obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(MacAddress obj) {
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
        NanolibJNI.delete_MacAddress(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public MacAddress() {
    this(NanolibJNI.new_MacAddress__SWIG_0(), true);
  }

  public MacAddress(MacAddress other) {
    this(NanolibJNI.new_MacAddress__SWIG_1(MacAddress.getCPtr(other), other), true);
  }

  public long size() {
    return NanolibJNI.MacAddress_size(swigCPtr, this);
  }

  public boolean isEmpty() {
    return NanolibJNI.MacAddress_isEmpty(swigCPtr, this);
  }

  public void fill(short u) {
    NanolibJNI.MacAddress_fill(swigCPtr, this, u);
  }

  public short get(int i) {
    return NanolibJNI.MacAddress_get(swigCPtr, this, i);
  }

  public void set(int i, short val) {
    NanolibJNI.MacAddress_set(swigCPtr, this, i, val);
  }

}