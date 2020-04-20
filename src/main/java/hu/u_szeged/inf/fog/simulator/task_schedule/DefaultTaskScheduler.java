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

import java.util.PriorityQueue;

public class DefaultTaskScheduler implements TaskScheduler {

    private PriorityQueue<DataCapsule> forwardDataCapsules = new PriorityQueue<DataCapsule>();
    private Application application;

    public DefaultTaskScheduler(Application application) {
        this.application = application;
    }

    @Override
    public void assign(DataCapsule dc) {
        forwardDataCapsules.add(dc);
    }

    @Override
    public void process() {
        long unprocessedData = (application.sumOfArrivedData - application.sumOfProcessedData);
        if (unprocessedData > 0) {

            long alreadyProcessedData = 0;

            //As long as we have unprocessed data
            while (unprocessedData != alreadyProcessedData) {
                if (unprocessedData - alreadyProcessedData > application.taskSize) {
                    application.allocatedData = application.taskSize;
                } else {
                    application.allocatedData = (unprocessedData - alreadyProcessedData);
                }

                final Application.VmCollector vml = application.VmSearch();
                //Is there a virtual machine that can process the data?
                if (vml == null) {

                    double ratio = (int) ((double) unprocessedData / application.taskSize);

                    //Send it over
                    if (ratio > application.threshold) {
                        application.strategy(unprocessedData - alreadyProcessedData);
                    }

                    System.out
                            .print("data/VM: " + ratio + " unprocessed after exit: " + unprocessedData + " decision:");
                    application.generateAndAddVM();

                    break;
                }

                //We can process the data
                try {
                    final double noi = application.allocatedData == application.taskSize ? application.numberOfInstruction
                            : (application.numberOfInstruction * application.allocatedData / application.taskSize);

                    alreadyProcessedData += application.allocatedData;
                    vml.isWorking = true;
                    application.currentTask++;

                    vml.vm.newComputeTask(noi, ResourceConsumption.unlimitedProcessing, new ConsumptionEventAdapter() {
                        long vmStartTime = Timed.getFireCount();
                        long allocatedDataTemp = application.allocatedData;
                        double noiTemp = noi;

                        @Override
                        public void conComplete() {
                            vml.isWorking = false;
                            vml.taskCounter++;
                            application.currentTask--;

                            application.stopTime = Timed.getFireCount();
                            application.timelineList.add(new TimelineGenerator.TimelineCollector(vmStartTime, Timed.getFireCount(), vml.id));
                            System.out.println(application.name + " " + vml.id + " started@ " + vmStartTime + " finished@ "
                                    + Timed.getFireCount() + " with " + allocatedDataTemp + " bytes, lasted "
                                    + (Timed.getFireCount() - vmStartTime) + " ,noi: " + noiTemp);

                        }
                    });
                    application.sumOfProcessedData += application.allocatedData;
                    removeFromContainer(application.allocatedData);
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
    public void moveDataCapsule(long size, Application source, Application destination, int direction) {
        long counter = 0;
        DataCapsule toSend;

        while (counter < size) {
            if (direction == -1) {
                toSend = source.backwardDataCapsules.poll();
                if (toSend != null) {
                    destination.backwardDataCapsules.add(toSend);
                    counter += toSend.getEventSize();
                } else {
                    break;
                }
            } else {
                if(!forwardDataCapsules.isEmpty()) {
                    toSend = forwardDataCapsules.poll();
                    if(destination != null) {
                        toSend.setDestination(destination);
                        toSend.addToDataPath(destination);
                        destination.registerDataCapsule(toSend);
                        counter += toSend.size;
                    }
                }
            }
        }
    }

    @Override
    public void afterProcess(DataCapsule dc) {
        if(dc.isActuationNeeded()) {
            try {
                sendToStation(dc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendToStation(DataCapsule dc) throws Exception {
        //this.application.backwardDataCapsules.add(dc);
        Application currentApp = dc.getDestination();
        currentApp.backwardDataCapsules.add(dc);
        while (!dc.getDataFlowPath().isEmpty()) {
            Application nextApp = dc.getDataFlowPath().pop();
            if(nextApp != currentApp) {
                if(nextApp.isSubscribed()) {
                    try {
                        long toProcess = dc.getEventSize();
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
                    new BrokerCheck(currentApp, nextApp, dc.getEventSize(), (nextApp.freq / 2));
                }
            }
        }

        if (currentApp != dc.getSource().getApp()) {
            throw new Exception("Station cannot be reached!");
        } else {

            NetworkNode.initTransfer(dc.getEventSize(), ResourceConsumption.unlimitedProcessing,
                    currentApp.computingAppliance.iaas.repositories.get(0), dc.getSource().getDn().localRepository,
                    new Station.ActualizationEvent(dc.getSource(), dc));
            currentApp.backwardDataCapsules.remove(dc);

        }
    }


    private void removeFromContainer(long sizeToRemove) {
        if (!forwardDataCapsules.isEmpty()) {
            int size = forwardDataCapsules.size();
            System.err.println(size);
            for (int i = 0; i < size; i++) {
                if (sizeToRemove >= forwardDataCapsules.peek().size) {
                    DataCapsule dc = forwardDataCapsules.poll();
                    if (dc != null) {
                        dc.setProcessTime(Timed.getFireCount());
                        afterProcess(dc);
                        sizeToRemove -= dc.size;
                    }
                }
            }
        }
    }

}
