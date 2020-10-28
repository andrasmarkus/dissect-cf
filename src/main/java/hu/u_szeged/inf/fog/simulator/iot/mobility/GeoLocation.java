package hu.u_szeged.inf.fog.simulator.iot.mobility;

public class GeoLocation {

    private static final double EARTH_RADIUS = 6378.137; //in km
    private double latitude;
    private double longitude;
    private boolean weightPoint = false;

    public GeoLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double calculateDistance(GeoLocation other) {
        double o_longitude = other.getLongitude();
        double o_latitude = other.getLatitude();
        double d_lat = o_latitude * Math.PI / 180 - this.latitude * Math.PI / 180;
        double d_long = o_longitude * Math.PI / 180 - this.longitude * Math.PI / 180;
        double a = Math.sin(d_lat/2) * Math.sin(d_lat/2) +
                Math.cos(this.latitude * Math.PI / 180) * Math.cos(o_latitude * Math.PI / 180) *
                Math.sin(d_long/2) * Math.sin(d_long/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = EARTH_RADIUS * c;
        //So it returns in meters
        return d * 1000;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setWeightPoint(boolean weightPoint) {
        this.weightPoint = weightPoint;
    }

    public boolean isWeightPoint() {
        return weightPoint;
    }
}
