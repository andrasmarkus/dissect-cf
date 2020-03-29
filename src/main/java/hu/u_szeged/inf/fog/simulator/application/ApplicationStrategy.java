package hu.u_szeged.inf.fog.simulator.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;

public abstract class ApplicationStrategy {

	public abstract void install(Application a);

}

class RandomApplicationStrategy extends ApplicationStrategy {

	public RandomApplicationStrategy(Application a) {
		this.install(a);
	}
	@Override
	public void install(Application a) {
		int rnd1, rnd2;
		ArrayList<ComputingAppliance> caList = new ArrayList<ComputingAppliance>();
		caList.addAll(a.computingAppliance.neighbourList);
		if(a.computingAppliance.parentNode!=null) {
			caList.add(a.computingAppliance.parentNode);
		}
		if(caList.size()>0) {
			Random randomGenerator = new Random();
			rnd1 = randomGenerator.nextInt(caList.size());
			rnd2 = randomGenerator.nextInt(caList.get(rnd1).applicationList.size());
			a.strategyApplication=caList.get(rnd1).applicationList.get(rnd2);
		}
	}   


}

class LoadApplicationStrategy extends ApplicationStrategy{
	
	public LoadApplicationStrategy(Application a) {
		this.install(a);
	}

	@Override
	public void install(Application a) {
		
		ArrayList<ComputingAppliance> caList = new ArrayList<ComputingAppliance>();
		caList.addAll(a.computingAppliance.neighbourList);
		if(a.computingAppliance.parentNode!=null) {
			caList.add(a.computingAppliance.parentNode);
		}
		if(caList.size()>0) {
			ComputingAppliance chosen=caList.get(0);
			
			for(ComputingAppliance ca :  caList) {
				int lat1 = chosen.iaas.repositories.get(0).getLatencies().get(ca.iaas.repositories.get(0).getName());
				int lat2 = ca.iaas.repositories.get(0).getLatencies().get(ca.iaas.repositories.get(0).getName());
				if(chosen.getloadOfResource()>ca.getloadOfResource() && lat2<lat1) {
					chosen = ca;
				}
				
				/*
				if(chosen.getloadOfResource()>ca.getloadOfResource()) {
					chosen = ca;
				}
				
				System.out.println("resource usage(%): "+ca.getloadOfResource());
				System.out.println("unprocessed data / tasksize: "+(ca.applicationList.get(0).sumOfArrivedData - ca.applicationList.get(0).sumOfProcessedData)/ca.applicationList.get(0).taskSize);
				System.out.println("distance: "+a.computingAppliance.calculateDistance(ca));
				System.out.println("latency: "+a.computingAppliance.iaas.repositories.get(0).getLatencies().get(ca.iaas.repositories.get(0).getName()));
				System.out.println("num of devices: "+ca.applicationList.get(0).deviceList.size());
				System.out.println("currentCost: "+ca.applicationList.get(0).getCurrentCost());
				System.out.println("price per tick: "+ca.applicationList.get(0).instance.getPricePerTick());
				System.out.println("VM CPUs: "+ca.applicationList.get(0).instance.getArc().getRequiredCPUs());
				*/
			}
			a.strategyApplication=chosen.applicationList.get(0);
		}
		
		
	}
	
}

class HoldDownApplicationStrategy extends ApplicationStrategy {

	public HoldDownApplicationStrategy(Application a) {
		this.install(a);
	}
	@Override
	public void install(Application a) {
		a.strategyApplication=null;
	}   


}

class PushUpApplicationStrategy extends ApplicationStrategy {

	public PushUpApplicationStrategy(Application a) {
		this.install(a);
	}
	@Override
	public void install(Application a) {
		int rnd1;
		ArrayList<ComputingAppliance> caList = new ArrayList<ComputingAppliance>();
		if(a.computingAppliance.parentNode!=null) {
			caList.add(a.computingAppliance.parentNode);
		}
		if(caList.size()>0) {
			Random randomGenerator = new Random();
			rnd1 = randomGenerator.nextInt(caList.get(0).applicationList.size());
			a.strategyApplication=caList.get(0).applicationList.get(rnd1);
		}
	}   


}
