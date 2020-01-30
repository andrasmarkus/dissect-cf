package hu.u_szeged.inf.fog.simulator.iot;

import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;
import hu.u_szeged.inf.fog.simulator.application.Application;

import java.util.Collection;
import java.util.Stack;

public class DataCapsule extends StorageObject {

    private Station source;
    private Application destination;
    private Stack<Application> dataFlowPath;
    private Collection<StorageObject> bulkStorageObject;

    public DataCapsule(String myid, Station source, Application destination) {
        super(myid);
        this.source = source;
        this.destination = destination;
        this.dataFlowPath = new Stack<Application>();
    }

    public DataCapsule(String myid, long mysize, boolean vary, Station source, Application destination) {
        super(myid, mysize, vary);
        this.source = source;
        this.destination = destination;
        this.dataFlowPath = new Stack<Application>();
    }

    public void addToDataPath(Application element) {
        this.dataFlowPath.push(element);
    }

    public void removeFromDataPath(Application element) {
        if (dataFlowPath.contains(element)) {
            this.dataFlowPath.remove(element);
        }
    }

    public Station getSource() {
        return source;
    }

    public void setSource(Station source) {
        this.source = source;
    }

    public Application getDestination() {
        return destination;
    }

    public void setDestination(Application destination) {
        this.destination = destination;
    }

    public Stack<Application> getDataFlowPath() {
        return dataFlowPath;
    }

    public void setDataFlowPath(Stack<Application> dataFlowPath) {
        this.dataFlowPath = dataFlowPath;
    }

    public Collection<StorageObject> getBulkStorageObject() {
        return bulkStorageObject;
    }

    public void setBulkStorageObject(Collection<StorageObject> bulkStorageObject) {
        this.bulkStorageObject = bulkStorageObject;
    }

    public void addToBulkStorageObject(StorageObject storageObject) {
        this.bulkStorageObject.add(storageObject);
    }


    //In case we need an actual tree, maybe an inner class would be handy.
}
