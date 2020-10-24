package hu.mta.sztaki.lpds.cloud.simulator.iaas;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import gnu.trove.list.linked.TDoubleLinkedList;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.ResourceAllocation;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.UnalterableConstraintsPropagator;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.MaxMinConsumer;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.MaxMinProvider;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceSpreader;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.notifications.SingleNotificationHandler;
import hu.mta.sztaki.lpds.cloud.simulator.notifications.StateDependentEventHandler;
import hu.mta.sztaki.lpds.cloud.simulator.util.PowerTransitionGenerator;

public class Microcontroller extends MaxMinProvider {
	
	public static final int defaultAllocLen = 1000;
	
	public static final int migrationAllocLen = 1000000;
	
	public static final double smallUtilization = 0.001;
	
	public static enum State {
		OFF,
		RUNNING,
		METERING
	};
	
	public static final EnumSet<State> ToRunningorHighEnergyConsumption = EnumSet.of(State.RUNNING, State.METERING);
	
	public static final EnumSet<State> TOOfforOff = EnumSet.of(State.OFF);
	
	public interface StateChangeListener {
		/**
		 * This function is called by the MC on subscribed objects when a MC's state
		 * changes. To simplify the implementation of the receiver objects, this state
		 * changed function actually propagates all state infromation (pre and post) as
		 * well as the microcontroller's reference which just went through the state
		 * change.
		 * 
		 * @param mc
		 *            the microcontroller that is involved in the state change
		 * @param oldState
		 *            the state the MC was in before this state change event was
		 *            delivered
		 * @param newState
		 *            the new state the MC will be in after the event is complete
		 */
		void stateChanged(Microcontroller mc, State oldState, State newState);
	}
	
	public class PowerStateDelayer extends ConsumptionEventAdapter {
		
		private State newState;
		
		final TDoubleLinkedList tasksDue;
		
		public final long transitionStart;
		
		ResourceConsumption currentConsumption = null;
		
		public PowerStateDelayer(final double[] tasklist, final State newPowerState) {
			onOffEvent = this;
			newState = newPowerState;
			tasksDue = new TDoubleLinkedList();
			tasksDue.add(tasklist);
			sendTask();
			transitionStart = Timed.getFireCount();
		}
		
		private void sendTask() {
			// Did we finish all the tasks for the state change?
			if (tasksDue.size() == 0) {
				// Mark the completion of the state change
				onOffEvent = null;
				class NetworkCausedStateDelay extends Timed {
					int infiniteLoopTest = 0;

					public NetworkCausedStateDelay() {
						tick(Timed.getFireCount());
					}

					@Override
					public void tick(long fires) {
						try {
							setState(newState);
							// Everything is fine we are good to go.
							unsubscribe();
						} catch (NetworkException nex) {
							// Some network activities stop the transition
							if (infiniteLoopTest++ > 10000) {
								throw new RuntimeException("Unsettling amount of network activity...");
							}
							long whenToCheckNext = localDisk.getLastEvent();
							if (whenToCheckNext > 0) {
								updateFrequency(whenToCheckNext + 1 - fires);
							}
						}
					}
				}
				new NetworkCausedStateDelay();
				return;
			}

			// No we did not, lets send some more to our direct consumer
			final double totalConsumption = tasksDue.removeAt(0);
			final double limit = tasksDue.removeAt(0);
			currentConsumption = new ResourceConsumption(totalConsumption, limit, directConsumer, Microcontroller.this,
					this);
			if (!currentConsumption.registerConsumption()) {
				throw new IllegalStateException(
						"PowerStateChange was not successful because resource consumption could not be registered");
			}
		}

		/**
		 * the notification handler when a task from the tasklist is complete
		 * 
		 * this handler actually sends the next task in.
		 */
		@Override
		public void conComplete() {
			sendTask();
		}
		
		@Override
		public void conCancelled(ResourceConsumption problematic) {
			throw new IllegalStateException("Unexpected termination of one of the state changing tasks");
		}
		
		public void addFurtherTasks(final double[] tasklist) {
			tasksDue.add(tasklist);
		}
		
		public void setNewState(State newState) {
			this.newState = newState;
		}
		
	}
	
	private final ConstantConstraints totalCapacities;
	
	private final AlterableResourceConstraints internalAvailableCaps;
	
	public final UnalterableConstraintsPropagator availableCapacities;
	
	private AlterableResourceConstraints promisedCapacities = AlterableResourceConstraints.getNoResources();
	
	private final AlterableResourceConstraints internalReallyFreeCaps;
	
	public final UnalterableConstraintsPropagator freeCapacities;
	
	public final Repository localDisk;
	
	private ResourceAllocation[] promisedResources = new ResourceAllocation[2];
	
	private int promisedAllocationsCount = 0;
	
	private State currentState = null;
	
	private final double[] onTransition;
	
	private final double[] offTransition;
	
	private final long onDelayEstimate;
	
	private final long offDelayEstimate;
	
	public final MaxMinConsumer directConsumer;
	
	public final Map<String, PowerState> hostPowerBehavior;
	
	private final StateDependentEventHandler<StateChangeListener, Pair<State, State>> stateListenerManager = new StateDependentEventHandler<Microcontroller.StateChangeListener, Pair<State, State>>(
			new SingleNotificationHandler<StateChangeListener, Pair<State, State>>() {
				@Override
				public void sendNotification(final StateChangeListener onObject, final Pair<State, State> states) {
					onObject.stateChanged(Microcontroller.this, states.getLeft(), states.getRight());
				}
			});
	
	private PowerStateDelayer onOffEvent = null;
	
	public Microcontroller(double cores, double perCorePocessing, long memory, Repository disk, int onD, int offD,
			Map<String, PowerState> cpuPowerTransitions) {
		this(cores, perCorePocessing, memory, disk,
				new double[] { onD * perCorePocessing * smallUtilization, perCorePocessing * smallUtilization },
				new double[] { offD * perCorePocessing * smallUtilization, perCorePocessing * smallUtilization },
				cpuPowerTransitions);
	}
	
	private long prepareTransitionalTasks(boolean on, double[] array) {
		final double[] writeHere = on ? onTransition : offTransition;
		System.arraycopy(array, 0, writeHere, 0, array.length);
		long odSum = 0;
		for (int i = 0; i < array.length; i += 2) {
			odSum += (long) (array[i] / array[i + 1]);
		}
		return odSum;
	}
	
	public Microcontroller(double cores, double perCorePocessing, long memory, Repository disk,
			double[] turnonOperations, double[] switchoffOperations, Map<String, PowerState> cpuPowerTransitions) {
		super(cores * perCorePocessing);
		if (cpuPowerTransitions == null) {
			throw new IllegalStateException("Cannot initialize physical machine without a complete power behavior set");
		}

		// Init resources:
		totalCapacities = new ConstantConstraints(cores, perCorePocessing, memory);
		internalAvailableCaps = new AlterableResourceConstraints(totalCapacities);
		availableCapacities = new UnalterableConstraintsPropagator(internalAvailableCaps);
		internalReallyFreeCaps = new AlterableResourceConstraints(totalCapacities);
		freeCapacities = new UnalterableConstraintsPropagator(internalReallyFreeCaps);
		localDisk = disk;

		hostPowerBehavior = Collections.unmodifiableMap(cpuPowerTransitions);
		onTransition = new double[turnonOperations.length];
		onDelayEstimate = prepareTransitionalTasks(true, turnonOperations);
		offTransition = new double[switchoffOperations.length];
		offDelayEstimate = prepareTransitionalTasks(false, switchoffOperations);

		try {
			setState(State.OFF);
		} catch (NetworkException e) {
			// This should never happen
			throw new RuntimeException(e);
		}
		directConsumer = new MaxMinConsumer(getPerTickProcessingPower());
	}
	
	private void actualSwitchOff() {
		try {
			switch (currentState) {
			case RUNNING:
				setState(State.OFF);
				new Timed() {
					@Override
					public void tick(final long fires) {
						if (State.OFF.equals(currentState)) {
							ResourceSpreader.FreqSyncer syncer = getSyncer();
							// Ensures that the switching off activities are only
							// started once all runtime activities complete for the
							// directConsumer
							if (syncer != null && syncer.isSubscribed()
									&& (underProcessing.size() + toBeAdded.size() - toBeRemoved.size() > 0)) {
								updateFrequency(syncer.getNextEvent() - fires + 1);
							} else {
								unsubscribe();
								new PowerStateDelayer(offTransition, State.OFF);
							}
						}
						// else: Another transition dropped the switchoff task. do
						// nothing
					}
				}.tick(Timed.getFireCount());
				break;
			case OFF:
			case METERING:
				setState(State.OFF);
				new Timed() {
					@Override
					public void tick(final long fires) {
						if (State.OFF.equals(currentState)) {
							ResourceSpreader.FreqSyncer syncer = getSyncer();
							// Ensures that the switching off activities are only
							// started once all runtime activities complete for the
							// directConsumer
							if (syncer != null && syncer.isSubscribed()
									&& (underProcessing.size() + toBeAdded.size() - toBeRemoved.size() > 0)) {
								updateFrequency(syncer.getNextEvent() - fires + 1);
							} else {
								unsubscribe();
								new PowerStateDelayer(offTransition, State.OFF);
							}
						}
						// else: Another transition dropped the switchoff task. do
						// nothing
					}
				}.tick(Timed.getFireCount());
				break;
			}
		} catch (NetworkException nex) {
			// Should not happen as long as the network node don't have a SWITCHINGOFF state
			throw new RuntimeException(nex);
		}

	}
	
	public boolean isRunning() {
		return currentState.equals(State.RUNNING);
	}
	
	public State getState() {
		return currentState;
	}
	
	public void turnon() {
		switch (currentState) {
		case OFF:
			try {
				setState(State.RUNNING);
			} catch (NetworkException nex) {
				// Should not happen as long as the networknode don't have a switchingon state
				throw new RuntimeException(nex);
			}

			if (onOffEvent == null) {
				new PowerStateDelayer(onTransition, State.RUNNING);
			} else {
				onOffEvent.addFurtherTasks(onTransition);
				onOffEvent.setNewState(State.RUNNING);
			}

			break;
		case RUNNING:
			break;
		case METERING:
			break;
		}
	}
	
	public boolean isHostableRequest(final ResourceConstraints requested) {
		return requested.compareTo(totalCapacities) <= 0;
	}
	
	public boolean isReHostableRequest(final ResourceConstraints requested) {
		return requested.compareTo(freeCapacities) <= 0;
	}
	
	@Override
	public String toString() {
		return "Microcontroller(S:" + currentState + " C:" + internalReallyFreeCaps.getRequiredCPUs() + " M:"
				+ internalReallyFreeCaps.getRequiredMemory() + " " + localDisk.toString() + " " + super.toString()
				+ ")";
	}
	
	public void subscribeStateChangeEvents(final StateChangeListener sl) {
		stateListenerManager.subscribeToEvents(sl);
	}
	
	public void unsubscribeStateChangeEvents(final StateChangeListener sl) {
		stateListenerManager.unsubscribeFromEvents(sl);
	}
	
	private void setState(final State newState) throws NetworkException {
		try {
			localDisk.setState(NetworkNode.State.valueOf(newState.name()));
			// Behaviour change propagated
		} catch (IllegalArgumentException e) {
			// No need to propagate behaviour change
		}
		final State pastState = currentState;
		currentState = newState;
		stateListenerManager.notifyListeners(Pair.of(pastState, newState));

		// Power state management:
		setCurrentPowerBehavior(PowerTransitionGenerator.getPowerStateFromMap(hostPowerBehavior, newState.toString()));
	}
	
	public ResourceConstraints getCapacities() {
		return totalCapacities;
	}
	
	public void metering() {
		switch (currentState) {
		case OFF:
			break;
		case RUNNING:
			try {
				setState(State.METERING);
			} catch (NetworkException nex) {
				throw new RuntimeException(nex);
			}

			if (onOffEvent == null) {
				new PowerStateDelayer(onTransition, State.METERING);
			} else {
				onOffEvent.addFurtherTasks(onTransition);
				onOffEvent.setNewState(State.METERING);
			}
		case METERING:
			break;
		}
	}
	

	
}
