package hu.u_szeged.inf.fog.simulator.demo;

import java.util.ArrayList;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.iot.Actuator;
import hu.u_szeged.inf.fog.simulator.iot.ActuatorRandomStrategy;
import hu.u_szeged.inf.fog.simulator.iot.Device.DeviceNetwork;
import hu.u_szeged.inf.fog.simulator.iot.SensorCharacteristics;
import hu.u_szeged.inf.fog.simulator.iot.Station;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;
import hu.u_szeged.inf.fog.simulator.providers.AmazonProvider;
import hu.u_szeged.inf.fog.simulator.providers.AzureProvider;
import hu.u_szeged.inf.fog.simulator.providers.BluemixProvider;
import hu.u_szeged.inf.fog.simulator.providers.BluemixProvider.Bluemix;
import hu.u_szeged.inf.fog.simulator.providers.Instance;
import hu.u_szeged.inf.fog.simulator.providers.OracleProvider;
import hu.u_szeged.inf.fog.simulator.util.TimelineGenerator;

/**
 * This class presents an IoT simulation dealing with 2 clouds and 500 smart devices and we also calculate the cost of the applications
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
public class IoTSimulation {
	
	public static void main(String[] args) throws Exception {
			
	// we create our clouds using predefined cloud schemas
	String cloudfile = ScenarioBase.resourcePath+"LPDS_original.xml";	
			
	ComputingAppliance cloud1 = new ComputingAppliance(cloudfile, "cloud1",0,0);
	ComputingAppliance cloud2 = new ComputingAppliance(cloudfile, "cloud2",0,0);
	
	// creating vm images and its resource needs
	
	// for a VM, first we need a virtual machine image, it needs 100 instruction for creating a VM from it, and it needs 1 GB of free space on a PM
	VirtualAppliance va = new VirtualAppliance("va", 100, 0, false, 1073741824L);
	
	// we have to define the resource needs of the VM, we use 1-2-4-8 CPU cores, 0.001 core processing power and 4 GB RAM
	AlterableResourceConstraints arc1 = new AlterableResourceConstraints(1,0.001,4294967296L);
	AlterableResourceConstraints arc2 = new AlterableResourceConstraints(2,0.001,4294967296L);
	AlterableResourceConstraints arc3 = new AlterableResourceConstraints(4,0.001,4294967296L);
	AlterableResourceConstraints arc4 = new AlterableResourceConstraints(8,0.001,4294967296L);
	
	// and now we make join them into 1 object called Instance with different hourly price (~0.036$)
	new Instance(va,arc1,0.00000001,"instance1");
	new Instance(va,arc2,0.000000015,"instance2");
	new Instance(va,arc3,0.000000020,"instance3");
	new Instance(va,arc4,0.000000025,"instance4");
	
	// creating the application modules: 5 minutes frequency, 250kB task size and max. 2400 instruction / task
	Application ca1 = new Application(5*60*1000, 256000, "instance1", "app1", 2400.0,1, "random", true);
	Application ca2 = new Application(5*60*1000, 256000, "instance3", "app2", 2400.0,1, "random", true);
	Application ca3 = new Application(5*60*1000, 256000, "instance2", "app3", 2400.0,1, "random", true);
	Application ca4 = new Application(5*60*1000, 256000, "instance4", "app4", 2400.0,1, "random", true);
	
	cloud1.addApplication(ca1,ca2);
	cloud2.addApplication(ca3,ca4);
	// we create 500 smart device with random installation strategy, 10kB storage, 10000 bandwidth, 
	// 24 hours long running time, 50 bytes of generated data by each sensor, each smart device has 5 sensor,
	// and the frequency is 1 minute, last 3 zero parameters are for the geolocation, but it is now irrelevant for us
	for(int i=0;i<500;i++) {
		DeviceNetwork dn  = new DeviceNetwork(10, 10240, 10000, 10000, 10000, "dnRepository"+i, null, null);
		new Station(10*60*1000, 50,dn, new Actuator(new ActuatorRandomStrategy()), 0, 24*60*60*1000,50, "random", new SensorCharacteristics(5, 5,30000, 60000, 0.3, 0.3), 60*1000, 0, 0).startMeter();
	}
	
	// Setting up the IoT pricing
	ArrayList<Bluemix> bmList = new ArrayList<Bluemix>();
	bmList.add(new Bluemix(0,499999,0.00097));
	bmList.add(new Bluemix(450000,6999999,0.00068));
	bmList.add(new Bluemix(7000000,Long.MAX_VALUE,0.00014));
		
	new BluemixProvider(bmList,ca1); new BluemixProvider(bmList,ca2); 
	new BluemixProvider(bmList,ca3); new BluemixProvider(bmList,ca4);
	
	new AmazonProvider(5,1000000,512,ca1); new AmazonProvider(5,1000000,512,ca2);
	new AmazonProvider(5,1000000,512,ca4); new AmazonProvider(5,1000000,512,ca3);
	
	new AzureProvider(86400000,421.65,6000000,4,ca1); new AzureProvider(86400000,421.65,6000000,4,ca2);
	new AzureProvider(86400000,421.65,6000000,4,ca3); new AzureProvider(86400000,421.65,6000000,4,ca4);
	
	new OracleProvider(2678400000L,0.93,15000,0.02344,1000, ca1); new OracleProvider(2678400000L,0.93,15000,0.02344,1000, ca2);
	new OracleProvider(2678400000L,0.93,15000,0.02344,1000, ca3); new OracleProvider(2678400000L,0.93,15000,0.02344,1000, ca4);
	
	// we start the simulation
	long starttime = System.nanoTime();
	Timed.simulateUntilLastEvent();
	long stopttime = System.nanoTime();
	
	// Print some information to the monitor / in file
	TimelineGenerator.generate();
	ScenarioBase.printInformation((stopttime-starttime),true);
	
	}

}
