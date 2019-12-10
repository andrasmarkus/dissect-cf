package hu.u_szeged.inf.fog.simulator.iot.actuator.eventemitter;

import hu.u_szeged.inf.fog.simulator.iot.actuator.ActuatorEvent;
import hu.u_szeged.inf.fog.simulator.iot.actuator.eventemitter.annotation.Subscribe;

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventEmitter extends Event {

    void subscribe(String channelName, Object subscriber) {
        if (!channels.containsKey(channelName)) {
            channels.put(channelName, new ConcurrentHashMap<Integer, WeakReference<Object>>());
        }

        channels.get(channelName).put(subscriber.hashCode(), new WeakReference<Object>(subscriber));
    }

    void publish(String channelName, ActuatorEvent event) {
        for (Map.Entry<Integer, WeakReference<Object>> subs : channels.get(channelName).entrySet()) {
            WeakReference<Object> subscriberRef = subs.getValue();

            Object subscriberObj = subscriberRef.get();
            if (subscriberObj != null) {
                for (final Method method : subscriberObj.getClass().getDeclaredMethods()) {
                    Annotation annotation = method.getAnnotation(Subscribe.class);
                    if (annotation != null) {
                        deliverMessage(subscriberObj, method, event);
                    }
                }
            }
        }
    }

    <T, P> boolean deliverMessage(T subscriber, Method method, ActuatorEvent event) {
        try {
            boolean methodFound = false;
            for (final Class paramClass : method.getParameterTypes()) {
                if (paramClass.equals(event.getClass())) {
                    methodFound = true;
                    break;
                }
            }
            if (methodFound) {
                method.setAccessible(true);
                method.invoke(subscriber, event);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
