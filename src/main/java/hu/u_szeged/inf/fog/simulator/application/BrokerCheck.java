package hu.u_szeged.inf.fog.simulator.application;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;

public class BrokerCheck extends DeferredEvent {

	private Application toApp;
	private Application fromApp;
	private long unprocessedData;
	private ComputingAppliance ca;
	long delay;
	
	public BrokerCheck(Application fromApp, Application toApp,  long unprocessedData, long delay) {
		super(delay);
		this.delay=delay;
		this.fromApp = fromApp;
		this.toApp = toApp;
		this.unprocessedData = unprocessedData;
	}

	@Override
	protected void eventAction() {

		if (toApp.broker.vm.getState().equals(VirtualMachine.State.RUNNING)) {
			final long unprocessed = unprocessedData;
			try {
				
				NetworkNode.initTransfer(unprocessedData, ResourceConsumption.unlimitedProcessing,
						fromApp.computingAppliance.iaas.repositories.get(0), toApp.computingAppliance.iaas.repositories.get(0), new ConsumptionEvent() {

							@Override
							public void conComplete() {
								toApp.sumOfArrivedData += unprocessed;
								toApp.incomingData--;
								Application.moveDataCapsule(fromApp, toApp, unprocessed, 1);
							}

							@Override
							public void conCancelled(ResourceConsumption problematic) {
							}

						});
			} catch (NetworkException e) {
				e.printStackTrace();
			} 
		}else {
			if(this.delay==1) {
				new BrokerCheck(this.fromApp,this.toApp,this.unprocessedData, 1);
			}else {
				new BrokerCheck(this.fromApp,this.toApp,this.unprocessedData, this.delay/2);
			}
			
		}

	}

}
