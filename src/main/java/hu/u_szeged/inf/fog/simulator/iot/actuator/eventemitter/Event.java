package hu.u_szeged.inf.fog.simulator.iot.actuator.eventemitter;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

public class Event {

    static EventEmitter eventEmitter;
    static ConcurrentHashMap<String, ConcurrentHashMap<Integer, WeakReference<Object>>> channels;


    static {
        init();
    }

    static void init() {
        channels = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, WeakReference<Object>>>();
        eventEmitter = new EventEmitter();
    }
}
