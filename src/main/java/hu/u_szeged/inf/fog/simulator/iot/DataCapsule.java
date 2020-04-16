package hu.u_szeged.inf.fog.simulator.iot;

import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;
import hu.u_szeged.inf.fog.simulator.application.Application;

import java.util.Collection;
import java.util.Random;
import java.util.Stack;

public class DataCapsule extends StorageObject implements Comparable<DataCapsule> {

    private static final int TOTAL = 200;
    private static final int CHANCE_FOR_ACTUATOR = 1;

    private Station source;
    private Application destination;
    private Stack<Application> dataFlowPath;
    private Collection<StorageObject> bulkStorageObject;
    private int eventSize;
    private Boolean actuationNeeded;
    private Boolean fogProcess;
    private long startTime, endTime;
    //0-4 : 0=no delay, 1=minimal delay, 2=medium delay, 3=high delay, 4=very high delay
    private int maxToleratedDelay;
    //1-3: 1=high, 2=mid, 3=low
    private int priorityLevel;



    public DataCapsule(String myid, long mysize, boolean vary, Station source, Application destination, int eventSize, Boolean fogProcess, long startTime, int maxToleratedDelay, int priorityLevel) {
        super(myid, mysize, vary);
        this.source = source;
        this.destination = destination;
        this.eventSize = eventSize;
        this.fogProcess = fogProcess;
        this.startTime = startTime;
        if(priorityLevel < 1) {
            this.priorityLevel = 1;
        } else if(priorityLevel > 3) {
            this.priorityLevel = 3;
        } else {
            this.priorityLevel = priorityLevel;
        }
        this.priorityLevel = priorityLevel;
        if(maxToleratedDelay < 0) {
            this.maxToleratedDelay = 0;
        } else if (maxToleratedDelay > 4) {
            this.maxToleratedDelay = 4;
        } else {
            this.maxToleratedDelay = maxToleratedDelay;
        }
        this.dataFlowPath = new Stack<Application>();
        setActuationNeeded();
    }

    public void addToDataPath(Application element) {
        this.dataFlowPath.push(element);
    }

    @Override
    public int compareTo(DataCapsule dc) {
        if (this.fogProcess.compareTo(dc.fogProcess) == 0) {
            return this.actuationNeeded.compareTo(dc.actuationNeeded);
        }
        return this.fogProcess.compareTo(dc.fogProcess);
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

    public boolean isActuationNeeded() {
        return actuationNeeded;
    }

    private void setActuationNeeded() {
        Random random = new Random();
        actuationNeeded = random.nextInt(TOTAL) <= CHANCE_FOR_ACTUATOR;
    }

    public boolean isFogProcess() {
        return fogProcess;
    }

    public void setFogProcess(boolean fogProcess) {
        this.fogProcess = fogProcess;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getMaxToleratedDelay() {
        return maxToleratedDelay;
    }

    public void setMaxToleratedDelay(int maxToleratedDelay) {
        this.maxToleratedDelay = maxToleratedDelay;
    }

    public int getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(int priorityLevel) {
        this.priorityLevel = priorityLevel;
    }
}
