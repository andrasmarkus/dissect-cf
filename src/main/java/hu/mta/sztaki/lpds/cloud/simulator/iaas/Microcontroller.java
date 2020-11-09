package hu.mta.sztaki.lpds.cloud.simulator.iaas;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.PowerStateDelayer;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.State;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.StateChangeListener;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.notifications.SingleNotificationHandler;
import hu.mta.sztaki.lpds.cloud.simulator.notifications.StateDependentEventHandler;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.util.PowerTransitionGenerator;

public class Microcontroller extends PhysicalMachine {

	public static enum State {
		OFF, RUNNING, METERING
	};

	public static final EnumSet<State> StatesOfHighEnergyConsumption = EnumSet.of(State.RUNNING, State.METERING);

	private State currentState = State.OFF;

	public Microcontroller(double cores, double perCorePocessing, long memory, Repository disk, int onD, int offD,
			Map<String, PowerState> cpuPowerTransitions) {
		super(cores, perCorePocessing, memory, disk, onD, offD, cpuPowerTransitions);

		if (disk.getName() == "host") {
			cpuPowerTransitions = defaultTransitions("host");
		}
	}

	public Microcontroller(double cores, double perCorePocessing, long memory, Repository disk,
			double[] turnonOperations, double[] switchoffOperations, Map<String, PowerState> cpuPowerTransitions) {
		super(cores, perCorePocessing, memory, disk, turnonOperations, switchoffOperations, cpuPowerTransitions);

		if (disk.getName() == "host") {
			cpuPowerTransitions = defaultTransitions("host");
		}
	}

	public void metering() throws NetworkException {
		switch (currentState) {
		case OFF:
			break;
		case RUNNING:
			try {
				setState(State.METERING);
			} catch (NetworkException nex) {
				throw new RuntimeException(nex);
			}

			break;
		case METERING:
			System.err.println("WARNING: an already metering MC was tasked to meter!");
			break;
		}
	}

	public interface StateChangeListener {
		void stateChanged(Microcontroller mc, State oldState, State newState);
	}

	protected final StateDependentEventHandler<StateChangeListener, Pair<State, State>> stateListenerManager = new StateDependentEventHandler<Microcontroller.StateChangeListener, Pair<State, State>>(
			new SingleNotificationHandler<StateChangeListener, Pair<State, State>>() {
				@Override
				public void sendNotification(final StateChangeListener onObject, final Pair<State, State> states) {
					onObject.stateChanged(Microcontroller.this, states.getLeft(), states.getRight());
				}
			});

	private void setState(final State newState) throws NetworkException {
		try {
			localDisk.setState(NetworkNode.State.valueOf(newState.name()));
		} catch (IllegalArgumentException e) {
		}
		final State pastState = currentState;
		currentState = newState;
		directConsumerUsageMoratory = newState != State.RUNNING;
		stateListenerManager.notifyListeners(Pair.of(pastState, newState));

		// Power state management:
		setCurrentPowerBehavior(PowerTransitionGenerator.getPowerStateFromMap(hostPowerBehavior, newState.toString()));
	}

	private static Map<String, PowerState> defaultTransitions(String type) {
		double minpower = 20;
		double idlepower = 200;
		double maxpower = 300;
		double diskDivider = 10;
		double netDivider = 20;
		EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> transitions = null;
		try {
			transitions = PowerTransitionGenerator.generateTransitions(minpower, idlepower, maxpower, diskDivider,
					netDivider);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		if (type.equals("host")) {
			return transitions.get(PowerTransitionGenerator.PowerStateKind.host);
		}

		return null;
	}

}
