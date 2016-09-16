package iot.extension;

import java.util.ArrayList;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
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
	private static long allgenerateddatasize = 0;
	private static long localfilesize = 0;

	public static Application getInstance(final long freq, int print) {
		if (app == null) {
			app = new Application(freq, print);
		} else {
			System.out.println("you can't create a second app!");
		}
		return Application.app;
	}

	private Application(final long freq, int print) {
		subscribe(freq);
		this.print = print;
		Application.vmlist = new ArrayList<VmCollector>();
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
				if(Application.i==0){
					Application.i++;
					for(Station s: Station.stations){
						s.startMeter(s.sd.freq);
					}
				}
				return vmc;
			}
		}
		return vmc;
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

	@Override
	public void tick(long fires) {
		// kilepesi feltetel az app szamara
		if (Station.allstationsize == Application.allgenerateddatasize && Application.allgenerateddatasize != 0) {
			unsubscribe();
		}
		if (Application.vmlist.isEmpty()) {
			this.generateAndAddVM();
		} else {
			final VmCollector vml = this.VmSearch();
			if (vml == null) {
				this.generateAndAddVM();
			} else {
				if ((Station.allstationsize - Application.allgenerateddatasize) > 5000000) {
					Application.localfilesize = 5000000;
				} else {
					Application.localfilesize = (Station.allstationsize - Application.allgenerateddatasize); // feladathoz
				}
				try {
					//System.out.println(Station.allstationsize + " : " + Application.allgenerateddatasize+ " : "+ vml.vm+" : "+fires);
					if (Application.localfilesize != 0) {
						vml.isworking = true;
						vml.vm.newComputeTask(Application.localfilesize /* 10000 */
						, ResourceConsumption.unlimitedProcessing, new ConsumptionEventAdapter() {
							//long i = Application.localfilesize;

							@Override
							public void conComplete() {
								vml.isworking = false;
								vml.worked=true;
								vml.tasknumber++;
								if (print == 1) {
									//System.out.println("VM computeTask has ended with " + i + " bytes.");
									
								}
							}
						});
					}
				} catch (NetworkException e) {
					e.printStackTrace();
				}
				
				Application.allgenerateddatasize += Application.localfilesize; // kilepesi
																				// feltetelhez

			}
		}

	}
}
