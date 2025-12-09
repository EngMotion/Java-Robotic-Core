package com.lucaf.robotic_core.dataInterfaces.impl;


import com.lucaf.robotic_core.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class SerialInterface extends IOInterface {
    List<Consumer<SerialEvent>> dataListeners = new ArrayList<>();

    public SerialInterface(String name, Logger logger) {
        super(name, logger);
    }

    public SerialInterface(String name) {
        super(name);
    }

    public abstract byte[] sendForResult(byte[] request) throws IOException;

    public void addDataListener(Consumer<SerialEvent> listener) {
        dataListeners.add(listener);
    }

    public void removeDataListener(Consumer<SerialEvent> listener) {
        dataListeners.remove(listener);
    }

    protected void emitDataEvent(SerialEvent event) {
        for (Consumer<SerialEvent> listener : dataListeners) {
            listener.accept(event);
        }
    }

    public abstract void purge() throws IOException;

    public void shutdown() {
        dataListeners.clear();
    }
}
