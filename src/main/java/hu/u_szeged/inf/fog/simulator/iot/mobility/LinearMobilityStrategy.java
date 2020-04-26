package hu.u_szeged.inf.fog.simulator.iot.mobility;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class LinearMobilityStrategy implements MobilityStrategy {

    GeoLocation currentPosition;
    Queue<GeoLocation> destinations = new LinkedList<GeoLocation>();
    double speed; //m/tick

    public LinearMobilityStrategy(GeoLocation currentPosition, double speed, GeoLocation ... destination) {
        this.currentPosition = currentPosition;
        this.destinations.addAll(Arrays.asList(destination));
        this.speed = speed;
    }

    @Override
    public GeoLocation move(long freq) {
        GeoLocation dest = null;
        if(!destinations.isEmpty()) {
            dest = destinations.peek();

            if (dest != null) {
                double distance = currentPosition.calculateDistance(dest);
                System.err.println(distance + " m");
                if (distance > movedDistance(freq)) {
                    double posX = dest.getLongitude() - currentPosition.getLongitude();
                    double posY = dest.getLatitude() - currentPosition.getLatitude();
                    double norm_posX = posX / distance;
                    double norm_posY = posY / distance;
                    currentPosition.setLongitude(currentPosition.getLongitude() + norm_posX * movedDistance(freq));
                    currentPosition.setLatitude(currentPosition.getLatitude() + norm_posY * movedDistance(freq));
                } else {
                    destinations.poll();
                }
                return currentPosition;
            }
        }
        //System.err.println("Longitude " + currentPosition.getLongitude() + " - Latitude" + currentPosition.getLatitude() );
        return null;
    }

    public double movedDistance(long freq) {
        return speed *freq;
    }

}
