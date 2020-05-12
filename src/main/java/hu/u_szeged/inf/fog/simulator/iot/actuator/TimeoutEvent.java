package hu.u_szeged.inf.fog.simulator.iot.actuator;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.u_szeged.inf.fog.simulator.iot.Station;

public class TimeoutEvent implements ActuatorEvent {
    public static int counter = 0;
    public TimeoutEvent(){}

    @Override
    public void actuate(Station station) {
        station.stopMeter();
        System.out.println("Station " + station + " timed out at: " + Timed.getFireCount());
        counter++;
    }
}
