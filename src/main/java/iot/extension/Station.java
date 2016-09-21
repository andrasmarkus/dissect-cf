package iot.extension;

import java.util.ArrayList;
import java.util.HashMap;
import hu.mta.sztaki.lpds.cloud.simulator.io.*;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.*;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.State;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.*;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;

public class Station extends Timed {

	/**
	 * Kulon osztaly a Station fobb adatainak a konnyebb attekinthetoseg
	 * erdekeben
	 */
	public static class Stationdata {
		public long lifetime;
		public long starttime;
		public long stoptime;
		public int filesize;
		public int sensornumber;
		public long freq;
		public String name;
		public String torepo;
		public int ratio;

		/**
		 * @param lt
		 * @param st
		 * @param stt
		 * @param fs
		 * @param sn
		 * @param freq
		 * @param name
		 * @param torepo
		 * @param ratio
		 */
		public Stationdata(long lt, long st, long stt, int fs, int sn, long freq, String name, String torepo,
				int ratio) {
			this.lifetime = lt;
			this.starttime = st;
			this.stoptime = stt;
			this.filesize = fs;
			this.sensornumber = sn;
			this.freq = freq;
			this.name = name;
			this.torepo = torepo;
			this.ratio = ratio;
		}

		@Override
		public String toString() {
			return "name=" + name + ", lifetime=" + lifetime + ", starttime=" + starttime + ", stoptime=" + stoptime
					+ ", filesize=" + filesize + ", sensornumber=" + sensornumber + ", freq=" + freq + ", torepo="
					+ torepo + " ,ratio=" + ratio;
		}
	}

	public Stationdata sd;
	private Repository repo;
	private Repository torepo;
	private long reposize;
	private long time;
	private HashMap<String, Integer> lmap;
	private int lat;
	private int i;
	private VirtualMachine vm;
	private PhysicalMachine pm;
	private boolean isWorking;
	private boolean isMetering;
	public static ArrayList<Station> stations = new ArrayList<Station>();
	public long generatedfilesize;
	public static long allstationsize=0;

	/**
	 * @param maxinbw
	 *            a repo savszelessege : Long
	 * @param maxoutbw
	 *            a repo savszelessege : Long
	 * @param diskbw
	 *            a repo savszelessege : Long
	 * @param reposize
	 *            a repo merete: Long
	 * @param sd
	 *            station-t jellemzo adatok : Stationdata
	 */
	public Station(long maxinbw, long maxoutbw, long diskbw, long reposize, final Stationdata sd) {
		this.vm = null;
		this.i = 0;
		this.sd = sd;
		this.pm = this.findPm(sd.torepo);
		isWorking = sd.lifetime == -1 ? false : true;
		lmap = new HashMap<String, Integer>();
		lat = 11;
		lmap.put(sd.name, lat);
		lmap.put(sd.torepo, lat);
		this.reposize = reposize;
		repo = new Repository(this.reposize, sd.name, maxinbw, maxoutbw, diskbw, lmap);
		this.torepo = this.findRepo(sd.torepo);
		//this.startMeter(sd.freq); // ezt majd mashol kell meghivni, ideiglenes!!
	}

	public String getName() {
		return this.sd.name;
	}
	
	public Repository getRepo() {
		return repo;
	}

	public void setRepo(Repository repo) {
		this.repo = repo;
	}

	/**
	 * Ha sikeresen megerkezett a celrepo-ba a SO, torli a Station repojabol
	 */
	private class StorObjEvent implements ConsumptionEvent {
		private String so;

		private StorObjEvent(String soid) {
			this.so = soid;
		}

		private long Size() {
			String[] splited = so.split("\\s+");
			return Integer.parseInt(splited[1]);
		}

		@Override
		public void conComplete() {
			;
			repo.deregisterObject(this.so);
		}

		@Override
		public void conCancelled(ResourceConsumption problematic) {
			System.out.println("conCanelled meghivodott!");
		}
	}

	/**
	 * Elkuldi az object-eket a celreponak
	 * 
	 * @param r
	 *            a cel repo
	 * @throws NetworkException
	 */
	private void startCommunicate(Repository r) throws NetworkException {
		for (StorageObject so : repo.contents()) {
			StorObjEvent soe = new StorObjEvent(so.id);
			repo.requestContentDelivery(so.id, r, soe);
		}
	}

	/**
	 * Elinditja a Station mukodeset
	 * 
	 * @param interval
	 *            frekvencia
	 */
	public void startMeter(final long interval) {
		if (isWorking) {
			subscribe(interval);
			isMetering = true;
			this.time=Timed.getFireCount();
		}
	}

	/**
	 * leallitja a Station mukodeset
	 */
	private void stopMeter() {
		isWorking = false;
		unsubscribe();
	}

	/**
	 * megkeresi a cel repository-t az iaas felhoben
	 * 
	 * @param torepo
	 *            a cel repo azonositoja
	 * @return
	 */
	private Repository findRepo(String torepo) {
		Repository r = null;
		for (Repository tmp : Cloud.iaas.repositories) {
			if (tmp.getName().equals(torepo)) {
				r = tmp;
			} else {
				for (PhysicalMachine pm : Cloud.iaas.machines) {
					if (pm.localDisk.getName().equals(torepo)) {
						r = pm.localDisk;
					}
				}
			}
		}
		return r;
	}

	/**
	 * megkeresi a cel repository-t az iaas felhoben
	 * 
	 * @param torepo
	 *            a cel repo azonositoja
	 * @return
	 */
	private PhysicalMachine findPm(String torepo) {
		PhysicalMachine p = null;
		for (PhysicalMachine pm : Cloud.iaas.machines) {
			if (pm.localDisk.getName().equals(torepo)) {
				p = pm;
			}
		}
		return p;
	}

	/**
	 * Ha letelt a Station mukodesenek ideje, leallitja azt Adatot gyujt &
	 * elkuldi a reponak
	 */
	@Override
	public void tick(long fires) {
		// a meres a megadott ideig tart csak
		if (Timed.getFireCount() < (sd.lifetime+this.time) && Timed.getFireCount() >= (sd.starttime+this.time)
				&& Timed.getFireCount() <= (sd.stoptime+this.time)) {
			for (int i = 0; i < sd.sensornumber; i++) {
				new Metering(sd.name, i, sd.filesize);
			}
		} else if (Timed.getFireCount() > (sd.stoptime+this.time)) {
			isMetering = false;
		}
		// de a station mukodese addig amig az osszes SO el nem lett kuldve
		if (this.repo.getFreeStorageCapacity() == reposize && Timed.getFireCount() > (sd.lifetime+this.time)) {
			this.stopMeter();
		}

		// kozponti tarolo a cel repo
		if (Cloud.iaas.repositories.contains(this.torepo)) {
			// megkeresi a celrepo-t es elkuldeni annak
			try {
				if (this.torepo != null) {
					if ((this.repo.getMaxStorageCapacity() - this.repo.getFreeStorageCapacity()) >= sd.ratio
							* sd.filesize || isMetering == false) {
						this.startCommunicate(this.torepo);
					}
				} else {
					System.out.println("Nincs kapcsolat a repo-k kozott!");
				}
			} catch (NetworkException e) {
				e.printStackTrace();
			}
		}
		// share nothing felho
		else {
			if (this.pm.getState().equals(State.RUNNING) && this.i == 0) {
				i++;
				try {
					if (!this.pm.isHostingVMs()) {
						this.vm = this.pm.requestVM(Cloud.getVa(), Cloud.getArc(),
								Cloud.iaas.repositories.get(0), 1)[0];
					} else {
						this.vm = this.pm.listVMs().iterator().next();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// megkeresi a celrepo-t es elkuldeni annak
			try {
				if ((this.repo.getMaxStorageCapacity() - this.repo.getFreeStorageCapacity()) >= sd.ratio * sd.filesize
						|| isMetering == false) {
					if (this.vm != null) {
						if (vm.getState().equals(VirtualMachine.State.RUNNING)) {
							//System.out.println("kuldve: "+fires);
							//System.out.println(this.pm.getCapacities() + " vm futasakor " + this.torepo.getName());
							this.startCommunicate(vm.getResourceAllocation().getHost().localDisk);
						}
					}
				}
			} catch (NetworkException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * toString metodus a Station lenyeges adatainak kiiratashoz,debugolashoz
	 */
	@Override
	public String toString() {
		return "Station [" + sd + ", reposize:" + this.repo.getMaxStorageCapacity() + ",fajlmeret "
				+ this.generatedfilesize + "]";
	}
}
