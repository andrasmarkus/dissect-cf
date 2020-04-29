package hu.u_szeged.inf.fog.simulator.iot.mobility;

import java.util.Random;

public class RandomMobilityStrategy implements MobilityStrategy {

    GeoLocation currentPosition;
    GeoLocation startPosition;
    double radius;
    double speed;
    Random random = new Random();
    static final long LONG_RATIO = 40075000;
    static final long LAT_RATIO = 111320;

    public RandomMobilityStrategy(GeoLocation currentPosition, double radius, double speed) {
        this.currentPosition = currentPosition;
        this.radius = radius;
        this.speed = speed;
        this.startPosition = new GeoLocation(currentPosition.getLatitude(), currentPosition.getLongitude());
    }

    @Override
    public GeoLocation move(long freq) {
        double angle = (random.nextDouble() * 360) * Math.PI / 180;
        double posX = currentPosition.getLongitude();
        double posY = currentPosition.getLatitude();
        posX += Math.cos(angle) * movedLong(freq, posX);
        posY += Math.sin(angle) * movedLat(freq);
        double distance = startPosition.calculateDistance(new GeoLocation(posY, posX));

        if(distance < radius) {
            currentPosition.setLongitude(posX);
            currentPosition.setLongitude(posY);
            //System.err.println("OLD : " + startPosition.getLongitude() + " - " + startPosition.getLatitude() + " NEW: " + posX + " - " + posY);
            return currentPosition;
        } else {
            return null;
        }

    }

    private double movedLong(long freq, double latitude) {
        double d = freq * speed;
        return (d/LONG_RATIO) * Math.cos(latitude * Math.PI/180) / 360;
    }

    private double movedLat(long freq) {
        return freq * speed / LAT_RATIO;
    }

}
