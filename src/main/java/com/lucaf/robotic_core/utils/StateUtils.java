package com.lucaf.robotic_core.utils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class StateUtils {
    public static int getInt(Object state) {
        if (state instanceof AtomicInteger) {
            return ((AtomicInteger) state).get();
        } else if (state instanceof Integer) {
            return (Integer) state;
        } else if (state instanceof Double) {
            return ((Double) state).intValue();
        } else {
            throw new IllegalArgumentException("Unsupported state type: " + state.getClass().getName());
        }
    }

    public static boolean getBoolean(Object state) {
        if (state instanceof AtomicBoolean){
            return ((AtomicBoolean) state).get();
        }else if (state instanceof Boolean) {
            return (Boolean) state;
        } else {
            throw new IllegalArgumentException("Unsupported state type: " + state.getClass().getName());
        }
    }
}
