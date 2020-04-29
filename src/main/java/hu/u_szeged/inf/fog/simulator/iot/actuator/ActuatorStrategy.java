package hu.u_szeged.inf.fog.simulator.iot.actuator;

import hu.u_szeged.inf.fog.simulator.iot.Station;

public interface ActuatorStrategy {
    ActuatorEvent selectEvent(Station station);
}

