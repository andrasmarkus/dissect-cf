package hu.u_szeged.inf.fog.simulator.iot.actuator;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.u_szeged.inf.fog.simulator.iot.Station;

public class StopStationEvent implements  ActuatorEvent {

    public StopStationEvent(){

    }

    @Override
    public void actuate(Station station) {
        station.stopMeter();
        System.out.println("Station " + station + " has been stopped at: " + Timed.getFireCount());
    }
}
