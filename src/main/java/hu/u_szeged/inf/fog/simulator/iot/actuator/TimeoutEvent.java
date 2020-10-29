package hu.u_szeged.inf.fog.simulator.iot.actuator;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;
import hu.u_szeged.inf.fog.simulator.iot.Station;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TimeoutEvent implements ActuatorEvent {
    public static long counter = 0;
    public static long unprocessed = 0;
    public TimeoutEvent(){}

    @Override
    public void actuate(Station station) {
        unprocessed += station.getFilesize();
        Collection<StorageObject> contents = station.dn.localRepository.contents();
        List<StorageObject> list = new ArrayList<StorageObject>(contents);
        for (StorageObject so : list) {
            station.dn.localRepository.deregisterObject(so);
        }
        counter++;
    }
}
