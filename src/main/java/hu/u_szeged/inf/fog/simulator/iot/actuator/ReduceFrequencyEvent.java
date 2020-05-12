package hu.u_szeged.inf.fog.simulator.iot.actuator;

import hu.u_szeged.inf.fog.simulator.iot.Station;

public class ReduceFrequencyEvent implements ActuatorEvent {

    private long measure;
    public static long counter = 0;

    public ReduceFrequencyEvent(long measure) {
        this.measure = measure;
    }

    @Override
    public void actuate(Station station) {
        long newFrequency = station.getFrequency() - measure;
        if (newFrequency >= station.sensorCharacteristics.getMinFreq() && newFrequency <= station.sensorCharacteristics.getMaxFreq()) {
            long old = station.getFrequency();
            station.changeFrequency(newFrequency);
            System.out.println("Frequency of station has been decreased from: " + old + " to: " + newFrequency);
            counter++;
        }
    }

}
