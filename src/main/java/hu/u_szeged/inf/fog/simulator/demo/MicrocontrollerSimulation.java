package hu.u_szeged.inf.fog.simulator.demo;

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
import hu.u_szeged.inf.fog.simulator.providers.Instance;
import hu.u_szeged.inf.fog.simulator.util.FogSimulationChart;
import hu.u_szeged.inf.fog.simulator.util.MicrocontrollerConsumptionChartGenerator;
import hu.u_szeged.inf.fog.simulator.util.MicrocontrollerPowerTransitionGenerator;

public class MicrocontrollerSimulation {

	public static void main(String[] args) throws Exception {		
		VirtualAppliance va = new VirtualAppliance("va", 100, 0, false, 1073741824L);
		
		AlterableResourceConstraints arc1 = new AlterableResourceConstraints(8,0.001,4294967296L);
			
		new Instance(va,arc1,0.00000001,"instance1");
		
		String cloudfile = ScenarioBase.resourcePath+"LPDS_original.xml";
		
		ComputingAppliance cloud1 = new ComputingAppliance(cloudfile, "cloud1",0,0);
		
		Application ca1 = new Application(5*60*1000, 256000, "instance1", "Cloud-app1", 2400.0, 1, "random", true, true);
		
		cloud1.addApplication(ca1);
		
		for(int i=0;i<10;i++) {
			int x,y;
			Random randomGenerator = new Random();
			x = randomGenerator.nextInt(21)-10;
			y = randomGenerator.nextInt(9)-10;
			
			HashMap<String, Integer> latencyMap = new HashMap<String, Integer>();
			
			final long disksize = 100001;
			
			//ESP32 (0.025, 0.155, 0.2)
			final EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> transitions = 
						MicrocontrollerPowerTransitionGenerator.generateTransitions(0.025, 0.155, 0.225, 0, 0);
			//final EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> transitions = 
						//MicrocontrollerPowerTransitionGenerator.generateTransitions(0.5, 1.45,1.7, 0, 0);
			final Map<String, PowerState> cpuTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.host);
			final Map<String, PowerState> stTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.storage);
			final Map<String, PowerState> nwTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.network);
			
			final Microcontroller mc;
			mc = new Microcontroller(1, 1, 1000, new Repository(disksize, "mc", 100, 100, 100, latencyMap, stTransitions, nwTransitions), 1, 1, cpuTransitions);
			
			// 20 perc üzemidõ - 1 perces frekvencia - 10 mp szenzorfrekvencia
			new Station(0, 1*20*60*1000, 50, 1, "random", 60*1000, x, y, mc, 10, 10*1000).startMeter();
		}

		long starttime = System.nanoTime();
		Timed.simulateUntilLastEvent();
		long stoptime = System.nanoTime();
		
		MicrocontrollerConsumptionChartGenerator.generate();
		FogSimulationChart.generate();
		ScenarioEnergy.printInformation(stoptime-starttime, false);
		
	}

}
