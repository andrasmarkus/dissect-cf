package hu.u_szeged.inf.fog.simulator.demo;

import java.util.Random;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.iot.Station;
import hu.u_szeged.inf.fog.simulator.iot.Device.DeviceNetwork;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;
import hu.u_szeged.inf.fog.simulator.providers.Instance;
import hu.u_szeged.inf.fog.simulator.util.TimelineGenerator;

public class NewFogSimulation   {
	
	public static void main(String[] args) throws Exception {
		
	// creating vm images and its resource needs
		
	// for a VM, first we need a virtual machine image, it needs 100 instruction for creating a VM from it, and it needs 1 GB of free space on a PM
	VirtualAppliance va = new VirtualAppliance("va", 100, 0, false, 1073741824L);
			
	// we have to define the resource needs of the VM, we use 4-8 CPU cores, 0.001 core processing power and 4 GB RAM
	AlterableResourceConstraints arc1 = new AlterableResourceConstraints(2,0.001,4294967296L);
	AlterableResourceConstraints arc2 = new AlterableResourceConstraints(4,0.001,8589934592L);
	AlterableResourceConstraints arc3 = new AlterableResourceConstraints(8,0.001,17179869184L);
			
	// and now we make join them into 1 object called Instance with different hourly price (~0.036$)
	new Instance(va,arc1,0.0000000141666667,"a1.large");
	new Instance(va,arc2,0.0000000283333333,"a1.xlarge");
	new Instance(va,arc3,0.0000000566666667,"a2.xlarge");	
		

	// cluster west: Amsterdam, Brussels, London, Paris
	String westfile1 = ScenarioBase.resourcePath+"/fuzzy/LPDS_london.xml";	
	String westfile2 = ScenarioBase.resourcePath+"/fuzzy/LPDS_brussels.xml";	
	String westfile3 = ScenarioBase.resourcePath+"/fuzzy/LPDS_amsterdam.xml";	
	String westfile4 = ScenarioBase.resourcePath+"/fuzzy/LPDS_paris.xml";	
	
	ComputingAppliance fog1 = new ComputingAppliance(westfile1, "London", 0, 51);
	ComputingAppliance fog2 = new ComputingAppliance(westfile2, "Brussels", 4, 51);
	ComputingAppliance fog3 = new ComputingAppliance(westfile3, "Amsterdam", 5, 52);
	ComputingAppliance fog4 = new ComputingAppliance(westfile4, "Paris", 2, 49);
	
		// creating the cloud application modules: 5 minutes frequency, 250kB task size and max. 2400 instruction / task
		Application fogapp1 = new Application(5*60*1000, 256000, "a1.large", "London-app", 2400.0, 1, "random", true);
		Application fogapp2 = new Application(5*60*1000, 256000, "a1.xlarge", "Brussels-app", 2400.0, 1, "random", false);
		Application fogapp3 = new Application(5*60*1000, 256000, "a1.large", "Amsterdam-app", 2400.0, 1, "random", true);
		Application fogapp4 = new Application(5*60*1000, 256000, "a1.large", "Paris-app", 2400.0, 1, "random", true);
		
		// mapping applications to the resources
		fog1.addApplication(fogapp1);
		fog2.addApplication(fogapp2);
		fog3.addApplication(fogapp3);
		fog4.addApplication(fogapp4);
				
	
	// cluster centre: Bratislava, Budapest, Prague, Vienna
	String centrefile1 = ScenarioBase.resourcePath+"/fuzzy/LPDS_bratislava.xml";	
	String centrefile2 = ScenarioBase.resourcePath+"/fuzzy/LPDS_budapest.xml";	
	String centrefile3 = ScenarioBase.resourcePath+"/fuzzy/LPDS_prague.xml";	
	String centrefile4 = ScenarioBase.resourcePath+"/fuzzy/LPDS_vienna.xml";	
	
	ComputingAppliance fog5 = new ComputingAppliance(centrefile1, "Bratislava", 17, 48);
	ComputingAppliance fog6 = new ComputingAppliance(centrefile2, "Budapest", 19, 48);
	ComputingAppliance fog7 = new ComputingAppliance(centrefile3, "Prague", 14, 50);
	ComputingAppliance fog8 = new ComputingAppliance(centrefile4, "Vienna", 16, 48);
	
		// creating the cloud application modules: 5 minutes frequency, 250kB task size and max. 2400 instruction / task
		Application fogapp5 = new Application(5*60*1000, 256000, "a1.large", "Bratislava-app", 2400.0, 1, "random", true);
		Application fogapp6 = new Application(5*60*1000, 256000, "a1.xlarge", "Budapest-app", 2400.0, 1, "random", false);
		Application fogapp7 = new Application(5*60*1000, 256000, "a1.large", "Prague-app", 2400.0, 1, "random", true);
		Application fogapp8 = new Application(5*60*1000, 256000, "a1.large", "Vienna-app", 2400.0, 1, "random", true);
		
		// mapping applications to the resources
			fog5.addApplication(fogapp5);
			fog6.addApplication(fogapp6);
			fog7.addApplication(fogapp7);
			fog8.addApplication(fogapp8);
					
	// cluster east: Kiev, Moscow, Vilnius, Warsaw
	String eastfile1 = ScenarioBase.resourcePath+"/fuzzy/LPDS_kiev.xml";	
	String eastfile2 = ScenarioBase.resourcePath+"/fuzzy/LPDS_moscow.xml";	
	String eastfile3 = ScenarioBase.resourcePath+"/fuzzy/LPDS_vilnius.xml";	
	String eastfile4 = ScenarioBase.resourcePath+"/fuzzy/LPDS_warsaw.xml";	
	
	ComputingAppliance fog9 = new ComputingAppliance(eastfile1, "Kiev", 31, 50);
	ComputingAppliance fog10 = new ComputingAppliance(eastfile2, "Moscow", 38, 56);
	ComputingAppliance fog11 = new ComputingAppliance(eastfile3, "Vilnius", 25, 55);
	ComputingAppliance fog12 = new ComputingAppliance(eastfile4, "Warsaw", 21, 52);
	
		// creating the cloud application modules: 5 minutes frequency, 250kB task size and max. 2400 instruction / task
		Application fogapp9 = new Application(5*60*1000, 256000, "a1.xlarge", "Kiev-app", 2400.0, 1, "random", false);
		Application fogapp10 = new Application(5*60*1000, 256000, "a1.large", "Moscow-app", 2400.0, 1, "random", true);
		Application fogapp11 = new Application(5*60*1000, 256000, "a1.large", "Vilnius-app", 2400.0, 1, "random", true);
		Application fogapp12 = new Application(5*60*1000, 256000, "a1.large", "Warsaw-app", 2400.0, 1, "random", true);

		// mapping applications to the resources
		fog9.addApplication(fogapp9);
		fog10.addApplication(fogapp10);
		fog11.addApplication(fogapp11);
		fog12.addApplication(fogapp12);
		
	// upper level fog nodes
	String upperfogfile1 = ScenarioBase.resourcePath+"/fuzzy/LPDS_athen.xml";	
	String upperfogfile2 = ScenarioBase.resourcePath+"/fuzzy/LPDS_stockholm.xml";	
		
	ComputingAppliance fog13 = new ComputingAppliance(upperfogfile1, "Athen", 24, 38);
	ComputingAppliance fog14 = new ComputingAppliance(upperfogfile2, "Stockholm", 18, 59);
		
			// creating the cloud application modules: 5 minutes frequency, 250kB task size and max. 2400 instruction / task
			Application fogapp13 = new Application(5*60*1000, 256000, "a1.xlarge", "Athen-app", 2400.0, 1, "random", false);
			Application fogapp14 = new Application(5*60*1000, 256000, "a1.xlarge", "Stockholm-app", 2400.0, 1, "random", false);
			
			// mapping applications to the resources
			fog13.addApplication(fogapp13);
			fog14.addApplication(fogapp14);
			
	// cloud
	String cloudfile1 = ScenarioBase.resourcePath+"/fuzzy/LPDS_frankfurt.xml";	
	
	ComputingAppliance cloud1 = new ComputingAppliance(cloudfile1, "Frankfurt", 15, 52);
	
		// creating the cloud application modules: 5 minutes frequency, 250kB task size and max. 2400 instruction / task
		Application cloudapp1 = new Application(5*60*1000, 256000, "a2.xlarge", "Athen-app", 2400.0, 1, "random", false);
		
		cloud1.addApplication(cloudapp1);
	
	/************ CONNECTIONS AND LATENCIES ************/ 					
		fog1.setLatency(fog2, 8);
		fog1.setLatency(fog3, 15);
		fog1.setLatency(fog4, 8);
		fog2.setLatency(fog3, 5);
		fog2.setLatency(fog4, 14);
		fog3.setLatency(fog4, 10);		
			
		fog5.setLatency(fog6, 5);
		fog5.setLatency(fog7, 14);
		fog5.setLatency(fog8, 2);
		fog6.setLatency(fog7, 9);
		fog6.setLatency(fog8, 7);
		fog7.setLatency(fog8, 2);
		
		fog9.setLatency(fog10, 40);
		fog9.setLatency(fog11, 30);
		fog9.setLatency(fog12, 21);
		fog10.setLatency(fog11, 24);
		fog10.setLatency(fog12, 54);
		fog11.setLatency(fog12, 10);
		
		fog2.setLatency(fog6, 28);
		fog2.setLatency(fog9, 52);
		fog2.setLatency(fog13, 29);
		fog2.setLatency(fog14, 56);
		fog6.setLatency(fog9, 42);
		fog6.setLatency(fog13, 44);
		fog6.setLatency(fog14, 41);
		fog9.setLatency(fog13, 67);
		fog9.setLatency(fog14, 71);
		fog13.setLatency(fog14, 70);
		
		cloud1.setLatency(fog13, 28);
		cloud1.setLatency(fog14, 48);
		
		fog1.addNeighbour(fog3, fog4);
		fog1.setParentNode(fog2);
		fog3.setParentNode(fog2);
		fog4.setParentNode(fog2);
	
		fog5.addNeighbour(fog7, fog8);	
		fog5.setParentNode(fog6);
		fog7.setParentNode(fog6);
		fog8.setParentNode(fog6);
		
		fog10.addNeighbour(fog11, fog12);
		fog10.setParentNode(fog9);
		fog11.setParentNode(fog9);
		fog12.setParentNode(fog9);
		
		fog2.addNeighbour(fog6, fog9, fog13, fog14);
		
		fog13.setParentNode(cloud1);
		fog14.setParentNode(cloud1);
		
		

		// we create 1000 smart device with random installation strategy, 10kB storage, 10000 bandwidth, 
		// 24 hours long running time, 50 bytes of generated data by each sensor, each smart device has 5 sensor,
		// and the frequency is 1 minute, last 3 zero parameters are for the geolocation, but it is now irrelevant for us
		for(int i=0;i<1000;i++) {
			int x,y;
			Random randomGenerator = new Random();
			x = randomGenerator.nextInt(49)-8;
			y = randomGenerator.nextInt(21)+40;
			
			DeviceNetwork dn  = new DeviceNetwork(50, 10240, 10000, 10000, 10000, "dnRepository"+i, null, null);
			new Station(10*60*1000,dn, 0, 24*60*60*1000, 50, "random", 5, 60*1000, x, y).startMeter();
		}
		
		
		// we start the simulation
		long starttime = System.nanoTime();
		Timed.simulateUntilLastEvent();
		long stopttime = System.nanoTime();
		
		// Print some information to the monitor / in file
		TimelineGenerator.generate();
		ScenarioBase.printInformation((stopttime-starttime),false);
	}
	
}