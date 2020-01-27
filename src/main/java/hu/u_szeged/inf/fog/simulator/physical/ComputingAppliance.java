package hu.u_szeged.inf.fog.simulator.physical;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.util.CloudLoader;
import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.application.CloudApp;
import hu.u_szeged.inf.fog.simulator.application.FogApp;
import hu.u_szeged.inf.fog.simulator.loaders.ApplianceModel;
import hu.u_szeged.inf.fog.simulator.loaders.ApplicationModel;
import hu.u_szeged.inf.fog.simulator.loaders.ComputingDevice;

public class ComputingAppliance {
	
	public Application parentApp;
	
	//we need every device for logging, so we need a list of all ComputingAppliance
	public static List<ComputingAppliance> allComputingAppliance = new ArrayList<ComputingAppliance>();
	
	//Every device have computing capacity
	public IaaSService iaas;
	//A device holds a list with a reference to its applications
	public List<Application> applications;
	
	//we store nearby devices in the neighbours list so we the appliance can communicate with them 
	public List<ComputingAppliance> neighbours = new ArrayList<ComputingAppliance>();
	
	//A unique identifier
	public String name;
	
	//we have to store x and y coord for geolocation
	public double x;
	public double y;
	
	public static String getIaasLoader(Map<String, String> iaasLoaders, ApplianceModel applianceModel) {
		String file = iaasLoaders.get(applianceModel.file);
		if (file == null) {
			file = iaasLoaders.get("cloud");
		}
		return file;
	}
	
	//this method read the appliances its applications and negihbouring devices from xml
	public static void loadAppliances(String appliancefile, Map<String, String> iaasLoaders) throws JAXBException, IOException, SAXException, ParserConfigurationException {
		
	
		for (ApplianceModel applianceModel : ApplianceModel.loadAppliancesXML(appliancefile)) {
			
			//if there is no appliance with the given name, we create it with all the members initialized
			if (getComputingApplianceByName(applianceModel.name) == null) {
				ComputingAppliance ca = new ComputingAppliance(getIaasLoader(iaasLoaders, applianceModel), applianceModel.name, applianceModel.xcoord, applianceModel.ycoord, Application.getApplicationsByName(applianceModel.parentApp));
				//populate the applications member from xml
				createApplications(applianceModel, getIaasLoader(iaasLoaders, applianceModel), ca);
				
				//populate the neighbours
				for (ComputingDevice cd : applianceModel.neighbourAppliances) {
					handleNullAppliancesWhenReading(cd.deviceName, ca, getIaasLoader(iaasLoaders, applianceModel));
				}
				
			//if there is already an appliance with the given name we set its members from the xml
			} else {
				
				ComputingAppliance ca = getComputingApplianceByName(applianceModel.name);
				ca.setX(applianceModel.xcoord);
				ca.setY(applianceModel.ycoord);
				ca.setParentApp(Application.getApplicationsByName(applianceModel.parentApp));
				
				//populate the applications member from xml
				createApplications(applianceModel, getIaasLoader(iaasLoaders, applianceModel), ca);
				
				//populate the neighbours
				for (ComputingDevice cd : applianceModel.neighbourAppliances) {
					handleNullAppliancesWhenReading(cd.deviceName, ca, getIaasLoader(iaasLoaders, applianceModel));
				}
				
			}
			
		}
	
	}
	
	//this method create the list of applications of an appliance and populate the childdevice member of an application
	public static void createApplications(ApplianceModel applianceModel, String iaasLoader, ComputingAppliance ca) throws IOException, SAXException, ParserConfigurationException {
		for (ApplicationModel am : applianceModel.applications) {
			if (am.type.equals("cloud")) {
				new CloudApp(am.freq, am.tasksize, am.instance, am.name, 0, getComputingApplianceByName(am.parentDevice));				
			} else {
				new FogApp(am.freq, am.tasksize, am.instance, am.name, 0, getComputingApplianceByName(am.parentDevice));
			}
		}
	}
	
	//creating the neighbours
	//if the neighbour don't exist yet, then we create it
	//else we can use the getComputingApplianceByName method to get the given appliance
	public static void handleNullAppliancesWhenReading(String applianceName, ComputingAppliance ca, String iaasfile) throws IOException, SAXException, ParserConfigurationException {
		if (getComputingApplianceByName(applianceName) == null ) {
			ca.neighbours.add(new ComputingAppliance(iaasfile, applianceName));
		} else {
			ca.neighbours.add(getComputingApplianceByName(applianceName));
		}
	}
	
	
	public ComputingAppliance(String loadfile, String name, double x, double y, Application parentApp) throws IOException, SAXException, ParserConfigurationException {
		if (loadfile != null) {
			//Cloudloader is in charge for create the appropiate machines
			this.iaas = CloudLoader.loadNodes(loadfile);
			
			
			this.name = name;
			this.applications = new ArrayList<Application>();
			
			this.x = x;
			this.y = y;
			this.parentApp = parentApp;
			
			//store all device in a list for logging purpose
			ComputingAppliance.allComputingAppliance.add(this);
		}
	}
	
	//we have to create an appliance with only a name for xml reading
	public ComputingAppliance(String loadfile, String name) throws IOException, SAXException, ParserConfigurationException {
		if (loadfile != null) {
			this.name = name;
			this.iaas = CloudLoader.loadNodes(loadfile);
			this.x = 0;
			this.y = 0;
			this.applications = new ArrayList<Application>();
			ComputingAppliance.allComputingAppliance.add(this);
		}
	}
	
	
	public static ComputingAppliance getComputingApplianceByName(String name) {
		ComputingAppliance appliance = null;
		for (ComputingAppliance ca : allComputingAppliance) {
			if (ca.name.equals(name)) {
				appliance = ca;
			}
		}
		return appliance;
	}
	
	public Application getLeastLoadedApplication() {
		double load = Double.MAX_VALUE;
		Application app = null;
		for (Application application : this.applications) {
			if (application.dataLoad < load /*&& application.isSubscribed()*/ ) {
				load = application.dataLoad;
				app = application;
			}
		}
		return app;
		//return this.applications.get(0);
	}
	
	public List<ComputingAppliance> getNeighboursWithSufficientLoad(double treshHold){
		List<ComputingAppliance> sufficientAppliances = new ArrayList<ComputingAppliance>();
		for (ComputingAppliance computingAppliance : neighbours) {
			if (computingAppliance.getLeastLoadedApplication().dataLoad < treshHold) {
				sufficientAppliances.add(computingAppliance);
			}
		}
		return sufficientAppliances;
	}
	
	public ComputingAppliance getARandomNeighbourApplianceFromSufficientAppliances(List<ComputingAppliance> neighbourComputingAppliances) {
		if (neighbourComputingAppliances.size() == 0) {
			return null;
		} else {
			Random ran = new Random();
			int indexOfRandomAppliacation = ran.nextInt(neighbourComputingAppliances.size());
			return neighbourComputingAppliances.get(indexOfRandomAppliacation);
		}
	}
	

	
	
	public void addParentApp(Application app) {
		this.parentApp = app;
	}
	
	
	public double getX() {
		return x;
	}
	
	public void setX(double x) {
		this.x  = x;
	}
	
	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
	
	public void setApplications(List<Application> apps){
		this.applications = apps;
	}
	
	public Application getParentApp() {
		return parentApp;
	}

	public void setParentApp(Application parentApp) {
		this.parentApp = parentApp;
	}

	@Override
	public String toString() {
		String apps = "";
		for (Application app : applications) {
			apps += app.name + " ";
		}
		return "ComputingAppliance name: " + name + " xCoord: " + x + " yCoord: " + y + 
				" applications: " + apps;
	}
	
	
	
	
}
