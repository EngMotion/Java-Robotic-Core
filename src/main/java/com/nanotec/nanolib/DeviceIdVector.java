/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.nanotec.nanolib;

public class DeviceIdVector extends java.util.AbstractList<DeviceId> implements java.util.RandomAccess {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected DeviceIdVector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(DeviceIdVector obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(DeviceIdVector obj) {
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
        NanolibJNI.delete_DeviceIdVector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public DeviceIdVector(DeviceId[] initialElements) {
    this();
    reserve(initialElements.length);

    for (DeviceId element : initialElements) {
      add(element);
    }
  }

  public DeviceIdVector(Iterable<DeviceId> initialElements) {
    this();
    for (DeviceId element : initialElements) {
      add(element);
    }
  }

  public DeviceId get(int index) {
    return doGet(index);
  }

  public DeviceId set(int index, DeviceId e) {
    return doSet(index, e);
  }

  public boolean add(DeviceId e) {
    modCount++;
    doAdd(e);
    return true;
  }

  public void add(int index, DeviceId e) {
    modCount++;
    doAdd(index, e);
  }

  public DeviceId remove(int index) {
    modCount++;
    return doRemove(index);
  }

  protected void removeRange(int fromIndex, int toIndex) {
    modCount++;
    doRemoveRange(fromIndex, toIndex);
  }

  public int size() {
    return doSize();
  }

  public DeviceIdVector() {
    this(NanolibJNI.new_DeviceIdVector__SWIG_0(), true);
  }

  public DeviceIdVector(DeviceIdVector other) {
    this(NanolibJNI.new_DeviceIdVector__SWIG_1(DeviceIdVector.getCPtr(other), other), true);
  }

  public long capacity() {
    return NanolibJNI.DeviceIdVector_capacity(swigCPtr, this);
  }

  public void reserve(long n) {
    NanolibJNI.DeviceIdVector_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() {
    return NanolibJNI.DeviceIdVector_isEmpty(swigCPtr, this);
  }

  public void clear() {
    NanolibJNI.DeviceIdVector_clear(swigCPtr, this);
  }

  public DeviceIdVector(int count, DeviceId value) {
    this(NanolibJNI.new_DeviceIdVector__SWIG_2(count, DeviceId.getCPtr(value), value), true);
  }

  private int doSize() {
    return NanolibJNI.DeviceIdVector_doSize(swigCPtr, this);
  }

  private void doAdd(DeviceId x) {
    NanolibJNI.DeviceIdVector_doAdd__SWIG_0(swigCPtr, this, DeviceId.getCPtr(x), x);
  }

  private void doAdd(int index, DeviceId x) {
    NanolibJNI.DeviceIdVector_doAdd__SWIG_1(swigCPtr, this, index, DeviceId.getCPtr(x), x);
  }

  private DeviceId doRemove(int index) {
    return new DeviceId(NanolibJNI.DeviceIdVector_doRemove(swigCPtr, this, index), true);
  }

  private DeviceId doGet(int index) {
    return new DeviceId(NanolibJNI.DeviceIdVector_doGet(swigCPtr, this, index), false);
  }

  private DeviceId doSet(int index, DeviceId val) {
    return new DeviceId(NanolibJNI.DeviceIdVector_doSet(swigCPtr, this, index, DeviceId.getCPtr(val), val), true);
  }

  private void doRemoveRange(int fromIndex, int toIndex) {
    NanolibJNI.DeviceIdVector_doRemoveRange(swigCPtr, this, fromIndex, toIndex);
  }

}