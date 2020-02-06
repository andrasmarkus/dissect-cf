package hu.u_szeged.inf.fog.simulator.util;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.pmscheduling.AlwaysOnMachines;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.pmscheduling.PhysicalMachineController;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.vmscheduling.RoundRobinScheduler;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.vmscheduling.Scheduler;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.util.PowerTransitionGenerator;
/**
 * Origin:
 * https://github.com/kecskemeti/dissect-cf-examples/blob/master/src/main/java/hu/mta/sztaki/lpds/cloud/simulator/examples/util/DCCreation.java
 * @author Andras Markus
 *
 */
public class IaaSCreator {

	public static IaaSService createDataCentre() throws Exception {
		
		// setting up vm and pm schedulers
		Class<? extends Scheduler> vmSched = RoundRobinScheduler.class;
		Class<? extends PhysicalMachineController> pmSched = AlwaysOnMachines.class;
		IaaSService iaas = new IaaSService(vmSched, pmSched);
		
		
		
		final EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> transitions = PowerTransitionGenerator
				.generateTransitions(20, 296, 493, 50, 108);
		final Map<String, PowerState> stTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.storage);
		final Map<String, PowerState> nwTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.network);

		

		// Creating the VA store for the cloud

		final String repoid = "Storage";
		// scaling the bandwidth accroding to the size of the cloud
		final double bwRatio = (numofCores * numofNodes) / (7f * 64f);
		// A single repo will hold 36T of data
		HashMap<String, Integer> latencyMapRepo = new HashMap<String, Integer>(numofNodes + 2);
		Repository mainStorage = new Repository(36000000000000l, repoid, (long) (bwRatio * 1250000),
				(long) (bwRatio * 1250000), (long) (bwRatio * 250000), latencyMapRepo, stTransitions, nwTransitions);
		iaas.registerRepository(mainStorage);

		// Creating the PMs for the cloud

		final Map<String, PowerState> cpuTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.host);
		ArrayList<PhysicalMachine> completePMList = new ArrayList<PhysicalMachine>(numofNodes);
		HashMap<String, Integer> latencyMapMachine = new HashMap<String, Integer>(numofNodes + 2);
		latencyMapMachine.put(repoid, 5); // 5 ms latency towards the repos
		final String machineid = "Node";
		for (int i = 1; i <= numofNodes; i++) {
			String currid = machineid + i;
			final double pmBWRatio = Math.max(numofCores / 7f, 1);
			PhysicalMachine pm = new PhysicalMachine(numofCores, 0.001, 256000000000l,
					new Repository(5000000000000l, currid, (long) (pmBWRatio * 250000), (long) (pmBWRatio * 250000),
							(long) (pmBWRatio * 50000), latencyMapMachine, stTransitions, nwTransitions),
					89000, 29000, cpuTransitions);
			latencyMapRepo.put(currid, 5);
			latencyMapMachine.put(currid, 3);
			completePMList.add(pm);
		}

		// registering the hosts and the IaaS services
		iaas.bulkHostRegistration(completePMList);
		return iaas;
	}
}
