package hu.u_szeged.inf.fog.simulator.iot.actuator;

import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.iot.Station;

public class DisconnectFromNode implements ActuatorEvent {

    public DisconnectFromNode(){}

    @Override
    public void actuate(Station d) {
        Application application = d.getApp();
        application.deviceList.remove(d);

//        d.dn.lmap.remove(d.getDn().repoName);
//        d.dn.lmap.remove(d.app.computingAppliance.iaas.repositories.get(0).getName());
//        d.app.computingAppliance.iaas.repositories.get(0).getLatencies().remove(d.getDn().repoName);
        d.setApp(null);

        System.out.println(d + " disconnected from current application");
    }
}
