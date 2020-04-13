package hu.u_szeged.inf.fog.simulator.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;
import hu.u_szeged.inf.fog.simulator.pliant.FuzzyIndicators;
import hu.u_szeged.inf.fog.simulator.pliant.Kappa;
import hu.u_szeged.inf.fog.simulator.pliant.Sigmoid;

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
			//possible cas in the list.
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
				
				
				//0-100%. smaller better 
				System.out.println("resource usage(%): "+ca.getloadOfResource());
				
				//smaller better
				System.out.println("unprocessed data / tasksize: "+(ca.applicationList.get(0).sumOfArrivedData - ca.applicationList.get(0).sumOfProcessedData)/ca.applicationList.get(0).taskSize);
				
				// GPS coordinate. //closer is better
				System.out.println("distance: "+a.computingAppliance.calculateDistance(ca));
				
				//unique but static. (smaller is better)
				System.out.println("latency: "+a.computingAppliance.iaas.repositories.get(0).getLatencies().get(ca.iaas.repositories.get(0).getName()));
				
				
				// (smaller better)
				System.out.println("num of devices: "+ca.applicationList.get(0).deviceList.size());
				//from the beginning smaller is better
				System.out.println("currentCost: "+ca.applicationList.get(0).getCurrentCost());
				// average price.
				System.out.println("price per tick: "+ca.applicationList.get(0).instance.getPricePerTick());
				
				//different for each layer. (larger is better)
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



class FuzzyApplicationStrategy extends ApplicationStrategy {

	public FuzzyApplicationStrategy(Application a) {
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
		
			//min max device size
			int deviceMin = Integer.MAX_VALUE;
			int deviceMax = Integer.MIN_VALUE;
			double MinPrice = Double.MAX_VALUE;
			double MaxPrice = Double.MIN_VALUE;
			
			for(int i=0;i<caList.size();i++) {
				ComputingAppliance ca = caList.get(i);
				int deviceSize = ca.applicationList.get(0).deviceList.size();
				if(deviceSize < deviceMin)
					deviceMin = deviceSize;
				if(deviceSize > deviceMax)
					deviceMax = deviceSize;
				
				double priceperTick = ca.applicationList.get(0).instance.getPricePerTick()*100000000;
				if(priceperTick < MinPrice)
					MinPrice = priceperTick;
				if(priceperTick > MaxPrice)
					MaxPrice = priceperTick;
			}	
			
			Vector<Double> loadOfResource = new Vector<Double>();
			Vector<Double> loadOfDevices = new Vector<Double>();
			Vector<Double> price = new Vector<Double>();
			for(int i=0;i<caList.size();i++) {
				
				ComputingAppliance ca = caList.get(i);
				Kappa kappa = new Kappa(3.0, 0.4);				
				Sigmoid<Object> sig = new Sigmoid<Object>(Double.valueOf(- 1.0 / 16.0), Double.valueOf(50));
				loadOfResource.add(sig.getat(ca.getloadOfResource()));
				
				
				sig = new Sigmoid<Object>(Double.valueOf(- 1.0 / 64.0), Double.valueOf((deviceMax-((deviceMax-deviceMin)/2.0))));
				loadOfDevices.add(sig.getat(new Double(ca.applicationList.get(0).deviceList.size())));
				
				System.out.println(ca.applicationList.get(0).instance.getPricePerTick()*100000000);
				
				sig = new Sigmoid<Object>(Double.valueOf(- 1.0 / 2.0), Double.valueOf((MaxPrice)));
				price.add(sig.getat(ca.applicationList.get(0).instance.getPricePerTick()*100000000));
			}
			
			Vector < Integer > score = new Vector < Integer > ();
	        for (int i = 0; i < caList.size(); ++i) {
	            Vector < Double > temp = new Vector < Double > ();
	            temp.add(loadOfResource.get(i));
	            temp.add(loadOfDevices.get(i));
	            temp.add(price.get(i));
	            score.add((int)(FuzzyIndicators.getAggregation(temp) * 100));
	        }
	        
	        Vector < Integer > finaldecision = new Vector < Integer > ();
	        for (int i = 0; i < caList.size(); ++i) {
	            finaldecision.add(i);
	        }
	            
	        
	        for (int i = 0; i < score.size(); ++i) {
	            for (int j = 0; j < score.get(i); j++) {
	                finaldecision.add(i);
	            }
	        }
	        Random rnd = new Random();
	        Collections.shuffle(finaldecision);
	        int temp = rnd.nextInt(finaldecision.size());
			
	        a.strategyApplication = caList.get(finaldecision.get(temp)).applicationList.get(0);
	        
	        //question
	        
	        //mindig csak egy van, a 0. kell visszaadni.
	        //a.strategyApplication = caList.get(temp).applicationList.get(0);
	        
			
			//result
			/// switch to another application
			//a.strategyApplication = caList.get(XXX);
			
			//remain the same application
			//a.strategyApplication = null;
		}
	}

}
