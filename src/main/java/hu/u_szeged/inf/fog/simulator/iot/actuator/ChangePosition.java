package hu.u_szeged.inf.fog.simulator.iot.actuator;

import hu.u_szeged.inf.fog.simulator.iot.Station;
import hu.u_szeged.inf.fog.simulator.iot.mobility.GeoLocation;

public class ChangePosition implements ActuatorEvent {

    private GeoLocation newLocation;
    public static long counter = 0;

    public ChangePosition(GeoLocation geoLocation) {
        this.newLocation = geoLocation;
    }

    @Override
    public void actuate(Station station) {
        station.setGeoLocation(newLocation);
        counter++;
    }
}
