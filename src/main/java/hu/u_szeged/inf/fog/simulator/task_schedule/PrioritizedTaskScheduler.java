package hu.u_szeged.inf.fog.simulator.task_schedule;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;
import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.application.BrokerCheck;
import hu.u_szeged.inf.fog.simulator.iot.DataCapsule;
import hu.u_szeged.inf.fog.simulator.iot.Station;
import hu.u_szeged.inf.fog.simulator.providers.Provider;
import hu.u_szeged.inf.fog.simulator.util.TimelineGenerator;

import java.util.LinkedList;
import java.util.Queue;

public class PrioritizedTaskScheduler implements TaskScheduler {

    Queue<DataCapsule> high = new LinkedList<DataCapsule>();
    Queue<DataCapsule> mid = new LinkedList<DataCapsule>();
    Queue<DataCapsule> low = new LinkedList<DataCapsule>();
    Application application;
    int t1, t2;
    long highSize, midSize, lowSize;
    long currentHighSize, currentMidSize, currentLowSize;

    public PrioritizedTaskScheduler(Application application, int t1, int t2) {
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
        this.highSize = (long) (application.taskSize * 0.2);
        this.midSize = (long) (application.taskSize * 0.3);
        this.lowSize = (long) (application.taskSize * 0.5);
    }

    @Override
    public void process() {
        long unprocessedData = application.sumOfArrivedData - application.sumOfProcessedData;
        if (unprocessedData > 0) {
            long alreadyProcessedData = 0;
            while (unprocessedData != alreadyProcessedData) {
                if (unprocessedData - alreadyProcessedData > application.taskSize) {
                    application.allocatedData = application.taskSize;
                } else {
                    application.allocatedData = (unprocessedData - alreadyProcessedData);
                }

                final Application.VmCollector vmCollector = application.VmSearch();
                if (vmCollector == null) {

                    double ratio = (int) ((double) unprocessedData / application.taskSize);

                    if (ratio > application.threshold) {
                        application.strategy(unprocessedData - alreadyProcessedData);
                    }

                    System.out.println("data/VM: " + ratio + " unprocessed after exit: " + unprocessedData + " decision:");
                    application.generateAndAddVM();
                    break;
                }

                try {
                    final double noi = application.allocatedData == application.taskSize ? application.numberOfInstruction
                            : (application.numberOfInstruction * application.allocatedData / application.taskSize);

                    alreadyProcessedData += application.allocatedData;
                    removeFromContainer(application.allocatedData);
                    vmCollector.isWorking = true;
                    application.currentTask++;

                    vmCollector.vm.newComputeTask(noi, ResourceConsumption.unlimitedProcessing, new ConsumptionEventAdapter() {
                        long vmStartTime = Timed.getFireCount();
                        long allocatedDataTemp = application.allocatedData;
                        double noiTemp = noi;

                        @Override
                        public void conComplete() {
                            vmCollector.isWorking = false;
                            vmCollector.taskCounter++;
                            application.currentTask--;

                            application.stopTime = Timed.getFireCount();
                            application.timelineList.add(new TimelineGenerator.TimelineCollector(vmStartTime, Timed.getFireCount(), vmCollector.id));
                            System.out.println(application.name + " " + vmCollector.id + " started@ " + vmStartTime + " finished@ "
                                    + Timed.getFireCount() + " with " + allocatedDataTemp + " bytes, lasted "
                                    + (Timed.getFireCount() - vmStartTime) + " ,noi: " + noiTemp);

                        }
                    });
                    application.sumOfProcessedData += application.allocatedData;
                } catch (NetworkNode.NetworkException e) {
                    e.printStackTrace();
                }

            }
            System.out.println(" load(%): " + application.getloadOfResource());
        }

        application.countVmRunningTime();
        application.turnoffVM();
        if (application.currentTask == 0 && application.incomingData == 0 && application.sumOfProcessedData == application.sumOfArrivedData
                && application.checkDeviceState()) {
            application.unsub();

            for (Provider p : application.providers) {
                if (p.isSubscribed()) {
                    p.shouldStop = true;
                }
            }
            StorageObject so = new StorageObject(application.name, application.sumOfProcessedData, false);
            if (!application.computingAppliance.iaas.repositories.get(0).registerObject(so)) {
                application.computingAppliance.iaas.repositories.get(0).deregisterObject(so);
                application.computingAppliance.iaas.repositories.get(0).registerObject(so);
            }

            for (Application.VmCollector vmcl : application.vmManagerlist) {
                try {
                    if (vmcl.vm.getState().equals(VirtualMachine.State.RUNNING)) {
                        if (vmcl.id.equals("broker")) {
                            vmcl.pm = vmcl.vm.getResourceAllocation().getHost();
                        }
                        vmcl.vm.switchoff(false);
                    }
                } catch (VirtualMachine.StateChangeException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void afterProcess(DataCapsule dataCapsule) {
        if(dataCapsule.isActuationNeeded()) {
            try {
                sendToStation(dataCapsule);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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


    @Override
    public void moveDataCapsule(long size, Application source, Application destination, int direction) {
        long counter = 0;
        DataCapsule toSend;

        while (counter < size) {

            if(direction == -1) {
                toSend = source.backwardDataCapsules.poll();
                if(toSend != null) {
                    destination.backwardDataCapsules.add(toSend);
                    counter += toSend.getEventSize();
                } else {
                    break;
                }
            } else if (!high.isEmpty()) {
                toSend = high.poll();
                if (destination != null) {
                    movingOperation(destination, toSend);
                    counter += toSend.size;
                    currentHighSize -= toSend.size;
                }
            } else if (!mid.isEmpty()) {
                toSend = mid.poll();
                if (destination != null) {
                    movingOperation(destination, toSend);
                    counter += toSend.size;
                    currentMidSize -= toSend.size;
                }
            } else if (!low.isEmpty()) {
                toSend = low.poll();
                if (destination != null) {
                    movingOperation(destination, toSend);
                    counter += toSend.size;
                    currentLowSize -= toSend.size;
                }
            } else {
                break;
            }
        }
    }

    private void movingOperation(Application destination, DataCapsule toSend) {
        //TODO: add to path when registering

        toSend.setDestination(destination);
        toSend.addToDataPath(destination);
        destination.registerDataCapsule(toSend);

    }

    private int priorityAssignment(DataCapsule dataCapsule, int estimateServiceTime) {
        int delay = dataCapsule.getMaxToleratedDelay();
        int priorityLevel = dataCapsule.getPriorityLevel();

        if(allContainerAreFull(dataCapsule.size)) {
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
                } else if (!isContainerFull(currentMidSize, midSize, dataCapsule.size)) {
                    return 1;
                } else {
                    return 2;
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
        double loadRatio = application.getloadOfResource();

        if (application.VmSearch() != null) {
            return 0;
        }

        if (loadRatio <= 50.0) {
            return 0;
        } else if (50.0 < loadRatio && loadRatio <= 62.0) {
            return 1;
        } else if (62.0 < loadRatio && loadRatio <= 75.0) {
            return 2;
        } else if (75.0 < loadRatio && loadRatio <= 85.0) {
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

    private void removeFromContainer(long sizeToRemove) {

        if (!high.isEmpty()) {
            int size = high.size();
            for (int i = 0; i < size; i++) {
                if (sizeToRemove >= high.peek().size) {
                    DataCapsule dc = high.poll();
                    if (dc != null && dc.isActuationNeeded()) {
                        dc.setProcessTime(Timed.getFireCount());
                        dc.setActuatorEvent(dc.getSource().getActuator().selectStrategyEvent());
                        afterProcess(dc);
                        sizeToRemove -= dc.size;
                        currentHighSize -= dc.size;
                    }
                }
            }
        }

        if (!mid.isEmpty()) {
            int size = mid.size();
            for (int i = 0; i < size; i++) {
                if (sizeToRemove >= mid.peek().size) {
                    DataCapsule dc = mid.poll();
                    if (dc != null && dc.isActuationNeeded()) {
                        dc.setProcessTime(Timed.getFireCount());
                        dc.setActuatorEvent(dc.getSource().getActuator().selectStrategyEvent());
                        afterProcess(dc);
                        sizeToRemove -= dc.size;
                        currentMidSize -= dc.size;
                    }
                }
            }
        }

        if (!low.isEmpty()) {
            int size = low.size();
            for (int i = 0; i < size; i++) {
                if (sizeToRemove >= low.peek().size) {
                    DataCapsule dc = low.poll();
                    if (dc != null && dc.isActuationNeeded()) {
                        dc.setProcessTime(Timed.getFireCount());
                        dc.setActuatorEvent(dc.getSource().getActuator().selectStrategyEvent());
                        afterProcess(dc);
                        sizeToRemove -= dc.size;
                        currentLowSize -= dc.size;
                    }
                }
            }
        }

    }

    private void sendToStation(DataCapsule dataCapsule) throws Exception {

        Application currentApp = dataCapsule.getDestination();
        currentApp.backwardDataCapsules.add(dataCapsule);
        while (!dataCapsule.getDataFlowPath().isEmpty()) {
            Application nextApp = dataCapsule.getDataFlowPath().pop();
            if(nextApp != currentApp) {
                if(nextApp.isSubscribed()) {
                    try {
                        long toProcess = dataCapsule.getEventSize();
                        currentApp.initiateDataTransfer(toProcess, currentApp, nextApp, -1);
                        currentApp = nextApp;
                    } catch (NetworkNode.NetworkException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        nextApp.restartApplication();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    new BrokerCheck(currentApp, nextApp, dataCapsule.getEventSize(), (nextApp.freq / 2));
                }
            }
        }
        try {
            NetworkNode.initTransfer(dataCapsule.getEventSize(), ResourceConsumption.unlimitedProcessing,
                    currentApp.computingAppliance.iaas.repositories.get(0), dataCapsule.getSource().getDn().localRepository,
                    new Station.ActualizationEvent(dataCapsule.getSource(), dataCapsule));
            currentApp.backwardDataCapsules.remove(dataCapsule);
        } catch (NetworkNode.NetworkException e) {
            e.printStackTrace();
        }


    }


}
