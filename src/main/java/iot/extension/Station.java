package iot.extension;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

import org.omg.Messaging.SyncScopeHelper;

import hu.mta.sztaki.lpds.cloud.simulator.io.*;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.util.PowerTransitionGenerator;
import hu.mta.sztaki.lpds.cloud.simulator.*;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.*;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;

public class Station extends Timed {
	
	/**
	 * Kulon osztaly a Station fobb adatainak a konnyebb attekinthetoseg erdekeben
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
		public Stationdata(long lt,long st,long stt,int fs,int sn,long freq,String name,String torepo,int ratio){
			this.lifetime=lt;
			this.starttime=st;
			this.stoptime=stt;
			this.filesize=fs;
			this.sensornumber=sn;
			this.freq=freq;
			this.name=name;
			this.torepo=torepo;		
			this.ratio=ratio;
		}
		@Override
		public String toString() {
			return "name=" + name + ", lifetime=" + lifetime + ", starttime="
					+ starttime + ", stoptime=" + stoptime + ", filesize=" + filesize + ", sensornumber=" + sensornumber
					+ ", freq=" + freq +  ", torepo=" + torepo + " ,ratio="+ratio;
		}
	}
	
	private Stationdata sd;
	private Repository repo;
	private Repository torepo;
	private long reposize;
	private HashMap<String, Integer> lmap;
	private int lat;
	private PhysicalMachine pm;
	private boolean isWorking;
	private boolean isMetering;
	public static ArrayList<Station> stations = new ArrayList<Station>();
	private static final double minpower = 20;
	private static final double idlepower = 200;
	private static final double maxpower = 300;
	private static final double diskDivider = 10;
	private static final double netDivider = 20;
	private static EnumMap<PhysicalMachine.PowerStateKind, EnumMap<PhysicalMachine.State, PowerState>> defaultTransitions;
	static {
		try {
			defaultTransitions = PowerTransitionGenerator.generateTransitions(minpower, idlepower, maxpower,
					diskDivider, netDivider);
		} catch (Exception e) {
			throw new IllegalStateException("Cannot initialize the default transitions");
		}
	}

	/**
	 * @param maxinbw a repo savszelessege : Long
	 * @param maxoutbw a repo savszelessege : Long
	 * @param diskbw a repo savszelessege : Long
	 * @param reposize a repo merete: Long
	 * @param sd station-t jellemzo adatok : Stationdata
	 */
	public Station(long maxinbw, long maxoutbw,long diskbw,long reposize,final Stationdata sd) {
		this.sd=sd;
		isWorking = sd.lifetime==-1 ? false : true;
		lmap = new HashMap<String, Integer>();
		lat = 11;
		lmap.put(sd.name, lat);
		lmap.put(sd.torepo, lat);
		this.reposize=reposize;
		repo = new Repository(this.reposize, sd.name, maxinbw, maxoutbw, diskbw, lmap);
		pm = new PhysicalMachine(8, 0.00155875, 8000000000L, repo, 89000, 29000, defaultTransitions);
		this.torepo = this.findRepo(sd.torepo);
		this.startMeter(sd.freq);	// ezt majd mashol kell meghivni, ideiglenes!!
	}
	
	public String getName(){
		return this.sd.name;
	}
	public Repository getRepo() {
		return repo;
	}

	public void setRepo(Repository repo) {
		this.repo = repo;
	}


	/**
	 *	Ha sikeresen megerkezett a celrepo-ba a SO, torli a Station repojabol
	 */
	private class StorObjEvent implements ConsumptionEvent {
		private String so;

		private StorObjEvent(String soid) {
			this.so = soid;
		}

		@Override
		public void conComplete() {
			repo.deregisterObject(this.so);

		}

		@Override
		public void conCancelled(ResourceConsumption problematic) {
			System.out.println("conCanelled meghivodott!");
		}
	}

	/**
	 * Elkuldi az object-eket a celreponak
	 * @param r a cel repo
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
	private void startMeter(final long interval) {
		if (isWorking) {
			subscribe(interval);
			isMetering = true;
		}			
	}

	/**
	 * leallitja a Station mukodeset
	 */
	private void stopMeter() {
		isWorking= false;
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
	 * Ha letelt a Station mukodesenek ideje, leallitja azt Adatot gyujt &
	 * elkuldi a reponak
	 */
	@Override
	public void tick(long fires) {
		// a meres a megadott ideig tart csak
		if (Timed.getFireCount() < sd.lifetime  && Timed.getFireCount()>=sd.starttime && Timed.getFireCount()<=sd.stoptime) {
			for (int i = 0; i < sd.sensornumber; i++) {
				new Metering(sd.name, i, sd.filesize);
			}
		}else if(Timed.getFireCount()>sd.stoptime){ 
			isMetering = false;
		}
		// de a station mukodese addig amig az osszes SO el nem lett kuldve
		if (this.repo.getFreeStorageCapacity() == reposize && Timed.getFireCount() > sd.lifetime) {
			this.stopMeter();
		}
		// megkeresi a celrepo-t es elkuldeni annak
		try {
			if (this.torepo!= null) { 
				if((this.repo.getMaxStorageCapacity()-this.repo.getFreeStorageCapacity())>=sd.ratio*sd.filesize || isMetering==false){
					this.startCommunicate(this.torepo);
				}
				
			} else {
				System.out.println("Nincs kapcsolat a repo-k kozott!");
			}
		} catch (NetworkException e) {
			e.printStackTrace();
		}
	}

	/**
	 * toString metodus a Station lenyeges adatainak kiiratashoz,debugolashoz
	 */
	@Override
	public String toString() {
		return "Station [" +sd+", reposize:" + this.repo.getMaxStorageCapacity() + "]";
	}
}
