package hu.u_szeged.inf.fog.simulator.application;


import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;

import java.util.Random;

public class FogApp extends Application {

    public static double TRESHOLD_TO_SEND = 1;

    public FogApp(long freq, long tasksize, String instance, String name, double noi,
                  ComputingAppliance computingAppliance) {
        super(freq, tasksize, instance, name, noi, computingAppliance);

    }

    public ComputingAppliance getParentDeviceOfApp() {
        return computingAppliance.parentApp.computingAppliance;
    }

    public void initiateDataTransferUp(long unprocessedData) throws NetworkException {


        this.computingAppliance.parentApp.incomingData++;
        this.sumOfArrivedData -= unprocessedData;
        if (this.computingAppliance.parentApp.isSubscribed()) {

            final long unprocessed = unprocessedData;
            NetworkNode.initTransfer(unprocessedData, ResourceConsumption.unlimitedProcessing,
                    this.computingAppliance.iaas.repositories.get(0),
                    this.getParentDeviceOfApp().iaas.repositories.get(0), new ConsumptionEvent() {

                        @Override
                        public void conComplete() {
                            moveDataCapsule(computingAppliance.parentApp);
                            computingAppliance.parentApp.sumOfArrivedData += unprocessed;
                            computingAppliance.parentApp.incomingData--;
                        }

                        @Override
                        public void conCancelled(ResourceConsumption problematic) {
                        }
                    });
        } else {
            try {
                this.computingAppliance.parentApp.restartApplication();

                new BrokerCheck(this, this.computingAppliance.parentApp, unprocessedData,
                        (this.computingAppliance.parentApp.freq / 2));
            } catch (VMManagementException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void tick(long fires) {
        long unprocessedData = (this.sumOfArrivedData - this.sumOfProcessedData);
        dataLoad = unprocessedData;
        if (unprocessedData > 0) {

            long processedData = 0;

            while (unprocessedData != processedData) {
                if (unprocessedData - processedData > this.tasksize) {
                    this.allocatedData = this.tasksize;
                } else {
                    this.allocatedData = (unprocessedData - processedData);
                }
                final VmCollector vml = this.VmSearch();
                if (vml == null) {
                    double ratio = ((double) unprocessedData / this.tasksize);

                    if (ratio > TRESHOLD_TO_SEND) {

                        Random rng = new Random();
                        int choice = rng.nextInt(2);


                        ComputingAppliance ca = getNeighbourFromAppliance(TRESHOLD_TO_SEND);
                        if (choice == 1 && ca != null) {
                            System.out.println("Sending Neighbour");
                            this.handleDataTransferToNeighbourAppliance(unprocessedData - processedData, ca);
                        } else {
                            try {
                                this.initiateDataTransferUp(unprocessedData - processedData);
                            } catch (NetworkException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                    System.out.print("data/VM: " + ratio + " unprocessed after exit: " + unprocessedData + " decision:");
                    this.generateAndAddVM();

                    break;

                } else {
                    try {
                        processedData = processData(vml, processedData);
                    } catch (NetworkException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println(" load(%): " + this.getLoadOfCloud());
        }

        this.countVmRunningTime();
        this.turnoffVM();

        if (canUnsubscribe()) {
            unsubscribeApplication();
        }

    }

    @Override
    public String toString() {

        return "fogApp=" + computingAppliance.name + " " + this.computingAppliance.x + " " + this.computingAppliance.y
                + " stations: " + this.ownStations.size();
    }

}
