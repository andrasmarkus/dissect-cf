package hu.u_szeged.inf.fog.simulator.iot;

public class Actuator{

    private ActuatorStrategy strategy;

    public Actuator(ActuatorStrategy actuatorStrategy) {
        this.strategy = actuatorStrategy;
    }

    public void executeEventOn(Station station) {
        strategy.selectEvent().actuate(station);
    }

    public ActuatorStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(ActuatorStrategy strategy) {
        this.strategy = strategy;
    }


}
