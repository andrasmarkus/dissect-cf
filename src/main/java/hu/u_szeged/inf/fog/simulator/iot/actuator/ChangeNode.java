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
    public void actuate(Station d) {
        from.deviceList.remove(d);

        d.setApp(to);
        to.deviceList.add(d);
        d.dn.lmap.put(d.getDn().repoName, d.dn.latency);
        d.dn.lmap.put(d.app.computingAppliance.iaas.repositories.get(0).getName(), d.dn.latency);
        d.app.computingAppliance.iaas.repositories.get(0).getLatencies().put(d.getDn().repoName, d.dn.latency);
        System.out.println("Application of station " + d + " has been changed");
    }
}
