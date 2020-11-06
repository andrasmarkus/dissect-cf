package hu.u_szeged.inf.fog.simulator.demo;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.energy.specialized.PhysicalMachineEnergyMeter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.Microcontroller;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
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
		final Repository r = new Repository(disksize, "repo", 10000, 10000, 10000, latencyMap, stTransitions, nwTransitions);
		r.setState(NetworkNode.State.RUNNING);
		
		final Microcontroller mc;
		mc = new Microcontroller(1, 1, 1000, new Repository(disksize, "mc", 10000, 10000, 10000, latencyMap, stTransitions, nwTransitions), 10, 10, cpuTransitions);
		
		//mc.turnon();
		final PhysicalMachineEnergyMeter pmm;
		final long freq = 100;
		final ArrayList<Long> readingtime = new ArrayList<Long>();
		final ArrayList<Double> readingpm = new ArrayList<Double>();
		
		pmm = new PhysicalMachineEnergyMeter(mc);
		
		class MeteredDataCollector extends Timed {
			// Call when we need to initiate data collection
			public void start() {
				subscribe(freq);
			}

			// Call when we need to terminate data collection
			public void stop() {
				unsubscribe();
			}

			// Automatically called with the frequency specified above
			@Override
			public void tick(final long fires) {
				
				try {
		            if (r.getCurrState().equals(Repository.State.RUNNING)) {
		                mc.metering();
		            }
		        } catch (NetworkException e) {
		            e.printStackTrace();
		        }
				
				readingtime.add(fires);
				readingpm.add(pmm.getTotalConsumption());
			}
		}
		final MeteredDataCollector mdc = new MeteredDataCollector();
		pmm.startMeter(freq, true);
		mdc.start();
		Timed.simulateUntil(Timed.getFireCount() + 1000);
		
		for (int i = 0; i < readingtime.size(); i++) {
			System.out.println(readingtime.get(i) + "," + readingpm.get(i) + "\n");
		}
		
	}

}
