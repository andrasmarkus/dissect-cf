package hu.u_szeged.inf.fog.simulator.application;


import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;


public class CloudApp extends Application {

    public static double TRESHOLD_TO_SEND = 1;

    public CloudApp(long freq, long tasksize, String instance, String name, double noi, ComputingAppliance computingAppliance) {
        super(freq, tasksize, instance, name, noi, computingAppliance);

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
                        ComputingAppliance ca = getNeighbourFromAppliance(TRESHOLD_TO_SEND);
                        if (ca != null) {
                            this.handleDataTransferToNeighbourAppliance(unprocessedData - processedData, ca);
                        }
                    }

                    System.out.print("data/VM: " + ratio + " unprocessed after exit: " + unprocessedData + " decision:");
                    this.generateAndAddVM();

                    break;
                }
                try {
                    processedData = processData(vml, processedData);
                } catch (NetworkException e) {
                    e.printStackTrace();
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

}
