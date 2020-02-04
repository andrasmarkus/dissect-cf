package hu.u_szeged.inf.fog.simulator.application;

import java.util.ArrayList;
import java.util.List;
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
import hu.u_szeged.inf.fog.simulator.iot.Actuator;
import hu.u_szeged.inf.fog.simulator.iot.ActuatorRandomStrategy;
import hu.u_szeged.inf.fog.simulator.iot.DataCapsule;
import hu.u_szeged.inf.fog.simulator.iot.Device;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;
import hu.u_szeged.inf.fog.simulator.providers.Instance;
import hu.u_szeged.inf.fog.simulator.providers.Provider;
import hu.u_szeged.inf.fog.simulator.util.TimelineGenerator.TimelineCollector;

public class Application extends Timed {

	public class VmCollector {

		PhysicalMachine pm;
		public VirtualMachine vm;
		boolean isWorking;
		public int taskCounter;
		long lastWorked;
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

	protected VmCollector broker;

	protected long freq;
	
	public Application strategyApplication;

	public long sumOfWorkTime;

	public long sumOfProcessedData;

	public long sumOfArrivedData;

	protected long allocatedData;

	protected int currentTask;
	
	public long stopTime;
	
	public int threshold;
	
	public int incomingData;

    private DataCapsule dataCapsule;
	
	public long allocatedDta;
	
	public String strategy;
	
	public ArrayList<Provider> providers;
	
	public boolean canJoin;
	
	public ArrayList<TimelineCollector> timelineList = new ArrayList<TimelineCollector>();
	
	public static ArrayList<Application> allApplication = new ArrayList<Application>();
	
	
	
	public Application(long freq, long taskSize, String instance, String name, double numberOfInstruction, int threshold, String strategy, boolean canJoin) {

	    this.dataCapsule = null;

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

	public void setComputingAppliance(ComputingAppliance ca) {
		this.computingAppliance = ca;
		this.computingAppliance.iaas.repositories.get(0).registerObject(this.instance.getVa());
		try {
			this.startBroker();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected boolean checkDeviceState() {
		for (Device d : this.deviceList) {
			if (d.isSubscribed()) {
				return false;
			}
		}
		return true;
	}

	private void startBroker() throws VMManagementException, NetworkException {

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

	protected VmCollector VmSearch() {
        for (VmCollector aVmManagerlist : this.vmManagerlist) {
            if ((!aVmManagerlist.isWorking
                    && aVmManagerlist.vm.getState().equals(VirtualMachine.State.RUNNING)
                    && !aVmManagerlist.id.equals("broker"))) {
                return aVmManagerlist;

            }
        }
		return null;
	}

	protected boolean generateAndAddVM() {

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

	protected boolean turnonVM() {
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

	protected void turnoffVM() {
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

    public void moveDataCapsule(Application application) {
        if (dataCapsule != null) {
            dataCapsule.setDestination(application);
            application.setDataCapsule(dataCapsule);
            dataCapsule.addToDataPath(application);
            this.dataCapsule = null;
        }
    }

	public double getCurrentCost() {
		return this.instance.calculateCloudCost(this.sumOfWorkTime);
	}

	protected void countVmRunningTime() {
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

	public double calculateDistance(ComputingAppliance one, ComputingAppliance other) {
		return Math.sqrt(Math.pow((one.x - other.x), 2) + Math.pow((one.y - other.y), 2));
	}

    public DataCapsule getDataCapsule() {
        return this.dataCapsule;
    }

    public void setDataCapsule(DataCapsule dataCapsule) {
        this.dataCapsule = dataCapsule;
    }

	public void tick(long fires) {
		long unprocessedData = (this.sumOfArrivedData - this.sumOfProcessedData);

		if (unprocessedData > 0) {

			long alreadyProcessedData = 0;

			while (unprocessedData != alreadyProcessedData) {

				if (unprocessedData - alreadyProcessedData > this.taskSize) {
					allocatedData = this.taskSize;
				} else {
					allocatedData = (unprocessedData - alreadyProcessedData);
				}

				final VmCollector vml = this.VmSearch();

				if (vml == null) {

					double ratio = (int) ((double) unprocessedData / this.taskSize);

					
					if (ratio > this.threshold) {
						strategy(unprocessedData - alreadyProcessedData);
						
					}

					System.out
							.print("data/VM: " + ratio + " unprocessed after exit: " + unprocessedData + " decision:");
					this.generateAndAddVM();

					break;
				}

				try {
					final double noi = this.allocatedData == this.taskSize ? this.numberOfInstruction
							: (double) (this.numberOfInstruction * this.allocatedData / this.taskSize);

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
                            if(dataCapsule != null) {
                                if (dataCapsule.getSource() != null && dataCapsule.getSource().isSubscribed()) {
                                    //Different strategies could be calculated before
                                    new Actuator(new ActuatorRandomStrategy(), dataCapsule.getSource(), 1);
                                }
                            }
							timelineList.add(new TimelineCollector(vmStartTime, Timed.getFireCount(), vml.id));
							System.out.println(name + " " + vml.id + " started@ " + vmStartTime + " finished@ "
									+ Timed.getFireCount() + " with " + allocatedDataTemp + " bytes, lasted "
									+ (Timed.getFireCount() - vmStartTime) + " ,noi: " + noiTemp);

						}
					});
					this.sumOfProcessedData += this.allocatedData;
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

	private void strategy(long unprocessedData) {

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
					NetworkNode.initTransfer(unprocessedData, ResourceConsumption.unlimitedProcessing,
							this.computingAppliance.iaas.repositories.get(0), this.strategyApplication.computingAppliance.iaas.repositories.get(0),
							new ConsumptionEvent() {

								@Override
								public void conComplete() {
									strategyApplication.sumOfArrivedData += unprocessed;
									strategyApplication.incomingData--;
									moveDataCapsule(strategyApplication);
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
}