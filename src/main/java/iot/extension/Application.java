package iot.extension;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.StateChangeException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import iot.extension.Application.VmCollector;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;

public class Application extends Timed {

	public static class VmCollector {
		public VirtualMachine vm;
		public boolean isworking;
		public int tasknumber;
		public boolean worked;
	
		public VmCollector(VirtualMachine vm, boolean isworking) {
			this.vm = vm;
			this.isworking = isworking;
			this.tasknumber=0;
			this.worked=false;
		}
	}
	private static int i=0;
	public static Application app;
	public static ArrayList<VmCollector> vmlist;
	private int print;
	private boolean delay;
	private static long allgenerateddatasize = 0;
	private static long localfilesize = 0;
	private static long temp;
	public static TreeMap<Long,Integer> hmap = new TreeMap();
	private static int feladatszam=0;

	
	public static Application getInstance(final long freq,boolean delay, int print) {
		if (app == null) {
			app = new Application(freq, delay,print);
		} else {
			System.out.println("you can't create a second app!");
		}
		return Application.app;
	}

	private Application(final long freq,boolean delay, int print) {
		subscribe(freq);
		this.print = print;
		Application.vmlist = new ArrayList<VmCollector>();
		this.delay=delay;
	}

	/**
	 * it searches the first vm which doesnt have work
	 * 
	 * @return
	 */
	private VmCollector VmSearch() {
		VmCollector vmc = null;
		for (int i = 0; i < Application.vmlist.size(); i++) {
			if (Application.vmlist.get(i).isworking == false
					&& Application.vmlist.get(i).vm.getState().equals(VirtualMachine.State.RUNNING)) {
				vmc = Application.vmlist.get(i);
				return vmc;
			}
		}
		return vmc;
	}

	
	private void startStation(){
		if(Application.i==0){
			Application.i++;
			System.out.println("Scenario started at: "+Timed.getFireCount());
			for(final Station s: Station.stations){
				Random randomGenerator = new Random();
				int randomInt = randomGenerator.nextInt(21);
				if(delay){
					new DeferredEvent((long)randomInt*60*1000) {
						
						@Override
						protected void eventAction() {
							s.startMeter(s.sd.freq);
						}
					};
				}
				else{
					s.startMeter(s.sd.freq);
				}
			}
		}
	}
	/**
	 * it makes a new VM with default false working variable
	 * 
	 * @return
	 */
	private boolean generateAndAddVM() {
		boolean b = false;
		try {
			b = Application.vmlist.add(new VmCollector(
					Cloud.iaas.requestVM(Cloud.getVa(), Cloud.getArc(), Cloud.iaas.repositories.get(0), 1)[0], false));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return b;
	}

	private boolean checkStationState(){
		boolean i=true;
		for(Station s : Station.stations){
			if(s.isSubscribed()){
				i=false;
			}
		}
		return i;
	}
	
	@Override
	public void tick(long fires) {
		
		if (Application.vmlist.isEmpty()) {
			this.generateAndAddVM(); //
		}
		if(this.VmSearch()!=null){
			this.startStation();
		}
		/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
		Application.localfilesize = (Station.allstationsize - Application.allgenerateddatasize); // tickben mennyi adatom erkezett feldolgozasra
		
		if(Application.localfilesize>0){ // ha van adat
			long processed = 0;
			while(Application.localfilesize!=processed){ //akkor addig inditsunk vm-eken feladatot
				if(Application.localfilesize-processed>250000){
					Application.temp=250000; // maximalis feldolgozott meret
				}else{
					Application.temp=(Application.localfilesize-processed);
				}
				processed+=Application.temp;
				/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
				//System.out.println(Application.temp+" : "+processed+ " : "+Application.localfilesize+" : "+fires);
				final VmCollector vml = this.VmSearch();
				if (vml == null) {
					this.generateAndAddVM();
				}else{
					try {
						final String printtart=vml.vm+" started at "+Timed.getFireCount();
						vml.isworking = true;
						Application.feladatszam++;
						vml.vm.newComputeTask(2400, ResourceConsumption.unlimitedProcessing, new ConsumptionEventAdapter() {
								long i = Timed.getFireCount();
								long ii = Application.temp;
								@Override
								public void conComplete() {
									vml.isworking = false;
									vml.worked=true;
									vml.tasknumber++;
									Application.feladatszam--;
									if (print == 1) {
										System.out.println(printtart+" finished at "+Timed.getFireCount()+ " with "+ii+" bytes,lasted "+(Timed.getFireCount()-i));
										
									}
									// kilepesi feltetel az app szamara
									if (Application.feladatszam==0 && checkStationState() && Station.allstationsize == Application.allgenerateddatasize && Application.allgenerateddatasize != 0) {
										unsubscribe();
										for(VmCollector vmcl : Application.vmlist){
											try {
												if(vmcl.vm.getState().equals(VirtualMachine.State.RUNNING)){
													vmcl.vm.switchoff(true);											
												}
											} catch (StateChangeException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
									}
								}
							});
						Application.allgenerateddatasize += Application.temp; // kilepesi
							// feltetelhez
						
					} catch (NetworkException e) {
						e.printStackTrace();
					}
				}
			}
		}
		int reqshutdown=0;
		for(VmCollector vmcl : Application.vmlist){
			if(vmcl.vm.getState().equals(VirtualMachine.State.RUNNING) &&  !vmcl.worked && !vmcl.isworking ){
				reqshutdown++;
			}
		}
		
			for(VmCollector vmcl : Application.vmlist){
				if(reqshutdown>1){
				if(vmcl.vm.getState().equals(VirtualMachine.State.RUNNING) &&  !vmcl.worked && !vmcl.isworking ){
					reqshutdown--;
					try {
						vmcl.vm.switchoff(true);
					} catch (StateChangeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		
		/* ------------------------------------*/
		int task=0;
		for(VmCollector vmcl : Application.vmlist){
			if(vmcl.tasknumber>0 && vmcl.worked && vmcl.isworking && vmcl.vm.getState().equals(VirtualMachine.State.RUNNING)){
			}
		}
		Application.hmap.put(Timed.getFireCount(), task);
	}
}

				

