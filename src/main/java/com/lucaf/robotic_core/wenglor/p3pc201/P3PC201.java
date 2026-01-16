package com.lucaf.robotic_core.wenglor.p3pc201;

import com.lucaf.robotic_core.impl.SensorInterface;
import com.lucaf.robotic_core.wenglor.impl.IndexInterface;

public class P3PC201 extends SensorInterface {
    final IndexInterface master;

    public P3PC201(IndexInterface master) {
        this.master = master;
    }
}
