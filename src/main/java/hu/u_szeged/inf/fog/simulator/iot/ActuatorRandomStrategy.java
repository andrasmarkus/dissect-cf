package hu.u_szeged.inf.fog.simulator.iot;

import java.util.Random;

public class ActuatorRandomStrategy implements ActuatorStrategy {

    public static final int NUMBER_OF_EVENTS = 3;

    @Override
    public ActuatorEvent selectEvent() {
        Random rand = new Random();
        int chosenEvent = rand.nextInt(NUMBER_OF_EVENTS);

        switch (chosenEvent) {
            case 0:
                return new RestartStationEvent();
            case 1:
                return new IncreaseFrequencyEvent(rand.nextInt(4));
            case 2:
                return new ReduceFrequencyEvent(rand.nextInt(3) + 1);
        }
        return null;
    }
}
