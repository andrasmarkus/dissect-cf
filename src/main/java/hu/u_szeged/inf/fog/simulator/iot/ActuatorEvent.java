package hu.u_szeged.inf.fog.simulator.iot;

import java.util.Random;

public interface ActuatorEvent {

    void actuate(Station station);

    long executionTime();
}

class RestartStationEvent implements ActuatorEvent {

    private final int maxTime = 15;
    private final int minTime = 10;

    @Override
    public void actuate(Station station) {
        //Kinda hacky
        station.restart();
    }

    @Override
    public long executionTime() {
        return new Random().nextInt((maxTime - minTime) + 1) + minTime;
    }
}

class IncreaseFrequencyEvent implements ActuatorEvent {

    private final int maxTime = 6;
    private final int minTime = 3;
    private int ratio;

    IncreaseFrequencyEvent(int ratio) {
        this.ratio = ratio;
    }

    @Override
    public void actuate(Station station) {
        if (ratio > 0) {
            long newFrequency = ratio * station.getFrequency();
            station.changeFrequency(newFrequency);
        }
    }

    @Override
    public long executionTime() {
        return new Random().nextInt((maxTime - minTime) + 1) + minTime;
    }
}

class ReduceFrequencyEvent implements ActuatorEvent {

    private final int maxTime = 6;
    private final int minTime = 3;
    private int ratio;

    ReduceFrequencyEvent(int ratio) {
        this.ratio = ratio;
    }

    @Override
    public void actuate(Station station) {
        if (ratio > 0) {
            long newFrequency = station.getFrequency() / ratio;
            station.changeFrequency(newFrequency);
        }
    }

    @Override
    public long executionTime() {
        return new Random().nextInt((maxTime - minTime) + 1) + minTime;
    }
}
