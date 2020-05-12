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
        double moved = movedDistance(freq);
        return setPosition(moved);
    }

    private GeoLocation setPosition(double travelDistance) {
        GeoLocation dest = null;
        if(!destinations.isEmpty()) {
            dest = destinations.peek();

            if (dest != null) {
                double distance = currentPosition.calculateDistance(dest);
                if (distance > travelDistance) {
                    double posX = dest.getLongitude() - currentPosition.getLongitude();
                    double posY = dest.getLatitude() - currentPosition.getLatitude();
                    double norm_posX = posX / distance;
                    double norm_posY = posY / distance;
                    currentPosition.setLongitude(currentPosition.getLongitude() + norm_posX * travelDistance);
                    currentPosition.setLatitude(currentPosition.getLatitude() + norm_posY * travelDistance);
                    return currentPosition;
                } else {
                    double remained = travelDistance - distance;
                    currentPosition = destinations.poll();
                    return setPosition(remained);
                }
            }
        }
        return null;
    }

    public double movedDistance(long freq) {
        return speed *freq;
    }

}
