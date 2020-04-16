package hu.u_szeged.inf.fog.simulator.task_schedule;

import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.iot.DataCapsule;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class PrioritizedTaskScheduler implements TaskScheduler {

    Queue<DataCapsule> high = new LinkedList<DataCapsule>();
    Queue<DataCapsule> mid = new LinkedList<DataCapsule>();
    Queue<DataCapsule> low = new LinkedList<DataCapsule>();
    Application application;
    Random random = new Random();
    int t1, t2;
    long highSize, midSize, lowSize;
    long currentHighSize, currentMidSize, currentLowSize;

    public PrioritizedTaskScheduler(Application application, int t1, int t2, long highSize, long midSize, long lowSize) {
        this.application = application;
        if(t1 < 0) {
            this.t1 = 0;
        } else if(t1 > 4) {
            this.t1 = 4;
        } else {
            this.t1 = t1;
        }
        if(this.t1 > t2) {
            int tmp = this.t1;
            this.t1 = t2;
            this.t2 = tmp;
        } else if(t2 > 4) {
            this.t2 = 4;
        } else {
            this.t2 = t2;
        }
        this.highSize = highSize;
        this.midSize = midSize;
        this.lowSize = lowSize;
    }

    @Override
    public void assign(DataCapsule dataCapsule) {
        //0-4
        int estimateServiceTime = calculateEstimateServiceTime(this.application);
        //0-2
        int chosenPriority = priorityAssignment(dataCapsule, estimateServiceTime);
        switch (chosenPriority) {
            case 0:
                high.add(dataCapsule);
                currentHighSize+=dataCapsule.size;
                break;
            case 1:
                mid.add(dataCapsule);
                currentMidSize+=dataCapsule.size;
                break;
            case 2:
                low.add(dataCapsule);
                currentLowSize+=dataCapsule.size;
                break;
        }
    }

    private int priorityAssignment(DataCapsule dataCapsule, int estimateServiceTime) {
        int delay = dataCapsule.getMaxToleratedDelay();
        int priorityLevel = dataCapsule.getPriorityLevel();

        if(allContainerAreFull(dataCapsule.size)) {
            //System.err.println("No resources are available");
            return -1;
        }

        if(delay == estimateServiceTime) {
            return 0;
        } else if(t1 < delay && delay <= t2) {
            if(priorityLevel == 1) {
                return 0;
            } else if(priorityLevel == 2) {
                return 1;
            } else {
                return 2;
            }
        } else {
            if(priorityLevel == 1) {
                if(!isContainerFull(currentHighSize, highSize, dataCapsule.size)) {
                    return 0;
                } else {
                    return 1;
                }
            } else if(priorityLevel == 2) {
                if(!isContainerFull(currentMidSize, midSize, dataCapsule.size)) {
                    return 1;
                } else {
                    return 2;
                }
            } else {
                return 2;
            }
        }

    }

    private int calculateEstimateServiceTime(Application application) {
        long unprocessedData = application.sumOfArrivedData - application.sumOfProcessedData;
        double loadRatio = (double) unprocessedData / application.taskSize;
        if(loadRatio <= 0.5) {
            return 0;
        } else if(0.5 < loadRatio && loadRatio <= 0.62) {
            return 1;
        } else if(0.62 < loadRatio && loadRatio <= 0.75) {
            return 2;
        } else if(0.75 < loadRatio && loadRatio <= 0.85) {
            return 3;
        } else {
            return 4;
        }

    }

    private boolean isContainerFull(long currentSize, long containerSize, long newDataSize){
        return currentSize+newDataSize > containerSize;
    }

    private boolean allContainerAreFull(long newDataSize) {
        return currentLowSize+newDataSize > lowSize && currentMidSize+newDataSize > midSize && currentHighSize+newDataSize > highSize;
    }

}
