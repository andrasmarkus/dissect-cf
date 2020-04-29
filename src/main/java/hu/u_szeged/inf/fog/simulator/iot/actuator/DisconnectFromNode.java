package hu.u_szeged.inf.fog.simulator.iot.actuator;

import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.iot.Station;

public class DisconnectFromNode implements ActuatorEvent {

    public DisconnectFromNode(){}

    @Override
    public void actuate(Station station) {
        Application application = station.getApp();
        application.deviceList.remove(station);
        station.setApp(null);
        System.out.println(station + " disconnected from current application");
    }
}
