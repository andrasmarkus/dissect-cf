package hu.u_szeged.inf.fog.simulator.iot.actuator;

import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.iot.Station;

public class ChangeNode implements ActuatorEvent {

    Application from, to;

    public ChangeNode(Application from, Application to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public void actuate(Station station) {
        from.deviceList.remove(station);
        station.setApp(to);
        to.deviceList.add(station);
        System.out.println("Application of station " + station + " has been changed");
    }
}
