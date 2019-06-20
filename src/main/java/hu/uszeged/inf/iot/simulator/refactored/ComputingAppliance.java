package hu.uszeged.inf.iot.simulator.refactored;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.util.CloudLoader;
import hu.uszeged.inf.iot.simulator.refactored.loaders.ApplianceModel;
import hu.uszeged.inf.iot.simulator.refactored.loaders.ApplicationModel;
import hu.uszeged.inf.iot.simulator.refactored.loaders.ComputingDevice;

public class ComputingAppliance {
	
	public Application parentApp;
	
	//we need every device for logging, so we need a list of all ComputingAppliance
	public static List<ComputingAppliance> allComputingDevice = new ArrayList<ComputingAppliance>();
	
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
	
	
	//this method read the appliances its applications and negihbouring devices from xml
	public static void loadAppliances(String appliancefile, String iaasLoader) throws JAXBException, IOException, SAXException, ParserConfigurationException {
		for (ApplianceModel applianceModel : ApplianceModel.loadAppliancesXML(appliancefile)) {
			
			//if there is no appliance with the given name, we create it with all the members initialized
			if (getComputingApplianceByName(applianceModel.name) == null) {
				ComputingAppliance ca = new ComputingAppliance(iaasLoader, applianceModel.name, applianceModel.xcoord, applianceModel.ycoord);
				//populate the applications member from xml
				createApplications(applianceModel, iaasLoader, ca);
				
				//populate the neighbours
				for (ComputingDevice cd : applianceModel.neighbourAppliances) {
					handleNullAppliancesWhenReading(cd.deviceName, ca, iaasLoader);
				}
				
			//if there is already an appliance with the given name we set its members from the xml
			} else {
				
				ComputingAppliance ca = getComputingApplianceByName(applianceModel.name);
				ca.setX(applianceModel.xcoord);
				ca.setY(applianceModel.ycoord);
				
				//populate the applications member from xml
				createApplications(applianceModel, iaasLoader, ca);
				
				//populate the neighbours
				for (ComputingDevice cd : applianceModel.neighbourAppliances) {
					handleNullAppliancesWhenReading(cd.deviceName, ca, iaasLoader);
				}
				
			}
			
		}
	
	}
	
	//this method create the list of applications of an appliance and populate the childdevice member of an application
	public static void createApplications(ApplianceModel applianceModel, String iaasLoader, ComputingAppliance ca) throws IOException, SAXException, ParserConfigurationException {
		for (ApplicationModel am : applianceModel.applications) {
			//type of app is CloudApp
			if (am.type.equals("CloudApp")) {
				CloudApp cloudapp = new CloudApp(am.freq, am.tasksize, am.instance, am.name, am.type, 0, getComputingApplianceByName(am.parentDevice));
				for (ComputingDevice cd : am.childDevice) {
					if (getComputingApplianceByName(cd.deviceName) == null) {
						cloudapp.childComputingDevice.add(new ComputingAppliance(iaasLoader ,cd.deviceName));
					} else {
						cloudapp.childComputingDevice.add(getComputingApplianceByName(cd.deviceName));
					}
					
				}
			
			//type of the app is FogApp
			} else {
				FogApp fogapp = new FogApp(am.freq, am.tasksize, am.instance, am.name, am.type, 0, getComputingApplianceByName(am.parentDevice));
				for (ComputingDevice cd : am.childDevice) {
					if (getComputingApplianceByName(cd.deviceName) == null) {
						fogapp.childComputingDevice.add(new ComputingAppliance(iaasLoader ,cd.deviceName));
					} else {
						fogapp.childComputingDevice.add(getComputingApplianceByName(cd.deviceName));
					}	
				}	
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
	
	
	
	
	public ComputingAppliance(String loadfile, String name, double x, double y) throws IOException, SAXException, ParserConfigurationException {
		if (loadfile != null) {
			//Cloudloader is in charge for create the appropiate machines
			this.iaas = CloudLoader.loadNodes(loadfile);
			this.name = name;
			this.applications = new ArrayList<Application>();
			
			this.x = x;
			this.y = y;
			
			//store all device in a list for logging purpose
			ComputingAppliance.allComputingDevice.add(this);
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
			ComputingAppliance.allComputingDevice.add(this);
		}
	}
	
	
	public static ComputingAppliance getComputingApplianceByName(String name) {
		ComputingAppliance appliance = null;
		for (ComputingAppliance ca : allComputingDevice) {
			if (ca.name.equals(name)) {
				appliance = ca;
			}
		}
		return appliance;
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
