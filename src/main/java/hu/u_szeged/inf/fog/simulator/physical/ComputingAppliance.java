package hu.u_szeged.inf.fog.simulator.physical;

import java.util.ArrayList;
import java.util.List;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.util.CloudLoader;
import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.iot.mobility.GeoLocation;

public class ComputingAppliance {
	
	public static List<ComputingAppliance> allComputingAppliance = new ArrayList<ComputingAppliance>();

	public ComputingAppliance parentNode;

	public IaaSService iaas;
	
	public List<Application> applicationList;
	
	public List<ComputingAppliance> neighbourList; 

	public String name;

	public GeoLocation location;

	public double range; //in m

	public ComputingAppliance(String loadfile, String name, GeoLocation location, double range) throws Exception {
		if (loadfile != null) {
			
			this.iaas = CloudLoader.loadNodes(loadfile);
						
			this.name = name;
			
			this.applicationList = new ArrayList<Application>();
			
			this.neighbourList = new ArrayList<ComputingAppliance>();
			
			this.location = location;

			this.range = range;

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


	
}
