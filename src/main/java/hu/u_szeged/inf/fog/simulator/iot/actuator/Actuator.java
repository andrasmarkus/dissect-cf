package hu.u_szeged.inf.fog.simulator.iot.actuator;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.u_szeged.inf.fog.simulator.iot.Station;

public class Actuator{

    private ActuatorStrategy strategy;
    private long latency;
    private Station station;
    public static int counter=0;

    public Actuator(ActuatorStrategy actuatorStrategy, long latency, Station station) {
        this.strategy = actuatorStrategy;
        this.latency = latency;
        this.station = station;
    }

    public ActuatorEvent selectStrategyEvent() {
        return strategy.selectEvent(station);
    }

    public void executeEvent(final ActuatorEvent event) {
        if(event != null) {
            new DeferredEvent(latency) {
                @Override
                protected void eventAction() {
                    event.actuate(station);
                    counter++;
                }
            };
        }
    }

    public void executeSingleEvent(final ActuatorEvent event, final Station station, long latency) {
        if(event != null) {
            new DeferredEvent(latency) {
                @Override
                protected void eventAction() {
                    event.actuate(station);
                    counter++;
                }
            };
        }
    }

    public ActuatorStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(ActuatorStrategy strategy) {
        this.strategy = strategy;
    }


    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }
}
