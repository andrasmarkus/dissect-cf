package hu.u_szeged.inf.fog.simulator.iot;

public class Actuator{

    private ActuatorStrategy strategy;

    public Actuator(ActuatorStrategy actuatorStrategy) {
        this.strategy = actuatorStrategy;
    }

    public void executeEventOn(Station station) {
        ActuatorEvent event = strategy.selectEvent(station);
        if(event != null) {
            event.actuate(station);
        }
    }

    public void executeSingleEvent(ActuatorEvent event, Station station) {
        event.actuate(station);
    }

    public ActuatorStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(ActuatorStrategy strategy) {
        this.strategy = strategy;
    }


}
