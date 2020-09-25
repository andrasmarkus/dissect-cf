package hu.u_szeged.inf.fog.simulator.demo;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.energy.specialized.PhysicalMachineEnergyMeter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.mta.sztaki.lpds.cloud.simulator.util.PowerTransitionGenerator;

public class Test {


	public static void main(String[] args) throws Exception{
		HashMap<String, Integer> latencyMap = new HashMap<String, Integer>();
		
		final EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> transitions = PowerTransitionGenerator
				.generateTransitions(10, 200, 300, 5, 5);
		final Map<String, PowerState> cpuTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.host);
		final Map<String, PowerState> stTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.storage);
		final Map<String, PowerState> nwTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.network);
		
		Repository pmr = new Repository(100, "pmr", 100, 100, 100, latencyMap, stTransitions, nwTransitions);
		PhysicalMachine pm = new PhysicalMachine(100, 100, 100, pmr, 100, 100, cpuTransitions);
		
		pm.turnon();
		PhysicalMachineEnergyMeter pme  = new PhysicalMachineEnergyMeter(pm);
		pme.startMeter(10, false);
		VirtualAppliance va = new VirtualAppliance("va", 1, 0, false, 1);
		pmr.registerObject(va);
		MDC mdc = new MDC(10, pme);
		mdc.start();
		
		Timed.simulateUntil(100);
		
		System.out.println("vm");
		VirtualMachine vm = pm.requestVM(va, pm.getCapacities(), pmr, 1)[0];
		
		Timed.simulateUntil(100);
		
		ConsumptionEventAdapter ce = new ConsumptionEventAdapter();
						
		System.out.println("task");
		vm.newComputeTask(10, ResourceConsumption.unlimitedProcessing, ce);

		Timed.simulateUntil(200);
		
		System.out.println("shutdown");
		vm.switchoff(true);
		
		Timed.simulateUntil(100);
		
		pm.switchoff(null);
		
		Timed.simulateUntilLastEvent();
		
		
	}
}

class MDC extends Timed {
	
	static ArrayList<Long> readingtime = new ArrayList<Long>();
	static ArrayList<Double> readingpm = new ArrayList<Double>();
	
	long freq;
	PhysicalMachineEnergyMeter pme;
	
	MDC(long freq, PhysicalMachineEnergyMeter pme){
		this.freq=freq;
		this.pme=pme;
	}

	public void start() {
		subscribe(freq);
		new DeferredEvent(400) {
			
			@Override
			protected void eventAction() {
				stop();
				pme.stopMeter();
			}
		};
	}

	public void stop() {
		unsubscribe();
	}

	@Override
	public void tick(final long fires) {
		System.out.println(fires+" "+pme.getTotalConsumption()+" "+pme.getObserved().getState());
	}
}