package hu.u_szeged.inf.fog.simulator.iot.actuator;

import hu.u_szeged.inf.fog.simulator.iot.Station;

public class RestartStationEvent implements ActuatorEvent {

    public RestartStationEvent() {

    }

    @Override
    public void actuate(Station station) {
        //TODO: this still needs to be fixed
        station.restart();
    }

}
