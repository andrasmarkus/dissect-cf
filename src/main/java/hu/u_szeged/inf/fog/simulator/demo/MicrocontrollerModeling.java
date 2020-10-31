package hu.u_szeged.inf.fog.simulator.demo;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.Microcontroller;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.Microcontroller.State;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.util.PowerTransitionGenerator;

public class MicrocontrollerModeling {

	public static void main(String[] args) throws Exception {
		final long disksize = 100001;
		HashMap<String, Integer> latencyMap = new HashMap<String, Integer>();
		latencyMap.put("pm1", 3);
		latencyMap.put("repo", 3);
		final EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> transitions = PowerTransitionGenerator.generateTransitions(10, 200, 300, 5, 5);
		final Map<String, PowerState> cpuTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.host);
		final Map<String, PowerState> stTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.storage);
		final Map<String, PowerState> nwTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.network);
		Repository r = new Repository(disksize, "repo", 10000, 10000, 10000, latencyMap, stTransitions, nwTransitions);
		r.setState(NetworkNode.State.METERING);
		
		Microcontroller mc;
		mc = new Microcontroller(1, 1, 1000, new Repository(disksize, "pm1", 10000, 10000, 10000, latencyMap, stTransitions, nwTransitions), 10, 10, cpuTransitions);
		
		//mc.turnon();
		mc.turnon();
		
		System.out.println(mc.getState());
	}

}
