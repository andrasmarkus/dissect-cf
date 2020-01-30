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
