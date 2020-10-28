package hu.u_szeged.inf.fog.simulator.iot.mobility;

import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.iot.Station;
import hu.u_szeged.inf.fog.simulator.iot.actuator.Actuator;
import hu.u_szeged.inf.fog.simulator.iot.actuator.ChangeNode;
import hu.u_szeged.inf.fog.simulator.iot.actuator.ConnectToNode;
import hu.u_szeged.inf.fog.simulator.iot.actuator.DisconnectFromNode;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;

public class DecisionMaker {

    Station station;

    public DecisionMaker(Station s) {
        this.station = s;
        handleDisconnectFromNode();
        handleConnectToNode();
    }

    private void handleDisconnectFromNode() {
        Application current = station.getApp();
        if(current != null) {
            if(calculateLatency(current.computingAppliance) > station.sensorCharacteristics.getMaxLatency()) {
                try {
                    disconnectFromApp(current);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (station.geoLocation.calculateDistance(current.computingAppliance.location) > current.computingAppliance.range) {
                try {
                    disconnectFromApp(current);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleConnectToNode() {
        //If it is currently attached to a node, but has a better one
        Application current = station.getApp();
        Application closest = closestApp();
        if (current != null) {
            if (closest != current && canConnectToApp(closest)) {
                //Change node
                try {
                    connectToApp(current, closest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //If it it currently NOT connected to a node
        else {
            if(canConnectToApp(closest)) {
                try {
                    connectToApp(closest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private boolean canConnectToApp(Application closest) {
        return calculateLatency(closest.computingAppliance) <= station.sensorCharacteristics.getMaxLatency()
                && station.geoLocation.calculateDistance(closest.computingAppliance.location) <= closest.computingAppliance.range
                && closest.getloadOfResource() < 70.0;
    }

    private Application closestApp() {
        double minLatency = Double.MAX_VALUE;
        Application closest = null;
        for (Application app : Application.allApplication) {
            ComputingAppliance ca = app.computingAppliance;
            if (calculateLatency(ca) < minLatency && app.canJoin) {
                minLatency = calculateLatency(ca);
                closest = app;
            }
        }
        return closest;
    }

    private double calculateLatency() {
        if (station.getApp() != null) {
            return station.geoLocation.calculateDistance(station.getApp().computingAppliance.location) / (50 * 1000) * 1.5;
        } else {
            return Double.MAX_VALUE;
        }
    }

    private double calculateLatency(ComputingAppliance ca) {
        return station.geoLocation.calculateDistance(ca.location) / (50 * 1000) * 1.5;
    }

    private void disconnectFromApp(final Application application) throws Exception {
        Actuator actuator = station.getActuator();

        if (actuator != null) {
            actuator.executeSingleEvent(new DisconnectFromNode(), station, 0);
        } else {
            throw new Exception("Actuator must be set in order to disconnect from fog node!");
        }

    }

    private void connectToApp(final Application from, final Application to) throws Exception {
        Actuator actuator = station.getActuator();
        if(actuator != null) {
            actuator.executeSingleEvent(new ChangeNode(from, to), station, 0);
        } else {
            throw new Exception("Actuator must be set in order to connect to a node");
        }
    }

    private void connectToApp(final Application application) throws Exception {
        Actuator actuator = station.getActuator();
        if(actuator != null) {
            actuator.executeSingleEvent(new ConnectToNode(application), station, 0);
        } else {
            throw new Exception("Actuator must be set in order to connect to a node");
        }
    }
}
