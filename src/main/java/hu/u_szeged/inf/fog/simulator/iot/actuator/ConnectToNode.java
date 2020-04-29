package hu.u_szeged.inf.fog.simulator.iot.actuator;

import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.iot.Station;

public class ConnectToNode implements ActuatorEvent {

    Application application;

    public ConnectToNode(Application application) {
        this.application = application;
    }

    @Override
    public void actuate(Station station) {
        station.setApp(application);
        application.deviceList.add(station);
        System.out.println("Station " + station + " has connected to a new node");

    }
}
