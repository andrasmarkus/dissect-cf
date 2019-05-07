/*
 *  ========================================================================
 *  DIScrete event baSed Energy Consumption simulaTor 
 *    					             for Clouds and Federations (DISSECT-CF)
 *  ========================================================================
 *  
 *  This file is part of DISSECT-CF.
 *  
 *  DISSECT-CF is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or (at
 *  your option) any later version.
 *  
 *  DISSECT-CF is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *  General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with DISSECT-CF.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  (C) Copyright 2019, Andras Markus (markusa@inf.u-szeged.hu)
 *  (C) Copyright 2019, Jozsef Daniel Dombi (dombijd@inf.u-szeged.hu)
 */

package hu.uszeged.inf.iot.simulator.entities;

import java.util.Collections;
import java.util.Random;
import java.util.Vector;
import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.uszeged.inf.iot.simulator.pliant.FuzzyIndicators;
import hu.uszeged.inf.iot.simulator.pliant.Kappa;
import hu.uszeged.inf.iot.simulator.pliant.Sigmoid;
import hu.uszeged.inf.iot.simulator.system.Application;

/**
 * The goal of this class to pair a device to an application. Four default strategy has been implemented, 
 * but you can implement your strategy, for this, you should implement the InstallionStrategy interface. 
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 * @author Jozsef Daniel Dombi (dombijd@inf.u-szeged.hu)
 */
public interface InstallionStrategy{
	
	/**
	 * This method needs to be overrode. It should pair an application with a device.
	 * @param s The device which to be paired.
	 */
	public void install(final Device s);
}

/**
 * Random strategy randomly chooses one available application.
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
class RandomStrategy implements InstallionStrategy{

	public RandomStrategy(Device s) {
		this.install(s);
	}
	@Override
	public void install(Device s) {
		Random randomGenerator = new Random();
		int rnd = randomGenerator.nextInt(Application.getApplications().size());
		Application.addStation(s, Application.getApplications().get(rnd));
		
		Device.lmap.put(s.getDn().getLocalRepository().getName(), Device.LATENCY);
		Device.lmap.put(s.app.getCloud().getIaas().repositories.get(0).getName(), Device.LATENCY);
		
		if(!s.app.isSubscribed()) {
			s.app.restartApplication();
		}
		
	}
}

/**
 * Cost strategy chooses the cheapest available application.
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
class CostStrategy implements InstallionStrategy{

	public CostStrategy(Device d) {
		this.install(d);
	}
	@Override
	public void install(Device s) {
		 double min=Integer.MAX_VALUE-1.0;
		 int choosen=-1;
		for(int i=0;i<Application.getApplications().size();++i){
			if(Application.getApplications().get(i).getInstance().getPricePerTick()<min){
				min = Application.getApplications().get(i).getInstance().getPricePerTick();
				choosen = i;
			}
		}
		Application.addStation(s, Application.getApplications().get(choosen));
		Device.lmap.put(s.getDn().getLocalRepository().getName(), Device.LATENCY);
		Device.lmap.put(s.app.getCloud().getIaas().repositories.get(0).getName(), Device.LATENCY);
		
		if(!s.app.isSubscribed()) {
			s.app.restartApplication();
		}
	}
	
}

/**
 * Runtime strategy orders the application based on the ratio of connected devices and the size of the physical resources,
 * and picks the first one.
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
class RuntimeStrategy implements InstallionStrategy{
	
	public RuntimeStrategy(Device s) {
		this.install(s);
	}
	
	@Override
	public void install(final Device s) {
		new DeferredEvent(s.startTime) {
			
			@Override
			protected void eventAction() {
				double min = Double.MAX_VALUE-1.0;
				int choosen = -1;
				for(int i=0;i< Application.getApplications().size();i++ ){
					double loadRatio = (Application.getApplications().get(i).getStations().size())/(Application.getApplications().get(i).getCloud().getIaas().machines.size());
					if(loadRatio<min){
						min=loadRatio;
						choosen = i;
					}
				}
				Application.addStation(s, Application.getApplications().get(choosen));
				Device.lmap.put(s.getDn().getLocalRepository().getName(), Device.LATENCY);
				Device.lmap.put(s.app.getCloud().getIaas().repositories.get(0).getName(), Device.LATENCY);
				
				if(!s.app.isSubscribed()) {
					s.app.restartApplication();
				}
			}
		};
		
		
	}
	
}

/**
 * This strategy uses the pliant theorem.
 * @author Jozsef Daniel Dombi (dombijd@inf.u-szeged.hu)
 *
 */
class FuzzyStrategy implements InstallionStrategy{
	Device d;
	public FuzzyStrategy(Device s) {
		this.d=s;
		this.install(s);
	}
	@Override
	public void install(final Device s) {
		new DeferredEvent(s.startTime) {
			
			@Override
			protected void eventAction() {
				int rsIdx = fuzzyDecision(d);
				Application.addStation(d, Application.getApplications().get(rsIdx));
				Device.lmap.put(d.getDn().getLocalRepository().getName(), Device.LATENCY);
				Device.lmap.put(d.app.getCloud().getIaas().repositories.get(0).getName(), Device.LATENCY);
				if(!s.app.isSubscribed()) {
					s.app.restartApplication();
				}
				
			}
			
			
		};		
	}
	
private int fuzzyDecision(Device s) {
		
		Kappa kappa = new Kappa(3.0,0.4);

		Sigmoid<Object> sig = new Sigmoid<Object>(Double.valueOf(-1.0/96.0), Double.valueOf(15));
		Vector<Double> price = new Vector<Double>();
		for(int i=0;i<Application.getApplications().size();++i){
			price.add(kappa.getAt(sig.getat(Application.getApplications().get(i).getInstance().getPricePerTick()*1000000000)));

		}

		double minprice = Double.MAX_VALUE;
		double maxprice= Double.MIN_VALUE;
		for(int i=0;i<Application.getApplications().size();++i){
			double currentprice = Application.getApplications().get(i).getCurrentCostofApp();
			if(currentprice > maxprice)
				maxprice = currentprice;
			if(currentprice < minprice)
				minprice = currentprice;
		}
		
		
		Vector<Double> currentprice = new Vector<Double>();
		sig = new Sigmoid<Object>(Double.valueOf(-1.0), Double.valueOf((maxprice-minprice)/2.0));
		for(int i=0;i<Application.getApplications().size();++i){
			currentprice.add(kappa.getAt(sig.getat(Application.getApplications().get(i).getCurrentCostofApp())));
		}
		
	
		double minworkload = Double.MAX_VALUE;
		double maxworkload= Double.MIN_VALUE;
		for(int i=0;i<Application.getApplications().size();++i){
			double workload = Application.getApplications().get(i).getLoadOfCloud();
			if(workload > maxworkload)
				maxworkload = workload;
			if(workload < minworkload)
				minworkload = workload;
		}
		
		Vector<Double> workload = new Vector<Double>();
		sig = new Sigmoid<Object>(Double.valueOf(-1.0), Double.valueOf(maxworkload));
		for(int i=0;i<Application.getApplications().size();++i){
			workload.add(kappa.getAt(sig.getat(Application.getApplications().get(i).getLoadOfCloud())));

		}
	
		Vector<Double> numberofvm = new Vector<Double>();
		sig = new Sigmoid<Object>(Double.valueOf(-1.0/8.0),Double.valueOf(3));
		for(int i=0;i<Application.getApplications().size();++i){			
			numberofvm.add(kappa.getAt(sig.getat(Double.valueOf(Application.getApplications().get(i).getVmlist().size()))));
		}

		double sum_stations = 0.0;
		for(int i=0;i<Application.getApplications().size();++i){			
			sum_stations += Application.getApplications().get(i).getStations().size();
		}
		
		Vector<Double> numberofstation = new Vector<Double>();
		sig = new Sigmoid<Object>(Double.valueOf(-0.125),Double.valueOf(sum_stations/(Application.getApplications().size())));
		for(int i=0;i<Application.getApplications().size();++i){		
			numberofstation.add(kappa.getAt(sig.getat(Double.valueOf(Application.getApplications().get(i).getStations().size()))));
		}
		
		Vector<Double> numberofActiveStation = new Vector<Double>();
		for(int i=0;i<Application.getApplications().size();++i){		
			double sum = 0.0;
			for(int j=0;j<Application.getApplications().get(i).getStations().size();j++) {
				Station stat = (Station) Application.getApplications().get(i).getStations().get(j);
				long time = Timed.getFireCount();
				if(stat.startTime >= time && stat.stopTime >= time)
					sum +=1;
			}	
			numberofActiveStation.add(sum);
		}
		sum_stations = 0.0;
		for(int i=0;i<numberofActiveStation.size();++i){			
			sum_stations += numberofActiveStation.get(i);
		}
		
		sig = new Sigmoid<Object>(Double.valueOf(-0.125),Double.valueOf(sum_stations/(numberofActiveStation.size())));
		for(int i=0;i<numberofActiveStation.size();++i){
			double a = numberofActiveStation.get(i);
			double b = sig.getat(a);
			double c = kappa.getAt(b);
			numberofActiveStation.set(i, c);
		}
		
		Vector<Double> preferVM = new Vector<Double>();
		sig = new Sigmoid<Object>(Double.valueOf(1.0/32),Double.valueOf(3));
		for(int i=0;i<Application.getApplications().size();++i){
			preferVM.add(kappa.getAt(sig.getat(Double.valueOf(Application.getApplications().get(i).getInstance().getArc().getRequiredCPUs()))));
		}
		
		Vector<Double> preferVMMem = new Vector<Double>();
		sig = new Sigmoid<Object>(Double.valueOf(1.0/256.0),Double.valueOf(350.0));
		for(int i=0;i<Application.getApplications().size();++i){	
			preferVMMem.add(kappa.getAt(sig.getat(Double.valueOf(Application.getApplications().get(i).getInstance().getArc().getRequiredMemory() / 10000000))));
		}
		
		Vector<Double> score = new Vector<Double>();
		for(int i=0;i<price.size();++i){
			Vector<Double> temp = new Vector<Double>();
			temp.add(price.get(i));
			temp.add(numberofstation.get(i));
			temp.add(numberofActiveStation.get(i));
			temp.add(preferVM.get(i));
			temp.add(workload.get(i));
			temp.add(currentprice.get(i));

			score.add(FuzzyIndicators.getAggregation(temp)*100);
		}
		Vector<Integer> finaldecision = new Vector<Integer>();
		for(int i=0;i<Application.getApplications().size();++i){
			finaldecision.add(i);	
		}
		for(int i=0;i<score.size();++i){
			for(int j = 0; j< score.get(i); j++) {
				finaldecision.add(i);
			}
		}
		Random rnd = new Random();
		Collections.shuffle(finaldecision);			
		int temp = rnd.nextInt(finaldecision.size());
		return finaldecision.elementAt(temp);		
		
		
	}
}