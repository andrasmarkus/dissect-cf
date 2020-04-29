package hu.u_szeged.inf.fog.simulator.iot.actuator;

import hu.u_szeged.inf.fog.simulator.iot.Station;
import hu.u_szeged.inf.fog.simulator.iot.mobility.GeoLocation;

public interface ActuatorEvent {

    void actuate(Station station);

}

