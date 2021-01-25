package hu.u_szeged.inf.fog.simulator.demo;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.mta.sztaki.lpds.cloud.simulator.util.PowerTransitionGenerator;
import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.iot.Station;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;
import hu.u_szeged.inf.fog.simulator.physical.Microcontroller;
import hu.u_szeged.inf.fog.simulator.providers.BluemixProvider;
import hu.u_szeged.inf.fog.simulator.providers.BluemixProvider.Bluemix;
import hu.u_szeged.inf.fog.simulator.providers.Instance;
import hu.u_szeged.inf.fog.simulator.util.FogSimulationChart;
import hu.u_szeged.inf.fog.simulator.util.MicrocontrollerConsumptionChartGenerator;
import hu.u_szeged.inf.fog.simulator.util.MicrocontrollerPowerTransitionGenerator;

public class CLOSER {
	
	public static void main(String[] args) throws Exception {
		
		// 2 GB virtual image,
		VirtualAppliance va = new VirtualAppliance("va", 100, 0, false, 2147483648L);
		
		// 8 CPU, 4 GB RAM and 4 CPU 2 GB RAM
		AlterableResourceConstraints arc1 = new AlterableResourceConstraints(8,0.001,4294967296L);
		AlterableResourceConstraints arc2 = new AlterableResourceConstraints(4,0.001,2147483648L);
		
		// hourly price: 0.202$, 0.101$ - AWS
		new Instance(va,arc1,0.000000056,"instance1");
		new Instance(va,arc2,0.000000028,"instance2");
		
		// resource file of LPDS
		String cloudfile = ScenarioBase.resourcePath+"LPDS_frankfurt.xml";
		String fogfile1 = ScenarioBase.resourcePath+"LPDS_london.xml";
		String fogfile2 = ScenarioBase.resourcePath+"LPDS_budapest.xml";
		String fogfile3 = ScenarioBase.resourcePath+"LPDS_vienna.xml";
		
		// topology frankfurt - cloud1; london, budapest, vienna
		ComputingAppliance cloud1 = new ComputingAppliance(cloudfile, "cloud1",15, 52);
		
		ComputingAppliance fog1 = new ComputingAppliance(fogfile1, "fog1", 0, 51);
		ComputingAppliance fog2 = new ComputingAppliance(fogfile2, "fog2", 19, 48);
		ComputingAppliance fog3 = new ComputingAppliance(fogfile3, "fog3", 16, 48);
		
		// IoT application; 5 min. freq, 
		Application ca1 = new Application(5*60*1000, 256000, "instance1", "Cloud-app1", 1200.0, 1, "energy", false, true);
		
		Application fa1 = new Application(5*60*1000, 256000, "instance2", "Fog-app1", 1200.0, 1, "energy", true, true);
		Application fa2 = new Application(5*60*1000, 256000, "instance2", "Fog-app2", 1200.0, 1, "energy", true, true);
		Application fa3 = new Application(5*60*1000, 256000, "instance2", "Fog-app3", 1200.0, 1, "energy", true, true);
		
	
		// mapping IoT app to a node
		cloud1.addApplication(ca1);
		fog1.addApplication(fa1);
		fog2.addApplication(fa2);
		fog3.addApplication(fa3);
		
		fog1.addNeighbour(fog2, fog3);
		fog1.setParentNode(cloud1);
		fog2.setParentNode(cloud1);
		fog3.setParentNode(cloud1);
		
		// london budapest frankfurt vienna
		fog1.setLatency(fog2, 29);
		fog1.setLatency(cloud1, 14);
		fog1.setLatency(fog3, 25);
		fog2.setLatency(cloud1, 17);
		fog2.setLatency(fog3, 6);
		fog3.setLatency(cloud1, 15);
		
		// IoT devices
		for(int i=0;i<100;i++) {
			
			int x,y;
			Random randomGenerator = new Random();
			x = randomGenerator.nextInt(60)-20;
			y = randomGenerator.nextInt(92)-20;
			
			HashMap<String, Integer> latencyMap = new HashMap<String, Integer>();
						
			//ESP32 (0.025, 0.155, 0.225)
			//Raspberry Pi (0.065, 1.475, 2.0)
			final EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> transitions = 
						MicrocontrollerPowerTransitionGenerator.generateTransitions(0.065, 1.475, 2.0, 0, 0);
			
			//final EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> transitions = 
			//		MicrocontrollerPowerTransitionGenerator.generateTransitions(0.025, 0.155, 0.225, 0, 0);
			
			final Map<String, PowerState> cpuTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.host);
			final Map<String, PowerState> stTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.storage);
			final Map<String, PowerState> nwTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.network);
			
			Microcontroller mc = new Microcontroller(1, 1, 1,
									 new Repository(536870912, "mc", 125000000, 125000000, 125000000, latencyMap, stTransitions, nwTransitions),
									 1, 1, cpuTransitions);
			
			// 60 min. operation time, 1 minute frequency, 10 sec. sensor freq
			new Station(0, 1*60*60*1000, 50, 1, "random", 60*1000, x, y, mc, 50, 10*1000).startMeter();
		}

		ArrayList<Bluemix> bmList = new ArrayList<Bluemix>();
		bmList.add(new Bluemix(0,499999,0.00097));
		bmList.add(new Bluemix(450000,6999999,0.00068));
		bmList.add(new Bluemix(7000000,Long.MAX_VALUE,0.00014));
		
		new BluemixProvider(bmList,ca1); new BluemixProvider(bmList,fa3);
		new BluemixProvider(bmList,fa1); new BluemixProvider(bmList,fa2);
		
		long starttime = System.nanoTime();
		Timed.simulateUntilLastEvent();
		long stoptime = System.nanoTime();
		
		MicrocontrollerConsumptionChartGenerator.generate();
		FogSimulationChart.generate();
		ScenarioEnergy.printInformation(stoptime-starttime, true);
		
	}

}
