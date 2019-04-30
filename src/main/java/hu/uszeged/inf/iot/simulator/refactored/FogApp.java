package hu.uszeged.inf.iot.simulator.refactored;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.StateChangeException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;
import hu.uszeged.inf.iot.simulator.providers.Provider;
import hu.uszeged.inf.iot.simulator.util.TimelineGenerator.TimelineCollector;
import hu.uszeged.inf.xml.model.ApplicationModel;

public class FogApp extends Application {
	
	//Station connect only to FogDevice(FogApp)
	
	public FogAppliance fogDevice;
	public List<Device> ownStations = new ArrayList<Device>(); 

	public FogApp(long freq, long tasksize, String cloud, String instance, String name, String type, double noi , ComputingAppliance computingAppliance) {
		super(freq, tasksize, cloud, instance, name, type, noi, computingAppliance);
		this.fogDevice = (FogAppliance) super.computingDevice;
		
		//the "gateWay" devices(this FogApp which is in the Devcie) store a list of station reference
		
		
		//need to add fogApps to a list for installation strategy
		Application.fogApplications.add(this);
		
	}

	//TODO kipr�b�lni
	@Override
	public void loadApplication(String appfile) throws JAXBException {
		for (ApplicationModel am : ApplicationModel.loadApplicationXML(appfile)) {
			new FogApp(am.freq, am.tasksize, am.cloud, am.instance, am.name,"fog",0, null);
		}
	}
	
	public ComputingAppliance getParentDeviceOfApp() {
		return fogDevice.parentApp.computingDevice;
	}
	
	//add this app to a specific station => InstallationStrategy
	
	

	@Override
	public void tick(long fires) {
		long unprocessedData = (this.sumOfArrivedData - this.sumOfProcessedData);

		if (unprocessedData > 0) {
			System.out.print(Timed.getFireCount()+" unprocessed data: "+unprocessedData+ " "+this.name+" ");
			long processedData = 0;

			while (unprocessedData != processedData) { 
				if (unprocessedData - processedData > this.tasksize) {
					this.allocatedData = this.tasksize; 
				} else {
					this.allocatedData = (unprocessedData - processedData);
				}
				final VmCollector vml = this.VmSearch();
				if (vml == null) {
					double ratio = ((double)unprocessedData/this.tasksize);
										
					if(ratio>2) {
						
						if(this.getParentDeviceOfApp()!=null) {
							try {
								if(this.fogDevice.parentApp.isSubscribed()) {
									this.sumOfArrivedData-=unprocessedData;					
									final long unprocessed = unprocessedData;
									NetworkNode.initTransfer(unprocessedData, ResourceConsumption.unlimitedProcessing, 
											this.fogDevice.iaas.repositories.get(0), this.getParentDeviceOfApp().iaas.repositories.get(0), new ConsumptionEvent() {

												@Override
												public void conComplete() {
													fogDevice.parentApp.sumOfArrivedData +=  unprocessed;
												}

												@Override
												public void conCancelled(ResourceConsumption problematic) {
													
												}
										
									});
								} else {
									try {
										this.fogDevice.parentApp.restartApplication();
									} catch (VMManagementException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								
							} catch (NetworkException e) {
								e.printStackTrace();
							}
						}
						
						
						//Application nearestApp = getNearestComputingAppliance(this);
						
						
						
						
						
					}
					System.out.print("data/VM: "+ratio+" unprocessed after exit: "+unprocessedData+ " decision:");
					this.generateAndAddVM();
					
					break;
					
					
					
				} else {
					try {
						final double noi = this.allocatedData == this.tasksize ? defaultNoi : (double) (2400 * this.allocatedData / this.tasksize);
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
										stopTime=Timed.getFireCount();
										timelineList.add(new TimelineCollector(vmStartTime,Timed.getFireCount(),vml.id));
											System.out.println(name +" "+vml.id + " started@ " + vmStartTime + " finished@ "
													+ Timed.getFireCount() + " with " + allocatedDataTemp + " bytes, lasted "
													+ (Timed.getFireCount() - vmStartTime) + " ,noi: " + noiTemp);

									}
								});
						this.sumOfProcessedData += this.allocatedData; 
					} catch (NetworkException e) {
						e.printStackTrace();
					}
				}
			}
			System.out.println(" load(%): "+this.getLoadOfCloud());
		}
		this.countVmRunningTime();
		this.turnoffVM();

		if (this.currentTask == 0) {
			unsubscribe();
			for(Provider p : this.providers) {
				if(p.isSubscribed()) {
					p.shouldStop=true;
				}
			}
			StorageObject so = new StorageObject(this.name, this.sumOfProcessedData, false);
			if(!this.fogDevice.iaas.repositories.get(0).registerObject(so)){
				this.fogDevice.iaas.repositories.get(0).deregisterObject(so);
				this.fogDevice.iaas.repositories.get(0).registerObject(so);
			}
			
			for (VmCollector vmcl : this.vmlist) {
				try {
					if (vmcl.vm.getState().equals(VirtualMachine.State.RUNNING)) {
						if(vmcl.id.equals("broker")) {
							vmcl.pm=vmcl.vm.getResourceAllocation().getHost();
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
		
		return "fogDevice=" + fogDevice.name + " " + this.computingDevice.x + " " +  this.computingDevice.y +  " stations: " + this.ownStations.size();
	}
	
}
