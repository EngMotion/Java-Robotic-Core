/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.nanotec.nanolib;

public class IntVector extends java.util.AbstractList<Long> implements java.util.RandomAccess {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected IntVector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(IntVector obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(IntVector obj) {
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
        NanolibJNI.delete_IntVector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public IntVector(long[] initialElements) {
    this();
    reserve(initialElements.length);

    for (long element : initialElements) {
      add(element);
    }
  }

  public IntVector(Iterable<Long> initialElements) {
    this();
    for (long element : initialElements) {
      add(element);
    }
  }

  public Long get(int index) {
    return doGet(index);
  }

  public Long set(int index, Long e) {
    return doSet(index, e);
  }

  public boolean add(Long e) {
    modCount++;
    doAdd(e);
    return true;
  }

  public void add(int index, Long e) {
    modCount++;
    doAdd(index, e);
  }

  public Long remove(int index) {
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

  public IntVector() {
    this(NanolibJNI.new_IntVector__SWIG_0(), true);
  }

  public IntVector(IntVector other) {
    this(NanolibJNI.new_IntVector__SWIG_1(IntVector.getCPtr(other), other), true);
  }

  public long capacity() {
    return NanolibJNI.IntVector_capacity(swigCPtr, this);
  }

  public void reserve(long n) {
    NanolibJNI.IntVector_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() {
    return NanolibJNI.IntVector_isEmpty(swigCPtr, this);
  }

  public void clear() {
    NanolibJNI.IntVector_clear(swigCPtr, this);
  }

  public IntVector(int count, long value) {
    this(NanolibJNI.new_IntVector__SWIG_2(count, value), true);
  }

  private int doSize() {
    return NanolibJNI.IntVector_doSize(swigCPtr, this);
  }

  private void doAdd(long x) {
    NanolibJNI.IntVector_doAdd__SWIG_0(swigCPtr, this, x);
  }

  private void doAdd(int index, long x) {
    NanolibJNI.IntVector_doAdd__SWIG_1(swigCPtr, this, index, x);
  }

  private long doRemove(int index) {
    return NanolibJNI.IntVector_doRemove(swigCPtr, this, index);
  }

  private long doGet(int index) {
    return NanolibJNI.IntVector_doGet(swigCPtr, this, index);
  }

  private long doSet(int index, long val) {
    return NanolibJNI.IntVector_doSet(swigCPtr, this, index, val);
  }

  private void doRemoveRange(int fromIndex, int toIndex) {
    NanolibJNI.IntVector_doRemoveRange(swigCPtr, this, fromIndex, toIndex);
  }

}