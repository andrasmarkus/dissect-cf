package hu.u_szeged.inf.fog.simulator.iot;

import hu.u_szeged.inf.fog.simulator.iot.mobility.GeoLocation;

import java.util.Random;

public interface ActuatorEvent {

    void actuate(Station station);

}

class RestartStationEvent implements ActuatorEvent {

    @Override
    public void actuate(Station station) {
        //TODO: this still needs to be fixed
        station.restart();
    }

}

class IncreaseFrequencyEvent implements ActuatorEvent {

    private long measure;

    IncreaseFrequencyEvent(long measure) {
        this.measure = measure;
    }

    @Override
    public void actuate(Station station) {
        long newFrequency = measure + station.getFrequency();
        if (newFrequency >= station.sensorCharacteristics.getMinFreq() && newFrequency <= station.sensorCharacteristics.getMaxFreq()) {
            long old = station.getFrequency();
            station.changeFrequency(newFrequency);
            System.out.println("Frequency of station has been increased from: " + old + " to: " + newFrequency);
        }
    }

}

class ReduceFrequencyEvent implements ActuatorEvent {

    private long measure;

    ReduceFrequencyEvent(long measure) {
        this.measure = measure;
    }

    @Override
    public void actuate(Station station) {
        long newFrequency = station.getFrequency() - measure;
        if (newFrequency >= station.sensorCharacteristics.getMinFreq() && newFrequency <= station.sensorCharacteristics.getMaxFreq()) {
            long old = station.getFrequency();
            station.changeFrequency(newFrequency);
            System.out.println("Frequency of station has been decreased from: " + old + " to: " + newFrequency);
        }
    }

}

class StopStationEvent implements  ActuatorEvent {

    @Override
    public void actuate(Station station) {
        station.stopMeter();
        System.out.println("Station " + station + " has been stopped");
    }
}

class ChangeFileSize implements ActuatorEvent {

    private long filesize;

    ChangeFileSize(long filesize) {
        this.filesize = filesize;
    }

    @Override
    public void actuate(Station station) {
        if(filesize > 0) {
            station.setFilesize(filesize);
            System.out.println(station + " station's filesize has been changed to " + filesize);
        }
    }
}

class ChangePosition implements ActuatorEvent {

    private GeoLocation newLocation;

    ChangePosition(GeoLocation geoLocation) {
        this.newLocation = geoLocation;
    }

    @Override
    public void actuate(Station station) {
        System.out.println("Location has been changed from " +station.geoLocation.getLongitude() + " - " + station.geoLocation.getLatitude() + " TO: " +
                newLocation.getLongitude() + " - " + newLocation.getLatitude());
        station.setGeoLocation(newLocation);
    }
}
