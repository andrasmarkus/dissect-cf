package hu.u_szeged.inf.fog.simulator.physical;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.specialized.PhysicalMachineEnergyMeter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.util.CloudLoader;
import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.loaders.ApplianceModel;
import hu.u_szeged.inf.fog.simulator.loaders.ApplicationModel;
import hu.u_szeged.inf.fog.simulator.loaders.NeigbourAppliancesModel;

public class ComputingAppliance {
	
	public static List<ComputingAppliance> allComputingAppliance = new ArrayList<ComputingAppliance>();

	public ComputingAppliance parentNode;

	public IaaSService iaas;
	
	public List<Application> applicationList;
	
	public List<ComputingAppliance> neighbourList; 

	public String name;

	public double x;
	
	public double y;
	
	public double energyConsumption;
	
	
	public ComputingAppliance(String loadfile, String name, double x, double y) throws Exception {
		if (loadfile != null) {
			
			this.iaas = CloudLoader.loadNodes(loadfile);
						
			this.name = name;
			
			this.applicationList = new ArrayList<Application>();
			
			this.neighbourList = new ArrayList<ComputingAppliance>();
			
			this.x = x;
			
			this.y = y;
			
			this.energyConsumption = 0;
			
			ComputingAppliance.allComputingAppliance.add(this);
			
		}else {
			throw new Exception("Description file of the resources is required!");
		}
	}

	
	public void readEnergy(final Application a) {
		for(PhysicalMachine pm: this.iaas.machines) {
			final PhysicalMachineEnergyMeter pmm = new PhysicalMachineEnergyMeter(pm);
			final ArrayList<Long> readingtime = new ArrayList<Long>();
			final ArrayList<Double> readingpm = new ArrayList<Double>();
			class MeteredDataCollector extends Timed {
				public void start() {
					subscribe(a.getFrequency());
				}
				public void stop() {
					unsubscribe();
				}
				@Override
				public void tick(final long fires) {
					readingtime.add(fires);
					readingpm.add(pmm.getTotalConsumption());

					if(Timed.getFireCount()>(24*12*a.getFrequency())) {
						this.stop();
						pmm.stopMeter();
						for(int i=0;i<readingtime.size();i++) {
							energyConsumption+=readingpm.get(i);
						}
					}
				}
			}
			final MeteredDataCollector mdc = new MeteredDataCollector();
			
			pmm.startMeter(a.getFrequency(), true);
			mdc.start();
		}
	}
	
	public void addNeighbour(ComputingAppliance... appliances) {
		for(ComputingAppliance ca : appliances) {
			this.neighbourList.add(ca);
			ca.neighbourList.add(this);
		}
	}
	
	public void addApplication(Application... applications) {
		for(Application app : applications) {
			app.setComputingAppliance(this);
			this.applicationList.add(app);
		}
	}
	
	public void setParentNode(ComputingAppliance ca) {
		this.parentNode=ca;
	}
	
	public void setLatency(ComputingAppliance that, int latency) {
		this.iaas.repositories.get(0).addLatencies(that.iaas.repositories.get(0).getName(), latency);
		that.iaas.repositories.get(0).addLatencies(this.iaas.repositories.get(0).getName(), latency);
	}

	public static void loadAppliance(String appliancefile, Map<String, String> iaasLoader) throws Exception {
		for (ApplianceModel am : ApplianceModel.loadAppliancesXML(appliancefile)) {
			System.out.println(am);
			ComputingAppliance ca = new ComputingAppliance(iaasLoader.get(am.file), am.name, am.xcoord, am.ycoord);			
			for(ApplicationModel a : am.getApplications()){
				ca.addApplication(new Application(a.freq, a.tasksize, a.instance, a.name, a.numOfInstruction, a.threshold, a.strategy, a.canJoin, a.canRead));
			}
		}
		for (ApplianceModel am : ApplianceModel.loadAppliancesXML(appliancefile)) {
				ComputingAppliance ca = getComputingApplianceByName(am.name);
				for(NeigbourAppliancesModel nam : am.getNeighbourAppliances()) {
					ComputingAppliance friend = getComputingApplianceByName(nam.name);
					if(Boolean.parseBoolean(nam.parent)) {
						ca.setParentNode(friend);
						ca.setLatency(friend, nam.latency);
					}else {
						ca.addNeighbour(friend);
						ca.setLatency(friend, nam.latency);
					}
				}
		}
	}
	
	private static ComputingAppliance getComputingApplianceByName(String name) {
		for(ComputingAppliance ca : ComputingAppliance.allComputingAppliance) {
			if(ca.name.equals(name)) {
				return ca;
			}
		}
		return null;
		
	}

	public double getloadOfResource() {
		double usedCPU = 0.0;
		for (VirtualMachine vm : this.iaas.listVMs()) {
			if (vm.getResourceAllocation() != null) {
				usedCPU += vm.getResourceAllocation().allocated.getRequiredCPUs();
			}
		}
		// TODO: why IaaS runningCapacities isn't equals with pm's capacities?
		return (usedCPU / this.iaas.getRunningCapacities().getRequiredCPUs()) * 100;
	}
	
	public double calculateDistance(ComputingAppliance other) {
		double result = Math.sqrt(Math.pow((this.x - other.x), 2) + Math.pow((this.y - other.y), 2));
		return result;
	}

	
}
