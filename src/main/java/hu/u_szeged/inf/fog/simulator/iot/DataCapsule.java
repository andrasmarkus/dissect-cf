package hu.u_szeged.inf.fog.simulator.iot;

import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;
import hu.u_szeged.inf.fog.simulator.application.Application;

import java.util.Collection;
import java.util.Random;
import java.util.Stack;

public class DataCapsule extends StorageObject {

    private static final int TOTAL = 200;
    private static final int CHANCE_FOR_ACTUATOR = 1;

    private Station source;
    private Application destination;
    private Stack<Application> dataFlowPath;
    private Collection<StorageObject> bulkStorageObject;
    private int eventSize;
    private boolean actuatorNeeded;

    public DataCapsule(String myid, long mysize, boolean vary, Station source, Application destination, int eventSize) {
        super(myid, mysize, vary);
        this.source = source;
        this.destination = destination;
        this.eventSize = eventSize;
        this.dataFlowPath = new Stack<Application>();
        setActuatorNeeded();
    }

    public void addToDataPath(Application element) {
        this.dataFlowPath.push(element);
    }


    public Station getSource() {
        return source;
    }

    public void setSource(Station source) {
        this.source = source;
    }

    public void setDestination(Application destination) {
        this.destination = destination;
    }

    public Stack<Application> getDataFlowPath() {
        return dataFlowPath;
    }

    public Collection<StorageObject> getBulkStorageObject() {
        return bulkStorageObject;
    }

    public void setBulkStorageObject(Collection<StorageObject> bulkStorageObject) {
        this.bulkStorageObject = bulkStorageObject;
    }

    public int getEventSize() {
        return eventSize;
    }

    public boolean isActuatorNeeded() {
        return actuatorNeeded;
    }

    private void setActuatorNeeded() {
        Random random = new Random();
        actuatorNeeded = random.nextInt(TOTAL) <= CHANCE_FOR_ACTUATOR;
    }

}
