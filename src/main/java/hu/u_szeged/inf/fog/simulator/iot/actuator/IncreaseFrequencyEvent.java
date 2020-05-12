package hu.u_szeged.inf.fog.simulator.iot.actuator;

import hu.u_szeged.inf.fog.simulator.iot.Station;

public class IncreaseFrequencyEvent implements ActuatorEvent {
    public static int counter = 0;
    private long measure;

    public IncreaseFrequencyEvent(long measure) {
        this.measure = measure;
    }

    @Override
    public void actuate(Station station) {
        long newFrequency = measure + station.getFrequency();
        if (newFrequency >= station.sensorCharacteristics.getMinFreq() && newFrequency <= station.sensorCharacteristics.getMaxFreq()) {
            long old = station.getFrequency();
            station.changeFrequency(newFrequency);
            System.out.println("Frequency of station has been increased from: " + old + " to: " + newFrequency);
            counter++;
        }
    }

}
