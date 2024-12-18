/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.nanotec.nanolib;

public class CanOpenNmtState {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CanOpenNmtState(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CanOpenNmtState obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(CanOpenNmtState obj) {
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
        NanolibJNI.delete_CanOpenNmtState(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public String getSTOPPED() {
    return NanolibJNI.CanOpenNmtState_STOPPED_get(swigCPtr, this);
  }

  public String getPRE_OPERATIONAL() {
    return NanolibJNI.CanOpenNmtState_PRE_OPERATIONAL_get(swigCPtr, this);
  }

  public String getOPERATIONAL() {
    return NanolibJNI.CanOpenNmtState_OPERATIONAL_get(swigCPtr, this);
  }

  public String getINITIALIZATION() {
    return NanolibJNI.CanOpenNmtState_INITIALIZATION_get(swigCPtr, this);
  }

  public String getUNKNOWN() {
    return NanolibJNI.CanOpenNmtState_UNKNOWN_get(swigCPtr, this);
  }

  public CanOpenNmtState() {
    this(NanolibJNI.new_CanOpenNmtState(), true);
  }

}
