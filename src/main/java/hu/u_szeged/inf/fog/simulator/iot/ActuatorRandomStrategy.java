package hu.u_szeged.inf.fog.simulator.iot;

import java.util.Random;

public class ActuatorRandomStrategy implements ActuatorStrategy {

    private static final int NUMBER_OF_EVENTS = 4;
    private static final int TOTAL_STOP = 100;
    private static final int ACTUAL_STOP = 5;
    private static final int FREQ_CHANGE_RATIO = 4;

    private static final int MAX_FILESIZE = 750;
    private static final int MIN_FILESIZE = 50;


    @Override
    public ActuatorEvent selectEvent(Station station) {
        Random rand = new Random();
        int chosenEvent = rand.nextInt(NUMBER_OF_EVENTS);

        switch (chosenEvent) {
            case 0:
                if(rand.nextInt(TOTAL_STOP) <= ACTUAL_STOP) {
                    //return new StopStationEvent();
                }
                break;
            case 1:
                return new IncreaseFrequencyEvent((rand.nextInt(FREQ_CHANGE_RATIO)+1)*1000);
            case 2:
                return new ReduceFrequencyEvent((rand.nextInt(FREQ_CHANGE_RATIO)+1)*1000);
            case 3:
                return new ChangeFileSize((long) rand.nextInt(MAX_FILESIZE - MIN_FILESIZE) + 1+ MIN_FILESIZE);

        }
        return null;
    }
}
