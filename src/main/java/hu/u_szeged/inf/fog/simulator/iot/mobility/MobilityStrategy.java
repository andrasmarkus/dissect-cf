package hu.u_szeged.inf.fog.simulator.iot.mobility;

public interface MobilityStrategy {
    GeoLocation move(long freq);
}
