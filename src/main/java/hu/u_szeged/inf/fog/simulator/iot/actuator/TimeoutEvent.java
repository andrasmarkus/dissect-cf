package hu.u_szeged.inf.fog.simulator.iot.actuator;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.u_szeged.inf.fog.simulator.iot.Station;

public class TimeoutEvent implements ActuatorEvent {
    public static long counter = 0;
    public static long unprocessed = 0;
    public TimeoutEvent(){}

    @Override
    public void actuate(Station station) {
        unprocessed += station.getFilesize();
        counter++;
    }
}
