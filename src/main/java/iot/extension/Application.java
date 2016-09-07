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

	private static class VmCollector {
		public VirtualMachine vm;
		public boolean isworking;

		public VmCollector(VirtualMachine vm, boolean isworking) {
			this.vm = vm;
			this.isworking = isworking;
		}
	}

	public static Application app;
	private VirtualAppliance va;
	private AlterableResourceConstraints arc = new AlterableResourceConstraints(2, 0.001, 160L);
	private ArrayList<VmCollector> vmlist;
	private int print;
	private static long allgenerateddatasize = 0;
	private static long localfilesize = 0;
	private VmCollector vml;

	public static Application getInstance(final long freq, VirtualAppliance va, int print) {
		if (app == null) {
			app = new Application(freq, va, print);
		} else {
			System.out.println("you can't create a second app!");
		}
		return Application.app;
	}

	private Application(final long freq, VirtualAppliance va, int print) {
		subscribe(freq);
		this.print = print;
		this.vmlist = new ArrayList<VmCollector>();
		if (va == null) {
			this.va = Cloud.getVa();
		} else {
			this.va = va;
			Cloud.iaas.repositories.get(1).registerObject(this.va);
		}
	}

	/**
	 * it searches the first vm which doesnt have work
	 * 
	 * @return
	 */

	private VmCollector VmSearch() {
		VmCollector vmc = null;
		for (int i = 0; i < this.vmlist.size(); i++) {
			if (this.vmlist.get(i).isworking == false) {
				vmc = this.vmlist.get(i);
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
			b = this.vmlist.add(new VmCollector(
					Cloud.iaas.requestVM(Cloud.getVa(), this.arc, Cloud.iaas.repositories.get(0), 1)[0], false));
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
		// ha erkezett mar adat vmelyik stationbol inditsunk a beerkezett adat
		// fuggvenyeben vm-et & feladatot
		if (Station.allstationsize > 0) {
			// ha meg nincs vm-unk egyaltalan
			if (this.vmlist.isEmpty()) {
				this.generateAndAddVM();
			}
			else {
				vml = this.VmSearch();
				if (vml == null) {
					this.generateAndAddVM();
					vml = this.VmSearch();
				} else {
					if (vml.vm.getState().equals(VirtualMachine.State.RUNNING)) {
						if ((Station.allstationsize - Application.allgenerateddatasize) > 5000) {
							Application.localfilesize = 5000;
						} else {
							Application.localfilesize = (Station.allstationsize - Application.allgenerateddatasize); // feladathoz
						}
						// System.out.println(Application.localfilesize +" "+
						// Station.allstationsize +"
						// "+Application.allgenerateddatasize);
						try {
							if (Application.localfilesize != 0) {
								vml.vm.newComputeTask(Application.localfilesize * 10000,
										ResourceConsumption.unlimitedProcessing, new ConsumptionEventAdapter() {
											long i = Application.localfilesize;

											@Override
											public void conComplete() {
												if (print == 1) {
													System.out
															.println("VM computeTask has ended with " + i + " bytes.");
													vml.isworking = false;
												}
											}
										});
							}
						} catch (NetworkException e) {
							e.printStackTrace();
						}
						vml.isworking = true;
						Application.allgenerateddatasize += Application.localfilesize; // kilepesi
																						// feltetelhez
					}
				}
			}
		}
	}
}
