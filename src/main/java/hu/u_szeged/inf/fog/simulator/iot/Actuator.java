package hu.u_szeged.inf.fog.simulator.iot;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;

public class Actuator extends DeferredEvent {

    private ActuatorStrategy strategy;
    private Station station;

    public Actuator(ActuatorStrategy actuatorStrategy, Station station, long delay) {
        super(delay);
        this.strategy = actuatorStrategy;
        this.station = station;
    }

    @Override
    protected void eventAction() {
        new DeferredEvent(strategy.selectEvent().executionTime()) {
            @Override
            protected void eventAction() {
                strategy.selectEvent().actuate(station);
            }
        };
    }

    public ActuatorStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(ActuatorStrategy strategy) {
        this.strategy = strategy;
    }


}
