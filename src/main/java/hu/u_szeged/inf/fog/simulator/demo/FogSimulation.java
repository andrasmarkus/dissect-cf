package hu.u_szeged.inf.fog.simulator.demo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.mta.sztaki.lpds.cloud.simulator.util.PowerTransitionGenerator;
import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.demo.ScenarioBase;
import hu.u_szeged.inf.fog.simulator.iot.Device.DeviceNetwork;
import hu.u_szeged.inf.fog.simulator.iot.Station;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;
import hu.u_szeged.inf.fog.simulator.physical.Microcontroller;
import hu.u_szeged.inf.fog.simulator.providers.AmazonProvider;
import hu.u_szeged.inf.fog.simulator.providers.AzureProvider;
import hu.u_szeged.inf.fog.simulator.providers.BluemixProvider;
import hu.u_szeged.inf.fog.simulator.providers.BluemixProvider.Bluemix;
import hu.u_szeged.inf.fog.simulator.providers.Instance;
import hu.u_szeged.inf.fog.simulator.providers.OracleProvider;
import hu.u_szeged.inf.fog.simulator.util.FogSimulationChart;
import hu.u_szeged.inf.fog.simulator.util.MicrocontrollerConsumptionChartGenerator;
import hu.u_szeged.inf.fog.simulator.util.MicrocontrollerPowerTransitionGenerator;
import hu.u_szeged.inf.fog.simulator.util.TimelineGenerator;

/**
 * This class presents an Fog simulation dealing with 2 clouds, 4 fog nodes and 1000 smart devices and we also calculate the cost of the applications
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
public class FogSimulation {
	
	public static void main(String[] args) throws Exception {
	
	// creating vm images and its resource needs
	
	// for a VM, first we need a virtual machine image, it needs 100 instruction for creating a VM from it, and it needs 1 GB of free space on a PM
	VirtualAppliance va = new VirtualAppliance("va", 100, 0, false, 1073741824L);
	
	// we have to define the resource needs of the VM, we use 4-8 CPU cores, 0.001 core processing power and 4 GB RAM
	AlterableResourceConstraints arc1 = new AlterableResourceConstraints(8,0.001,4294967296L);
	AlterableResourceConstraints arc2 = new AlterableResourceConstraints(4,0.001,4294967296L);
	
	// and now we make join them into 1 object called Instance with different hourly price (~0.036$)
	new Instance(va,arc1,0.00000001,"instance1");
	new Instance(va,arc2,0.000000015,"instance2");
	
	String cloudfile = ScenarioBase.resourcePath+"LPDS_original.xml";	
	String fogfile = ScenarioBase.resourcePath+"LPDS_Fog_T1.xml";
	
	// we create our clouds using predefined cloud schema
	ComputingAppliance cloud1 = new ComputingAppliance(cloudfile, "cloud1",-4,5);
	ComputingAppliance cloud2 = new ComputingAppliance(cloudfile, "cloud2",4,5);
	
	// creating the cloud application modules: 5 minutes frequency, 175kB task size and max. 2400 instruction / task
	Application ca1 = new Application(5*60*1000, 256000, "instance1", "Cloud-app1", 2400.0, 1, "random", false, true);
	Application ca2 = new Application(5*60*1000, 256000, "instance1", "Cloud-app2", 2400.0, 1, "random", false, true);

	// we create our fog nodes using predefined fog schema
	ComputingAppliance fog1 = new ComputingAppliance(fogfile, "fog1",-6,0);
	ComputingAppliance fog2 = new ComputingAppliance(fogfile, "fog2",-2,0);
	ComputingAppliance fog3 = new ComputingAppliance(fogfile, "fog3",2,0);
	ComputingAppliance fog4 = new ComputingAppliance(fogfile, "fog4",6,0);

	System.out.println(fog1.iaas.repositories.get(0).getLatencies());
	System.out.println(cloud1.iaas.repositories.get(0).getLatencies());
	
	fog1.setLatency(cloud1, 20);
	
	System.out.println(fog1.iaas.repositories.get(0).getLatencies());
	System.out.println(cloud1.iaas.repositories.get(0).getLatencies());
	
	fog1.setParentNode(cloud1);
	fog2.addNeighbour(fog3);
	
	// creating the fog application modules: 5 minutes frequency, 175kB task size and max. 2400 instruction / task
	Application fa1 = new Application(5*60*1000, 179200, "instance2", "Fog-app3", 2400.0, 1, "random", true, true);
	Application fa2 = new Application(5*60*1000, 179200, "instance2", "Fog-app4", 2400.0, 1, "random", true, true);
	Application fa3 = new Application(5*60*1000, 179200, "instance2", "Fog-app5", 2400.0, 1, "random", true, true);
	Application fa4 = new Application(5*60*1000, 179200, "instance2", "Fog-app6", 2400.0, 1, "random", true, true);
	
	cloud1.addApplication(ca1);
	cloud2.addApplication(ca2);
	fog1.addApplication(fa1);
	fog2.addApplication(fa2);
	fog3.addApplication(fa3);
	fog4.addApplication(fa4);
	
	// we create 1000 smart device with random installation strategy, 10kB storage, 10000 bandwidth, 
	// 24 hours long running time, 50 bytes of generated data by each sensor, each smart device has 5 sensor,
	// and the frequency is 1 minute, last 3 zero parameters are for the geolocation, but it is now irrelevant for us
	for(int i=0;i<10000;i++) {
		int x,y;
		Random randomGenerator = new Random();
		x = randomGenerator.nextInt(21)-10;
		y = randomGenerator.nextInt(9)-10;
		
		HashMap<String, Integer> latencyMap = new HashMap<String, Integer>();
		
		final long disksize = 100001;
		
		//ESP32 (0.025, 0.155, 0.2)
		final EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> transitions = 
					MicrocontrollerPowerTransitionGenerator.generateTransitions(0.065, 1.475, 2.0, 0, 0);
		//final EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> transitions = 
					//MicrocontrollerPowerTransitionGenerator.generateTransitions(0.5, 1.45,1.7, 0, 0);
		final Map<String, PowerState> cpuTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.host);
		final Map<String, PowerState> stTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.storage);
		final Map<String, PowerState> nwTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.network);
		
		final Microcontroller mc;
		mc = new Microcontroller(1, 1, 1000, new Repository(disksize, "mc", 100, 100, 100, latencyMap, stTransitions, nwTransitions), 1, 1, cpuTransitions);
		
		// 20 perc �zemid� - 1 perces frekvencia - 10 mp szenzorfrekvencia
		new Station(0, 1*20*60*1000, 50, 1, "random", 60*1000, x, y, mc, 10, 10*1000).startMeter();
	}
	
	// Setting up the IoT pricing
	/*ArrayList<Bluemix> bmList = new ArrayList<Bluemix>();
	bmList.add(new Bluemix(0,499999,0.00097));
	bmList.add(new Bluemix(450000,6999999,0.00068));
	bmList.add(new Bluemix(7000000,Long.MAX_VALUE,0.00014));
		
	new BluemixProvider(bmList,ca1); new BluemixProvider(bmList,ca2); new BluemixProvider(bmList,fa1);
	new BluemixProvider(bmList,fa2); new BluemixProvider(bmList,fa3); new BluemixProvider(bmList,fa4);
	
	new AmazonProvider(5,1000000,512,ca1); new AmazonProvider(5,1000000,512,ca2); new AmazonProvider(5,1000000,512,fa1);
	new AmazonProvider(5,1000000,512,fa2); new AmazonProvider(5,1000000,512,fa3); new AmazonProvider(5,1000000,512,fa4);
	
	new AzureProvider(86400000,421.65,6000000,4,ca1); new AzureProvider(86400000,421.65,6000000,4,ca2); new AzureProvider(86400000,421.65,6000000,4,fa1);
	new AzureProvider(86400000,421.65,6000000,4,fa2);  new AzureProvider(86400000,421.65,6000000,4,fa3); new AzureProvider(86400000,421.65,6000000,4,fa4);
	
	new OracleProvider(2678400000L,0.93,15000,0.02344,1000, ca1); new OracleProvider(2678400000L,0.93,15000,0.02344,1000, ca2); 
	new OracleProvider(2678400000L,0.93,15000,0.02344,1000, fa1); new OracleProvider(2678400000L,0.93,15000,0.02344,1000, fa2);
	new OracleProvider(2678400000L,0.93,15000,0.02344,1000, fa3); new OracleProvider(2678400000L,0.93,15000,0.02344,1000, fa4);*/
		
	// we start the simulation
	long starttime = System.nanoTime();
	Timed.simulateUntilLastEvent();
	long stoptime = System.nanoTime();
	
	// Print some information to the monitor / in file
	//TimelineGenerator.generate();
	//FogSimulationChart.generate();
	/*ScenarioBase.printInformation((stopttime-starttime),true);
	ScenarioBase.printInformation(stopttime-starttime, false);*/
	MicrocontrollerConsumptionChartGenerator.generate();
	FogSimulationChart.generate();
	ScenarioEnergy.printInformation(stoptime-starttime, false);
	
	}

}
