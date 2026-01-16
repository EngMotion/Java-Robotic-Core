package com.lucaf.robotic_core.impl;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class MasterInterface {
    protected final AtomicBoolean isInitialized = new AtomicBoolean(false);
    protected final AtomicBoolean isShutdown = new AtomicBoolean(false);
    protected final AtomicBoolean hasError = new AtomicBoolean(false);

    public Future<Boolean> initialize() {
        isInitialized.set(true);
        return null;
    }

    public void shutdown() throws IOException {
        isShutdown.set(true);
    }

    public boolean isShutdown() {
        return isShutdown.get();
    }

    public boolean isInitialized() {
        return isInitialized.get();
    }

    public boolean hasError() {
        return hasError.get();
    }

}
