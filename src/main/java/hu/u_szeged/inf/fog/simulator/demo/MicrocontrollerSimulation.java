package hu.u_szeged.inf.fog.simulator.demo;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.Microcontroller;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.mta.sztaki.lpds.cloud.simulator.util.PowerTransitionGenerator;
import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.iot.Device.DeviceNetwork;
import hu.u_szeged.inf.fog.simulator.iot.Station;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;
import hu.u_szeged.inf.fog.simulator.providers.Instance;

public class MicrocontrollerSimulation {

	public static void main(String[] args) throws Exception {		
		VirtualAppliance va = new VirtualAppliance("va", 100, 0, false, 1073741824L);
		
		AlterableResourceConstraints arc1 = new AlterableResourceConstraints(8,0.001,4294967296L);
		
		new Instance(va,arc1,0.00000001,"instance1");
		
		String cloudfile = ScenarioBase.resourcePath+"LPDS_original.xml";	
		
		ComputingAppliance cloud1 = new ComputingAppliance(cloudfile, "cloud1",-4,5);
		
		Application ca1 = new Application(5*60*1000, 256000, "instance1", "Cloud-app1", 2400.0, 1, "random", false, true);

		System.out.println(cloud1.iaas.repositories.get(0).getLatencies());
		System.out.println(cloud1.iaas.repositories.get(0).getLatencies());
		
		cloud1.addApplication(ca1);
		
		for(int i=0;i<1;i++) {
			int x,y;
			Random randomGenerator = new Random();
			x = randomGenerator.nextInt(21)-10;
			y = randomGenerator.nextInt(9)-10;
			
			HashMap<String, Integer> latencyMap = new HashMap<String, Integer>();
			
			final long disksize = 100001;
			
			final EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> transitions = PowerTransitionGenerator.generateTransitions(10, 200, 300, 5, 5);
			final Map<String, PowerState> cpuTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.host);
			final Map<String, PowerState> stTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.storage);
			final Map<String, PowerState> nwTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.network);
			
			final Microcontroller mc;
			mc = new Microcontroller(1, 1, 1000, new Repository(disksize, "mc", 10000, 10000, 10000, latencyMap, stTransitions, nwTransitions), 10, 10, cpuTransitions);
			
			/*DeviceNetwork dn  = new DeviceNetwork(10, 10240, 10000, 10000, 10000, "mc", null, null,
					1, 1, 1000, new Repository(100001, "mc", 10000, 10000, 10000, latencyMap, stTransitions, nwTransitions), 10, 10, cpuTransitions);*/
			new Station(10*60*1000,dn, 0, 24*60*60*1000, 50, "random", 5, 60*1000, x, y, mc, 10).startMeter();
		}
		
		Timed.simulateUntilLastEvent();

	}

}
