package hu.u_szeged.inf.fog.simulator.iot.actuator;

import hu.u_szeged.inf.fog.simulator.iot.Station;
import hu.u_szeged.inf.fog.simulator.iot.mobility.GeoLocation;

public class ChangePosition implements ActuatorEvent {

    private GeoLocation newLocation;

    public ChangePosition(GeoLocation geoLocation) {
        this.newLocation = geoLocation;
    }

    @Override
    public void actuate(Station station) {
        System.out.println("Location has been changed from " +station.geoLocation.getLongitude() + " - " + station.geoLocation.getLatitude() + " TO: " +
                newLocation.getLongitude() + " - " + newLocation.getLatitude());
        station.setGeoLocation(newLocation);
    }
}
