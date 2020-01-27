package hu.u_szeged.inf.fog.simulator.application;


import java.util.Random;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.StateChangeException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;
import hu.u_szeged.inf.fog.simulator.providers.Provider;
import hu.u_szeged.inf.fog.simulator.util.TimelineGenerator.TimelineCollector;
import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;

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
						} 
						else {
							try {
								this.initiateDataTransferUp(unprocessedData - processedData);
							} catch (NetworkException e) {
								e.printStackTrace();
							}
						}

					}

					System.out
							.print("data/VM: " + ratio + " unprocessed after exit: " + unprocessedData + " decision:");
					this.generateAndAddVM();

					break;

				} else {
					try {
				final double noi = this.allocatedData == this.tasksize ? defaultNoi
							: (double) (2400 * this.allocatedData / this.tasksize);
						//final double noi = 2500000; -> this is only for iFogSim comparison
						processedData += this.allocatedData;
						vml.isWorking = true;
						this.currentTask++;

						vml.vm.newComputeTask(noi, ResourceConsumption.unlimitedProcessing,
								new ConsumptionEventAdapter() {
									long vmStartTime = Timed.getFireCount();
									long allocatedDataTemp = allocatedData;
									double noiTemp = noi;

									@Override
									public void conComplete() {
										vml.isWorking = false;
										vml.taskCounter++;
										currentTask--;
										stopTime = Timed.getFireCount();
										timelineList
												.add(new TimelineCollector(vmStartTime, Timed.getFireCount(), vml.id));
										System.out.println(name + " " + vml.id + " started@ " + vmStartTime
												+ " finished@ " + Timed.getFireCount() + " with " + allocatedDataTemp
												+ " bytes, lasted " + (Timed.getFireCount() - vmStartTime) + " ,noi: "
												+ noiTemp);

									}
								});
						this.sumOfProcessedData += this.allocatedData;
					} catch (NetworkException e) {
						e.printStackTrace();
					}
				}
			}
			System.out.println(" load(%): " + this.getLoadOfCloud());
		}

		this.countVmRunningTime();
		this.turnoffVM();

		if ((this.currentTask == 0 && this.incomingData == 0 && this.sumOfProcessedData == this.sumOfArrivedData
				&& this.checkStationState())) {

			unsubscribe();
			for (Provider p : this.providers) {
				if (p.isSubscribed()) {
					p.shouldStop = true;
				}
			}
			StorageObject so = new StorageObject(this.name, this.sumOfProcessedData, false);
			if (!this.computingAppliance.iaas.repositories.get(0).registerObject(so)) {
				this.computingAppliance.iaas.repositories.get(0).deregisterObject(so);
				this.computingAppliance.iaas.repositories.get(0).registerObject(so);
			}

			for (VmCollector vmcl : this.vmlist) {
				try {
					if (vmcl.vm.getState().equals(VirtualMachine.State.RUNNING)) {
						if (vmcl.id.equals("broker")) {
							vmcl.pm = vmcl.vm.getResourceAllocation().getHost();
						}
						vmcl.vm.switchoff(true);

					}
				} catch (StateChangeException e) {
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public String toString() {

		return "fogApp=" + computingAppliance.name + " " + this.computingAppliance.x + " " + this.computingAppliance.y
				+ " stations: " + this.ownStations.size();
	}

}
