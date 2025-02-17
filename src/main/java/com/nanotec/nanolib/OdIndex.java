/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.nanotec.nanolib;

public class OdIndex {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected OdIndex(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(OdIndex obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(OdIndex obj) {
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
        NanolibJNI.delete_OdIndex(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public OdIndex(int index, short subIndex) {
    this(NanolibJNI.new_OdIndex__SWIG_0(index, subIndex), true);
  }

  public OdIndex() {
    this(NanolibJNI.new_OdIndex__SWIG_1(), true);
  }

  public int getIndex() {
    return NanolibJNI.OdIndex_getIndex(swigCPtr, this);
  }

  public short getSubIndex() {
    return NanolibJNI.OdIndex_getSubIndex(swigCPtr, this);
  }

  public String toString() {
    return NanolibJNI.OdIndex_toString(swigCPtr, this);
  }

}
