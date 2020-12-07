package hu.u_szeged.inf.fog.simulator.physical;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
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

	private State currentState = Microcontroller.State.OFF;

	public Microcontroller(double cores, double perCorePocessing, long memory, Repository disk, int onD, int offD,
			Map<String, PowerState> cpuPowerTransitions) {
		super(cores, perCorePocessing, memory, disk, onD, offD, cpuPowerTransitions);
	}

	public Microcontroller(double cores, double perCorePocessing, long memory, Repository disk,
			double[] turnonOperations, double[] switchoffOperations, Map<String, PowerState> cpuPowerTransitions) {
		super(cores, perCorePocessing, memory, disk, turnonOperations, switchoffOperations, cpuPowerTransitions);
	}
	
	public State getMicrocontrollerState() {
		return this.currentState;
	}
	
	public boolean isRunning() {
		if (this.currentState == State.RUNNING) {
			return true;
		}
		
		return false;
	}
	
	public boolean isMetering() {
		if (this.currentState == State.METERING) {
			return true;
		}
		return false;
	}
	
	public boolean isSwitchedOff() {
		if (this.currentState == State.OFF) {
			return true;
		}
		
		return false;
	}
	
	public void setStateToRunning() {
		switch (this.currentState) {
		case OFF:
			this.turnon();
			break;
		case RUNNING:
			System.err.println("WARNING: an already running MC was tasked to switch on!");
		case METERING:
			try {
				this.setState(Microcontroller.State.RUNNING);
			} catch (NetworkException nex) {
				throw new RuntimeException(nex);
			}
			break;
		default:
			break;
		}
	}
	
	public void turnoff() {
		switch (this.currentState) {
		case OFF:
			System.err.println("WARNING: an already switched off MC was tasked to switch off!");
			break;
		case RUNNING:
			try {
				this.setState(Microcontroller.State.OFF);
			} catch (NetworkException nex) {
				throw new RuntimeException(nex);
			}
			break;
		case METERING:
			try {
				this.setState(Microcontroller.State.RUNNING);
			} catch (NetworkException nex) {
				throw new RuntimeException(nex);
			}
			
			this.turnoff();
			break;
		default:
			break;
		}
	}
	
	public void turnon() {
		switch (this.currentState) {
		case OFF:
			try {
				this.setState(Microcontroller.State.RUNNING);
			} catch (NetworkException nex) {
				throw new RuntimeException(nex);
			}

			break;
		case RUNNING:
			System.err.println("WARNING: an already running MC was tasked to switch on!");
		case METERING:
			break;
		default:
			break;
		}
	}

	public void metering() throws NetworkException {
		switch (this.currentState) {
		case OFF:
			break;
		case RUNNING:
			try {
				this.setState(Microcontroller.State.METERING);
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
