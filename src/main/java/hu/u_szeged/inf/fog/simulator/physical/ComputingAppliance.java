package hu.u_szeged.inf.fog.simulator.physical;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
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
	
	
	public ComputingAppliance(String loadfile, String name, double x, double y) throws Exception {
		if (loadfile != null) {
			
			this.iaas = CloudLoader.loadNodes(loadfile);
						
			this.name = name;
			
			this.applicationList = new ArrayList<Application>();
			
			this.neighbourList = new ArrayList<ComputingAppliance>();
			
			this.x = x;
			
			this.y = y;
			
			ComputingAppliance.allComputingAppliance.add(this);
			
		}else {
			throw new Exception("Description file of the resources is required!");
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
			System.out.println("? "+am);
			ComputingAppliance ca = new ComputingAppliance(iaasLoader.get(am.file), am.name, am.xcoord, am.ycoord);			
			for(ApplicationModel a : am.getApplications()){
				ca.addApplication(new Application(a.freq, a.tasksize, a.instance, a.name, a.numOfInstruction, a.threshold, a.strategy, a.canJoin));
			}
		}
		for (ApplianceModel am : ApplianceModel.loadAppliancesXML(appliancefile)) {
			
				ComputingAppliance ca = getComputingApplianceByName(am.name);
				System.out.println("! "+ca.name);
				System.out.println("+ "+ am.neighbours);
				for(NeigbourAppliancesModel nam : am.neighbours) {
					System.out.println(nam);
					ComputingAppliance friend = getComputingApplianceByName(nam.name);
					System.out.println("- " +friend.name);
					if(Boolean.parseBoolean(nam.parent) && nam.parent!=null) {
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
