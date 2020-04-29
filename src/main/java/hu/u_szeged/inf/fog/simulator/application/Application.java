package hu.u_szeged.inf.fog.simulator.application;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.ResourceAllocation;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.StateChangeException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;
import hu.u_szeged.inf.fog.simulator.iot.DataCapsule;
import hu.u_szeged.inf.fog.simulator.iot.Device;
import hu.u_szeged.inf.fog.simulator.iot.Station;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;
import hu.u_szeged.inf.fog.simulator.providers.Instance;
import hu.u_szeged.inf.fog.simulator.providers.Provider;
import hu.u_szeged.inf.fog.simulator.task_schedule.TaskScheduler;
import hu.u_szeged.inf.fog.simulator.util.TimelineGenerator.TimelineCollector;

import java.util.*;

public class Application extends Timed {

	public class VmCollector {

		public PhysicalMachine pm;
		public VirtualMachine vm;
		public boolean isWorking;
		public int taskCounter;
		public long lastWorked;
		public long workingTime;
		public String id;
		public long installed;
		public int restarted;

		VmCollector(VirtualMachine vm) {
			this.vm = vm;
			this.isWorking = false;
			this.taskCounter = 0;
			this.workingTime = 0;
			this.lastWorked = Timed.getFireCount();
			this.installed = Timed.getFireCount();
			this.id = Integer.toString(vmManagerlist.size());
			this.restarted = 0;
		}
	}

	public List<Device> deviceList;

	public ArrayList<VmCollector> vmManagerlist;

	public double numberOfInstruction;

	public long taskSize;

	public ComputingAppliance computingAppliance;

	public String name;

	public Instance instance;

	public VmCollector broker;

	public long freq;

	public Application strategyApplication;

	public long sumOfWorkTime;

	public long sumOfProcessedData;

	public long sumOfArrivedData;

	public long allocatedData;

	public int currentTask;

	public long stopTime;

	public int threshold;

	public int incomingData;

	public long allocatedDta;

	public String strategy;

	public ArrayList<Provider> providers;

	public boolean canJoin;

	public ArrayList<TimelineCollector> timelineList = new ArrayList<TimelineCollector>();

	public static ArrayList<Application> allApplication = new ArrayList<Application>();

	public Queue<DataCapsule> forwardDataCapsules;
	public Queue<DataCapsule> backwardDataCapsules;
	public TaskScheduler taskScheduler;


	public Application(long freq, long taskSize, String instance, String name, double numberOfInstruction, int threshold, String strategy, boolean canJoin) {

		forwardDataCapsules = new LinkedList<DataCapsule>();
		backwardDataCapsules = new LinkedList<DataCapsule>();

		Application.allApplication.add(this);

		this.deviceList = new ArrayList<Device>();

		this.vmManagerlist = new ArrayList<VmCollector>();

		providers = new ArrayList<Provider>();

		this.taskSize = taskSize;

		this.threshold = threshold;

		this.numberOfInstruction = numberOfInstruction;

		this.strategy = strategy;

		this.name = name;

		this.freq = freq;

		this.canJoin = canJoin;

		subscribe(this.freq);

		this.instance = Instance.getInstances().get(instance);

		this.sumOfWorkTime = 0;

		this.sumOfProcessedData = 0;

		this.currentTask = 0;

		this.sumOfArrivedData = 0;

		this.incomingData = 0;

	}

	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	public boolean registerDataCapsule(DataCapsule dc) {
		if(taskScheduler != null) {
			taskScheduler.assign(dc);
			sumOfArrivedData+=dc.size;
			return true;
		} else {
			if(getloadOfResource() >= 95) {
				return false;
			}
			this.forwardDataCapsules.add(dc);
			sumOfArrivedData+=dc.size;
			return true;
		}
	}

	public void unsub() {
		unsubscribe();
	}

	public void setComputingAppliance(ComputingAppliance ca) {
		this.computingAppliance = ca;
		this.computingAppliance.iaas.repositories.get(0).registerObject(this.instance.getVa());
		try {
			this.startBroker();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean checkDeviceState() {
		for (Device d : this.deviceList) {
			if (d.isSubscribed()) {
				return false;
			}
		}
		return true;
	}

	public void startBroker() throws VMManagementException, NetworkException {

		if (this.broker != null && this.broker.pm != null && this.vmManagerlist.contains(this.broker)
				&& this.broker.pm.isReHostableRequest(this.instance.getArc())) {
			ResourceAllocation ra = this.broker.pm.allocateResources(this.instance.getArc(), false,
					PhysicalMachine.defaultAllocLen);
			this.broker.restarted++;
			this.broker.vm.switchOn(ra, null);
			this.broker.lastWorked = Timed.getFireCount();
			for (Provider p : this.providers) {
				p.startProvider();
			}
		} else {
			try {
				VirtualMachine vm = this.computingAppliance.iaas.requestVM(this.instance.getVa(),
						this.instance.getArc(), this.computingAppliance.iaas.repositories.get(0), 1)[0];
				if (vm != null) {
					VmCollector vmc = new VmCollector(vm);
					vmc.id = "broker";
					this.vmManagerlist.add(vmc);
					this.broker = vmc;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public VmCollector VmSearch() {
		for (VmCollector aVmManagerlist : this.vmManagerlist) {
			if ((!aVmManagerlist.isWorking
					&& aVmManagerlist.vm.getState().equals(VirtualMachine.State.RUNNING)
					&& !aVmManagerlist.id.equals("broker"))) {
				return aVmManagerlist;

			}
		}
		return null;
	}

	public boolean generateAndAddVM() {

		try {
			if (!this.turnonVM()) {
				for (PhysicalMachine pm : this.computingAppliance.iaas.machines) {
					if (pm.isReHostableRequest(this.instance.getArc())) {
						VirtualMachine vm = pm.requestVM(this.instance.getVa(), this.instance.getArc(),
								this.computingAppliance.iaas.repositories.get(0), 1)[0];
						if (vm != null) {
							VmCollector vmc = new VmCollector(vm);
							vmc.pm = pm;
							this.vmManagerlist.add(vmc);
							System.out.print(" asked new VM");
							return true;
						}

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean turnonVM() {
		for (VmCollector aVmManagerlist : this.vmManagerlist) {
			if (aVmManagerlist.vm.getState().equals(VirtualMachine.State.SHUTDOWN)
					&& aVmManagerlist.pm.isReHostableRequest(this.instance.getArc())) {
				try {
					ResourceAllocation ra = aVmManagerlist.pm.allocateResources(this.instance.getArc(),
							false, PhysicalMachine.defaultAllocLen);
					aVmManagerlist.restarted++;
					aVmManagerlist.vm.switchOn(ra, null);
					aVmManagerlist.lastWorked = Timed.getFireCount();
					System.out.print(" turned on VM");
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public void turnoffVM() {
		for (VmCollector vmcl : this.vmManagerlist) {
			if (vmcl.vm.getState().equals(VirtualMachine.State.RUNNING) && !vmcl.id.equals("broker")
					&& !vmcl.isWorking) {
				try {
					vmcl.vm.switchoff(false);
				} catch (StateChangeException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public double getloadOfResource() {
		double usedCPU = 0.0;
		for (VirtualMachine vm : this.computingAppliance.iaas.listVMs()) {
			if (vm.getResourceAllocation() != null) {
				usedCPU += vm.getResourceAllocation().allocated.getRequiredCPUs();
			}
		}
		// TODO: why IaaS runningCapacities isn't equals with pm's capacities?
		return (usedCPU / this.computingAppliance.iaas.getRunningCapacities().getRequiredCPUs()) * 100;
	}

	//Moving a given amount of datacapsules from one application to an other

	public static void moveDataCapsule(Application source, Application destination, long dataSize, int direction) {
		long counter = 0;
		DataCapsule toSend;
		while (counter < dataSize) {
			if(direction != -1) {
				toSend = source.forwardDataCapsules.poll();
			} else {
				toSend = source.backwardDataCapsules.poll();
			}
			if (toSend != null) {
				toSend.setDestination(destination);
				if (direction != -1) {
					toSend.addToDataPath(destination);
					counter += toSend.size;
					destination.forwardDataCapsules.add(toSend);
				} else {
					destination.backwardDataCapsules.add(toSend);
					counter += toSend.getEventSize();
				}
			} else {
				break;
			}
		}
	}

	public double getCurrentCost() {
		return this.instance.calculateCloudCost(this.sumOfWorkTime);
	}

	public void countVmRunningTime() {
		for (VmCollector vmc : this.vmManagerlist) {
			if (vmc.vm.getState().equals(VirtualMachine.State.RUNNING)) {
				vmc.workingTime += (Timed.getFireCount() - vmc.lastWorked);
				sumOfWorkTime += (Timed.getFireCount() - vmc.lastWorked);
				vmc.lastWorked = Timed.getFireCount();
			}
		}
	}

	public void restartApplication() throws VMManagementException, NetworkException {
		System.out.println("\n" + this.name + " application has been restarted at " + Timed.getFireCount());
		subscribe(this.freq);
		this.startBroker();
	}

	public void tick(long fires) {

//		if(taskScheduler != null) {
//			taskScheduler.process();
//			return;
//		}


		long unprocessedData = (this.sumOfArrivedData - this.sumOfProcessedData);
		if (unprocessedData > 0) {

			long alreadyProcessedData = 0;

			//As long as we have unprocessed data
			while (unprocessedData != alreadyProcessedData) {
				if (unprocessedData - alreadyProcessedData > this.taskSize) {
					allocatedData = this.taskSize;
				} else {
					allocatedData = (unprocessedData - alreadyProcessedData);
				}

				final VmCollector vml = this.VmSearch();
				//Is there a virtual machine that can process the data?
				if (vml == null) {

					double ratio = (int) ((double) unprocessedData / this.taskSize);

					//Send it over
					if (ratio > this.threshold) {
						strategy(unprocessedData - alreadyProcessedData);
					}

					System.out
							.print("data/VM: " + ratio + " unprocessed after exit: " + unprocessedData + " decision:");
					this.generateAndAddVM();

					break;
				}

				//We can process the data
				try {
					final double noi = this.allocatedData == this.taskSize ? this.numberOfInstruction
							: (this.numberOfInstruction * this.allocatedData / this.taskSize);

					alreadyProcessedData += this.allocatedData;
					vml.isWorking = true;
					this.currentTask++;

					vml.vm.newComputeTask(noi, ResourceConsumption.unlimitedProcessing, new ConsumptionEventAdapter() {
						long vmStartTime = Timed.getFireCount();
						long allocatedDataTemp = allocatedData;
						double noiTemp = noi;

						@Override
						public void conComplete() {
							vml.isWorking = false;
							vml.taskCounter++;
							currentTask--;

							stopTime = Timed.getFireCount();
							timelineList.add(new TimelineCollector(vmStartTime, Timed.getFireCount(), vml.id));
							System.out.println(name + " " + vml.id + " started@ " + vmStartTime + " finished@ "
									+ Timed.getFireCount() + " with " + allocatedDataTemp + " bytes, lasted "
									+ (Timed.getFireCount() - vmStartTime) + " ,noi: " + noiTemp);

						}
					});
					this.sumOfProcessedData += this.allocatedData;
					try {
						transferBackToStation(allocatedData);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (NetworkException e) {
					e.printStackTrace();
				}

			}
			System.out.println(" load(%): " + this.getloadOfResource());
		}
		this.countVmRunningTime();
		this.turnoffVM();
		if (this.currentTask == 0 && this.incomingData == 0 && this.sumOfProcessedData == this.sumOfArrivedData
				&& this.checkDeviceState()) {
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

			for (VmCollector vmcl : this.vmManagerlist) {
				try {
					if (vmcl.vm.getState().equals(VirtualMachine.State.RUNNING)) {
						if (vmcl.id.equals("broker")) {
							vmcl.pm = vmcl.vm.getResourceAllocation().getHost();
						}
						vmcl.vm.switchoff(false);
					}
				} catch (StateChangeException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void strategy(long unprocessedData) {

		if (this.strategy.equals("random")) {
			new RandomApplicationStrategy(this);
		} else {
			try {
				throw new Exception("This application strategy does not exist!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(this.strategyApplication!=null) {
			this.transferToApplication(unprocessedData);
		}
	}

	public void transferToApplication(long unprocessedData) {
		if(this.strategyApplication!=null) {
			this.strategyApplication.incomingData++;
			this.sumOfArrivedData -= unprocessedData;
			if (this.strategyApplication.isSubscribed()) {
				final long unprocessed = unprocessedData;
				try {
					final Application source = this;
					final Application destination = this.strategyApplication;
					NetworkNode.initTransfer(unprocessedData, ResourceConsumption.unlimitedProcessing,
							this.computingAppliance.iaas.repositories.get(0), this.strategyApplication.computingAppliance.iaas.repositories.get(0),
							new ConsumptionEvent() {

								@Override
								public void conComplete() {
									strategyApplication.sumOfArrivedData += unprocessed;
									strategyApplication.incomingData--;
									moveDataCapsule(source, destination, unprocessed, 1);
								}

								@Override
								public void conCancelled(ResourceConsumption problematic) {

								}
							});
				} catch (NetworkException e) {
					e.printStackTrace();
				}

			} else {
				try {
					this.strategyApplication.restartApplication();
				} catch (Exception e) {
					e.printStackTrace();
				}
				new BrokerCheck(this, this.strategyApplication, unprocessedData, (this.strategyApplication.freq / 2));
			}
		}


	}

	private void transferBackToStation(long allocatedData) throws Exception {
		copyForwardToBackward(allocatedData);
		long sent = 0;
		Application currentApp = this;
		while (sent < allocatedData) {
			DataCapsule toSend = backwardDataCapsules.poll();
			if (toSend != null) {
				if(toSend.isActuationNeeded()) {
					toSend.setProcessTime(Timed.getFireCount());
					toSend.setActuatorEvent(toSend.getSource().getActuator().selectStrategyEvent());
					while (!toSend.getDataFlowPath().isEmpty()) {
						Application nextApp = toSend.getDataFlowPath().pop();
						if (nextApp != currentApp) {
							if (nextApp.isSubscribed()) {
								try {
									long toProcess = toSend.getEventSize();
									initiateDataTransfer(toProcess, currentApp, nextApp, -1);
									currentApp = nextApp;
								} catch (Exception e) {
									e.printStackTrace();
								}
							} else {
								try {
									nextApp.restartApplication();
								} catch (Exception e) {
									e.printStackTrace();
								}
								new BrokerCheck(currentApp, nextApp, toSend.getEventSize(), (nextApp.freq / 2));
							}

						}
					}

					if (currentApp != toSend.getSource().getApp()) {
						throw new Exception("Station cannot be reached!");
					} else {

						NetworkNode.initTransfer(toSend.getEventSize(), ResourceConsumption.unlimitedProcessing,
								currentApp.computingAppliance.iaas.repositories.get(0), toSend.getSource().getDn().localRepository,
								new Station.ActualizationEvent(toSend.getSource(), toSend));
						currentApp.backwardDataCapsules.remove(toSend);

					}

					sent += toSend.size;
				}

			} else {
				break;
			}

		}

	}

	private void copyForwardToBackward(long allocatedData) {
		long tmp=0;
		for(DataCapsule dc : forwardDataCapsules) {
			if(!backwardDataCapsules.contains(dc)) {
				backwardDataCapsules.add(dc);
				tmp += dc.size;
			}
			if(tmp >= allocatedData) {
				break;
			}
		}
		forwardDataCapsules.removeAll(backwardDataCapsules);
	}

	public void initiateDataTransfer(final long dataSize, final Application source, final Application destination, final int direction) throws NetworkException {
		NetworkNode.initTransfer(dataSize, ResourceConsumption.unlimitedProcessing,
				source.computingAppliance.iaas.repositories.get(0), destination.computingAppliance.iaas.repositories.get(0),
				new ConsumptionEvent() {

					@Override
					public void conComplete() {
						if(taskScheduler != null) {
							taskScheduler.moveDataCapsule(dataSize, source, destination, direction);
						} else {
							moveDataCapsule(source, destination, dataSize, direction);
						}
					}

					@Override
					public void conCancelled(ResourceConsumption problematic) {

					}
				});
	}

}