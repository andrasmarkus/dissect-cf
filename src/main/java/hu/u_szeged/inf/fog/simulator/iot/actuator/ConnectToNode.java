package hu.u_szeged.inf.fog.simulator.iot.actuator;

import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.iot.Device;
import hu.u_szeged.inf.fog.simulator.iot.Station;

public class ConnectToNode implements ActuatorEvent {

    Application application;

    public ConnectToNode(Application application) {
        this.application = application;
    }

    @Override
    public void actuate(Station d) {
        d.setApp(application);
        application.deviceList.add(d);
        d.dn.lmap.put(d.getDn().repoName, d.dn.latency);
        d.dn.lmap.put(d.app.computingAppliance.iaas.repositories.get(0).getName(), d.dn.latency);
        d.app.computingAppliance.iaas.repositories.get(0).getLatencies().put(d.getDn().repoName, d.dn.latency);
        System.out.println("Station " + d + " has been connected to a new node");

    }
}
