package hu.u_szeged.inf.fog.simulator.iot.actuator;

import hu.u_szeged.inf.fog.simulator.iot.Station;

public class ChangeFileSize implements ActuatorEvent {

    private long filesize;
    public static long counter = 0;

    public ChangeFileSize(long filesize) {
        this.filesize = filesize;
    }

    @Override
    public void actuate(Station station) {
        if(filesize > 0) {
            station.setFilesize(filesize);
            System.out.println(station + " station's filesize has been changed to " + filesize);
            counter++;
        }
    }
}
