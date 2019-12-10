package hu.u_szeged.inf.fog.simulator.iot;

import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;

import java.util.Stack;

public class DataCapsule<T> extends StorageObject {

    private T source;
    private T destination;
    private Stack<T> dataFlowPath;


    public DataCapsule(String myid, T source, T destination) {
        super(myid);
        this.source = source;
        this.destination = destination;
        this.dataFlowPath = new Stack<T>();
    }

    public DataCapsule(String myid, long mysize, boolean vary, T source, T destination) {
        super(myid, mysize, vary);
        this.source = source;
        this.destination = destination;
        this.dataFlowPath = new Stack<T>();
    }

    public void addToDataPath(T element) {
        this.dataFlowPath.push(element);
    }

    public void removeFromDataPath(T element) {
        if (dataFlowPath.contains(element)) {
            this.dataFlowPath.remove(element);
        }
    }

    public void updateCurrentNodes(T source, T destination) {
        setSource(source);
        setDestination(destination);
    }

    public T getSource() {
        return source;
    }

    public void setSource(T source) {
        this.source = source;
    }

    public T getDestination() {
        return destination;
    }

    public void setDestination(T destination) {
        this.destination = destination;
    }

    public Stack<T> getDataFlowPath() {
        return dataFlowPath;
    }

    public void setDataFlowPath(Stack<T> dataFlowPath) {
        this.dataFlowPath = dataFlowPath;
    }

    //In case we need an actual tree, maybe an inner class would be handy.
}
