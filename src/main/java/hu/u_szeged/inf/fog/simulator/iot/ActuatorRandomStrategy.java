package hu.u_szeged.inf.fog.simulator.iot;

import java.util.Random;

public class ActuatorRandomStrategy implements ActuatorStrategy {

    private static final int NUMBER_OF_EVENTS = 3;
    private static final int TOTAL_STOP = 100;
    private static final int ACTUAL_STOP = 5;
    private static final int FREQ_CHANGE_RATIO = 9;


    @Override
    public ActuatorEvent selectEvent() {
        Random rand = new Random();
        int chosenEvent = rand.nextInt(NUMBER_OF_EVENTS);

        switch (chosenEvent) {
            case 0:
                if(rand.nextInt(TOTAL_STOP) <= ACTUAL_STOP) {
                    return new StopStationEvent();
                }
            case 1:
                return new IncreaseFrequencyEvent((rand.nextInt(FREQ_CHANGE_RATIO)+1)*1000);
            case 2:

                return new ReduceFrequencyEvent((rand.nextInt(FREQ_CHANGE_RATIO)+1)*1000);
        }
        return null;
    }
}
