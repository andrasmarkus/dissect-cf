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

public class NewFogSimulationHold   {
	
	public static void main(String[] args) throws Exception {
	
	// app strategy
	final String strategy = "hold";
	
	// device strategy
	final String deviceStrategy = "random";
	
	// num. of the instruction
	final long numberOfInstruction = 600;
	
	// tasksize -> 250kB
	final long tasksize = 256000;
	
	// app frequency ->5 min
	final long freq = 5*60*1000;
	
	//  smart devices' characteristics
	final int numberOfDevices = 10000;
	final long deviceFreq = 60*1000; // -> 1 min
	final long deviceStart = 0;
	final long deviceStop = 24*60*60*1000; // -> 24 hours
		
	// for a VM, first we need a virtual machine image, it needs 100 instruction for creating a VM from it, and it needs 1 GB of free space on a PM
	VirtualAppliance va = new VirtualAppliance("va", 100, 0, false, 1073741824L);
			
	// we have to define the resource needs of the VM
	AlterableResourceConstraints arc1 = new AlterableResourceConstraints(2,0.001,4294967296L);
	AlterableResourceConstraints arc2 = new AlterableResourceConstraints(4,0.001,8589934592L);
	AlterableResourceConstraints arc3 = new AlterableResourceConstraints(8,0.001,17179869184L);
			
	// and now we make join them into 1 object called Instance with different hourly price
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
	
		// creating the cloud application modules
		Application fogapp1 = new Application(freq, tasksize, "a1.large", "London-app", numberOfInstruction, 1, strategy, true);
		Application fogapp2 = new Application(freq, tasksize, "a1.xlarge", "Brussels-app", numberOfInstruction, 1, strategy, false);
		Application fogapp3 = new Application(freq, tasksize, "a1.large", "Amsterdam-app", numberOfInstruction, 1, strategy, true);
		Application fogapp4 = new Application(freq, tasksize, "a1.large", "Paris-app", numberOfInstruction, 1, strategy, true);
		
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
	
		// creating the cloud application modules
		Application fogapp5 = new Application(freq, tasksize, "a1.large", "Bratislava-app", numberOfInstruction, 1, strategy, true);
		Application fogapp6 = new Application(freq, tasksize, "a1.xlarge", "Budapest-app", numberOfInstruction, 1, strategy, false);
		Application fogapp7 = new Application(freq, tasksize, "a1.large", "Prague-app", numberOfInstruction, 1, strategy, true);
		Application fogapp8 = new Application(freq, tasksize, "a1.large", "Vienna-app", numberOfInstruction, 1, strategy, true);
		
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
	
		// creating the cloud application modules
		Application fogapp9 = new Application(freq, tasksize, "a1.xlarge", "Kiev-app", numberOfInstruction, 1, strategy, false);
		Application fogapp10 = new Application(freq, tasksize, "a1.large", "Moscow-app", numberOfInstruction, 1, strategy, true);
		Application fogapp11 = new Application(freq, tasksize, "a1.large", "Vilnius-app", numberOfInstruction, 1, strategy, true);
		Application fogapp12 = new Application(freq, tasksize, "a1.large", "Warsaw-app", numberOfInstruction, 1, strategy, true);

		// mapping applications to the resources
		fog9.addApplication(fogapp9);
		fog10.addApplication(fogapp10);
		fog11.addApplication(fogapp11);
		fog12.addApplication(fogapp12);
		
	// hold level fog nodes
	String holdfogfile1 = ScenarioBase.resourcePath+"/fuzzy/LPDS_athen.xml";	
	String holdfogfile2 = ScenarioBase.resourcePath+"/fuzzy/LPDS_stockholm.xml";	
		
	ComputingAppliance fog13 = new ComputingAppliance(holdfogfile1, "Athen", 24, 38);
	ComputingAppliance fog14 = new ComputingAppliance(holdfogfile2, "Stockholm", 18, 59);
		
			// creating the cloud application modules
			Application fogapp13 = new Application(freq, tasksize, "a1.xlarge", "Athen-app", numberOfInstruction, 1, strategy, false);
			Application fogapp14 = new Application(freq, tasksize, "a1.xlarge", "Stockholm-app", numberOfInstruction, 1, strategy, false);
			
			// mapping applications to the resources
			fog13.addApplication(fogapp13);
			fog14.addApplication(fogapp14);
			
	// cloud
	String cloudfile1 = ScenarioBase.resourcePath+"/fuzzy/LPDS_frankfurt.xml";	
	
	ComputingAppliance cloud1 = new ComputingAppliance(cloudfile1, "Frankfurt", 15, 52);
	
		// creating the cloud application modules
		Application cloudapp1 = new Application(freq, tasksize, "a2.xlarge", "Frankfurt-app", numberOfInstruction, 1, strategy, false);
		
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
		cloud1.setLatency(fog2, 10);
		cloud1.setLatency(fog6, 18);
		cloud1.setLatency(fog9, 31);
		
		fog1.addNeighbour(fog3, fog4);
		fog3.addNeighbour(fog4);
		fog1.setParentNode(fog2);
		fog3.setParentNode(fog2);
		fog4.setParentNode(fog2);
	
		fog5.addNeighbour(fog7, fog8);	
		fog7.addNeighbour(fog8);
		fog5.setParentNode(fog6);
		fog7.setParentNode(fog6);
		fog8.setParentNode(fog6);
		
		fog10.addNeighbour(fog11, fog12);
		fog11.addNeighbour(fog12);
		fog10.setParentNode(fog9);
		fog11.setParentNode(fog9);
		fog12.setParentNode(fog9);
		
		fog2.addNeighbour(fog6, fog9, fog13, fog14);
		fog6.addNeighbour(fog9, fog13, fog14);
		fog9.addNeighbour(fog13, fog14);
		fog13.addNeighbour(fog14);
		
		fog2.setParentNode(cloud1);
		fog6.setParentNode(cloud1);
		fog9.setParentNode(cloud1);
		fog13.setParentNode(cloud1);
		fog14.setParentNode(cloud1);


		/* 0-24 */
		for(int i=0;i<numberOfDevices;i++) {
			int x,y;
			Random holdGenerator = new Random();
			x = holdGenerator.nextInt(49)-8;
			y = holdGenerator.nextInt(21)+40;
			
			DeviceNetwork dn  = new DeviceNetwork(50, 10240, 10000, 10000, 10000, "dnRepository"+i, null, null);
			new Station(dn, deviceStart, deviceStop, 50, deviceStrategy, 5, deviceFreq, x, y).startMeter();
		}
		/*
		// 5-24 
		for(int i=0;i<2000;i++) {
			int x,y;
			Random holdGenerator = new Random();
			x = holdGenerator.nextInt(49)-8;
			y = holdGenerator.nextInt(21)+40;
			
			DeviceNetwork dn  = new DeviceNetwork(50, 10240, 10000, 10000, 10000, "dnRepository"+i, null, null);
			new Station(dn, 5*60*60*1000, deviceStop, 50, deviceStrategy, 5, deviceFreq, x, y).startMeter();
		}
		
		// 10-24 
		for(int i=0;i<2000;i++) {
			int x,y;
			Random holdGenerator = new Random();
			x = holdGenerator.nextInt(49)-8;
			y = holdGenerator.nextInt(21)+40;
			
			DeviceNetwork dn  = new DeviceNetwork(50, 10240, 10000, 10000, 10000, "dnRepository"+i, null, null);
			new Station(dn, 10*60*60*1000, deviceStop, 50, deviceStrategy, 5, deviceFreq, x, y).startMeter();
		}
		
		// 15-24 
		for(int i=0;i<2000;i++) {
			int x,y;
			Random holdGenerator = new Random();
			x = holdGenerator.nextInt(49)-8;
			y = holdGenerator.nextInt(21)+40;
			
			DeviceNetwork dn  = new DeviceNetwork(50, 10240, 10000, 10000, 10000, "dnRepository"+i, null, null);
			new Station(dn, 15*60*60*1000, deviceStop, 50, deviceStrategy, 5, deviceFreq, x, y).startMeter();
		}
		
		// 20-24
		for(int i=0;i<2000;i++) {
			int x,y;
			Random holdGenerator = new Random();
			x = holdGenerator.nextInt(49)-8;
			y = holdGenerator.nextInt(21)+40;
			
			DeviceNetwork dn  = new DeviceNetwork(50, 10240, 10000, 10000, 10000, "dnRepository"+i, null, null);
			new Station(dn, 20*60*60*1000, deviceStop, 50, deviceStrategy, 5, deviceFreq, x, y).startMeter();
		} */
		
		
		
		// we start the simulation
		long starttime = System.nanoTime();
		Timed.simulateUntilLastEvent();
		long stopttime = System.nanoTime();
		
		// Print some information to the monitor / in file
		TimelineGenerator.generate();
		ScenarioBase.printInformation((stopttime-starttime),false);
	}
	
}