package hu.mta.sztaki.lpds.cloud.simulator.iaas;

import java.util.EnumSet;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.State;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.util.PowerTransitionGenerator;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;

public class Microcontroller extends PhysicalMachine {
	
	public static enum State {
		OFF,
		RUNNING,
		METERING
	};
	
	public static final EnumSet<State> StatesOfHighEnergyConsumption = EnumSet.of(State.RUNNING, State.METERING);
	
	private State currentState = null;
	
	public Microcontroller(double cores, double perCorePocessing, long memory, Repository disk, int onD, int offD,
			Map<String, PowerState> cpuPowerTransitions) {
		super(cores, perCorePocessing, memory, disk, onD, offD, cpuPowerTransitions);
	}
	
	public Microcontroller(double cores, double perCorePocessing, long memory, Repository disk,
			double[] turnonOperations, double[] switchoffOperations, Map<String, PowerState> cpuPowerTransitions) {
		super(cores, perCorePocessing, memory, disk, turnonOperations, switchoffOperations, cpuPowerTransitions);
	}
	
	public void metering() throws NetworkException {
		switch (currentState) {
		case OFF:
			break;
		case RUNNING:
			setState(State.METERING);
		case METERING:
			break;
		}
	}

	private void setState(final State newState) throws NetworkException {
		try {
			localDisk.setState(NetworkNode.State.valueOf(newState.name()));
		} catch (IllegalArgumentException e) {
		}

		currentState = newState;
		directConsumerUsageMoratory = newState != State.RUNNING;
		//stateListenerManager.notifyListeners(Pair.of(pastState, newState));

		// Power state management:
		setCurrentPowerBehavior(PowerTransitionGenerator.getPowerStateFromMap(hostPowerBehavior, newState.toString()));
	}



}
