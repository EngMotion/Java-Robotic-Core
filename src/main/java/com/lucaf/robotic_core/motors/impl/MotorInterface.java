package com.lucaf.robotic_core.motors.impl;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class MotorInterface {
    protected final AtomicBoolean isInitialized = new AtomicBoolean(false);
    protected final AtomicBoolean isShutdown = new AtomicBoolean(false);
    protected final AtomicBoolean isStopped = new AtomicBoolean(false);
    protected final AtomicBoolean isMoving = new AtomicBoolean(false);
    protected final AtomicBoolean hasError = new AtomicBoolean(false);

    public void stop() throws IOException {
        isStopped.set(true);
    }

    public Future<Boolean> initialize() {
        isInitialized.set(true);
        return null;
    }

    public Future<Boolean> start() throws RejectedExecutionException {
        isStopped.set(false);
        return null;
    }

    public void shutdown() throws IOException {
        stop();
        isShutdown.set(true);
    }

    public boolean isShutdown() {
        return isShutdown.get();
    }

    public boolean isStopped() {
        return isStopped.get();
    }

    public boolean isInitialized() {
        return isInitialized.get();
    }

    public boolean isMoving() {
        return isMoving.get();
    }

    public boolean setMoving(boolean moving) {
        return isMoving.getAndSet(moving);
    }

    public boolean hasError() {
        return hasError.get();
    }

    public boolean canMove() {
        return isInitialized.get() && !isShutdown.get() && !isStopped.get() && !hasError.get();
    }
}
