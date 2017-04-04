package iot.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import hu.mta.sztaki.lpds.cloud.simulator.io.*;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.*;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.State;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.*;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;

/**
 * This class simulates one of the IoT world's entity which is the smart device. Behavior of the class 
 * depends on time (as recurring event).
 * Az osztaly szimulalja egy IoT okos eszkoz mukodeset. A szimulacio soran idotol fuggo, visszatero esemenykent mukodik.
 */
public class Station extends Timed {

	/**
	 * This class helps to handle the attributon of station because for transparency and instantiation.
	 * Kulon osztaly a Station fobb adatainak a konnyebb attekinthetoseg es peldanyositas erdekeben.
	 */
	public static class Stationdata {
		
		/**
		 *
		 * The final time when the station send data.
		 * Az utolso ido, mikor a station meg adatot fog kuldeni.
		 */
		private long lifetime;
		
		/**
		 *  The time when the station starts generating and sending data.	
		 *  Az az ido, mikor a station elkezdi az adatgeneralast es az adatkuldest.
		 */
		private long starttime;
		
		/**
		 *  The time when the station stops generating and sending data.	
		 *  Az az ido, mikor a station befejezi az adatgeneralast es az adatkuldest.
		 */
		private long stoptime;
		
		/**
		 *  Size of the generated data.
		 *  A generalt adatmerete.
		 */
		private int filesize;
		
		/**
		 *  Number of the station's sensors.
		 *  A station szenzorjainak a szama.
		 */
		private int sensornumber;
		
		/**
		 *  The frequncy which tells the time between generating-sending data.
		 *  A frekvencia, amely megmondja mennyi ido teljen el 2 adatgeneralas es adatkuldes kozott.
		 */
		private long freq;
		
		/**
		 * Name ID of the station.
		 * Station azonosito.
		 */
		private String name;
		
		/**
		 * Name ID of the repository which will receive the data
		 * A repository ID, amelyik fogadja az adatot.
		 */
		private String torepo;
		
		/**
		 *  It tells how much unit of data will be locally store before sending.
		 *  Arany, amely megmondja, hogy hany egysegnyi adat legyen lokalisan eltarolva kuldes elott.
		 */
		private int ratio;

		/**
		 * Getter method for data sending-generating frequency of the station. Getter metodus a station frekvenciajahoz.
		 */
		public long getFreq() {
			return freq;
		}

		/**
		 * Getter method for the final time when data sending can happen. Getter metodus a vegso idohoz amikor tortenhet meg adatkuldes. 
		 */
		public long getLifetime() {
			return lifetime;
		}

		/**
		 * Getter method for the number of station's sensors. Getter metodus a station szenzorainak szamahoz.
		 */
		public int getSensornumber() {
			return sensornumber;
		}

		/**
		 * Constructor creates useful and necessary data for work of a station.
		 * @param lt final time to sending data - utolso ido adatkuldeshez
		 * @param st time when the data sending and data generating starts - az az ido, mikor elkezdodik az adatgeneralas es adatkuldes
		 * @param stt time when the data sending and data generating stops - az az ido, mikor befejezodik az adatgeneralas es adatkuldes
		 * @param fs size of the generated data - a generalt adat merete
		 * @param sn count of the sensors which generate the data - szenzorok szama,amelyek adatot generalnak
		 * @param freq time frequency between generate-send data - frekvencia 2 adatgeneralas-kuldes kozott
		 * @param name ID of the station - station azonosito
		 * @param torepo ID of the repository (its name) - a cel repository neve
		 * @param ratio how much unit of data will be locally store before sending - lokalis adattarolasi aranyt mondja meg az adatkuldes elott
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

		/**
		 * toString can be useful for debuging or loging.
		 * toString metodus debugolashoz,logolashoz.
		 */
		@Override
		public String toString() {
			return "Stationdata [lifetime=" + lifetime + ", starttime=" + starttime + ", stoptime=" + stoptime
					+ ", filesize=" + filesize + ", sensornumber=" + sensornumber + ", freq=" + freq + ", name=" + name
					+ ", torepo=" + torepo + ", ratio=" + ratio + "]";
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
	private boolean randommetering;
	public Cloud cloud;
	int cloudnumber;
	private int messagecount;
	private static ArrayList<Station> stations = new ArrayList<Station>();
	private static long[] stationvalue; 
	
	public static ArrayList<Station> getStations() {
		return stations;
	}

	public static void setStations(ArrayList<Station> stations) {
		Station.stations = stations;
	}

	public static long[] getStationvalue() {
		return stationvalue;
	}

	public static void setStationvalue(long[] stationvalue) {
		Station.stationvalue = stationvalue;
	}

	public void setMessagecount(int messagecount) {
		this.messagecount = messagecount;
	}

	public int getMessagecount() {
		return messagecount;
	}
	

	public long generatedfilesize;
	public static long allstationsize=0;
	
	

	/**
	 * A konstruktor vegzi el a kozos halozatba szervezest az IaaS felhovel
	 * @param maxinbw a station tarolojanak halozati input savszelessege
	 * @param maxoutbw a station tarolojanak halozati output savszelessege
	 * @param diskbw a tarolo lemezenek savszelessege 
	 * @param reposize a tarolo merete
	 * @param sd tovabbi parametereket tarolo Stationdata objektum
	 * @param randommetering true eseten a meresek kesleltetve lesznek random ideig
	 */
	public Station(long maxinbw, long maxoutbw, long diskbw, long reposize, final Stationdata sd,boolean randommetering) {
		this.vm = null;
		this.i = 0;
		this.sd = sd;
		this.messagecount=0;
		isWorking = sd.lifetime == -1 ? false : true;
		lmap = new HashMap<String, Integer>();
		lat = 11;
		lmap.put(sd.name, lat);
		lmap.put(sd.torepo, lat);
		this.reposize = reposize;
		repo = new Repository(this.reposize, sd.name, maxinbw, maxoutbw, diskbw, lmap);
		
		this.randommetering=randommetering;
	}

	/**
	 * TODO kommentezes!!!!!!!!
	 * @return
	 */
	public int getCloudnumber() {
		return cloudnumber;
	}

	public void setCloudnumber(int cloudnumber) {
		this.cloudnumber = cloudnumber;
	}
	public String getName() {
		return this.sd.name;
	}
	
	void setCloud(Cloud cloud) {
		this.cloud = cloud;
	}
	public Repository getRepo() {
		return repo;
	}

	public void setRepo(Repository repo) {
		this.repo = repo;
	}

	/**
	 * Ha sikeresen megerkezett a celrepo-ba a StorageObject, akkor a lokalis tarolobol torli azt.
	 */
	private class StorObjEvent implements ConsumptionEvent {
		private String so;

		private StorObjEvent(String soid) {
			this.so = soid;
		}
		// nem hasznalt metodus, kesobb meg hasznos lehet
		private long Size() {
			String[] splited = so.split("\\s+");
			return Integer.parseInt(splited[1]);
		}

		@Override
		public void conComplete() {
			repo.deregisterObject(this.so);
			cloud.getIaas().repositories.get(0).deregisterObject(this.so);
			
		}

		@Override
		public void conCancelled(ResourceConsumption problematic) {
			System.out.println("A StorageObject torlese sikertelen!");
		}
	}

	/**
	 * Elkuldi a lokalis taroloban talalhato StorageObject-eket a celrepoba
	 * @param r a cel repository
	 */
	private void startCommunicate(Repository r) throws NetworkException {
		for (StorageObject so : repo.contents()) {
			StorObjEvent soe = new StorObjEvent(so.id);
			repo.requestContentDelivery(so.id, r, soe);
		}
	}

	/**
	 * A metodus lathatosaga package private, hivasa az Application osztalybol tortenik, ez 
	 * inditja el a Station mukodeset.
	 * @param interval az ismetlodesi frekvencia
	 */
	 void startMeter(final long interval) {
		if (isWorking) {
			subscribe(interval);
			this.time=Timed.getFireCount();
			this.pm = this.findPm(sd.torepo);
			this.torepo = this.findRepo(sd.torepo);
		}
	}

	/**
	 * Leallitja a Station mukodeset, hivasa akkor fog bekovetkezni, ha a szimulalt ido
	 * meghaladja a Station lifetime-jat es mar minden StorageObject el lett kuldve
	 */
	private void stopMeter() {
		isWorking = false;
		unsubscribe();
		this.torepo.registerObject(new StorageObject(this.sd.name,generatedfilesize, false));
	}

	/**
	 * Megkeresi a celrepot az IaaS felhoben
	 * @param torepo a celrepo azonositoja
	 */
	private Repository findRepo(String torepo) {
		Repository r = null;
		
		for (Repository tmp : this.cloud.getIaas().repositories) {
			if (tmp.getName().equals(torepo)) {
				r = tmp;
			} else { // TODO: kell ez az else ag?!
				for (PhysicalMachine pm :  this.cloud.getIaas().machines) {
					if (pm.localDisk.getName().equals(torepo)) {
						r = pm.localDisk;
					}
				}
			}
		}
		return r;
	}

	/**
	 * Abban az esetben, ha a celrepo nem kozponti tarolo, hanem fizikai gep lemeze, akkor 
	 * a metodus megkeresi az adott fizikai gepet
	 * @param torepo
	 * @return
	 */
	private PhysicalMachine findPm(String torepo) {
		PhysicalMachine p = null;
		for (PhysicalMachine pm :  this.cloud.getIaas().machines) {
			if (pm.localDisk.getName().equals(torepo)) {
				p = pm;
			}
		}
		return p;
	}

	/**
	 * A tick() metodus folyamatosan hivodik meg a beallitott frekvencianak megfeleloen.
	 * Ebben a metodusban tortenik az adatgeneralas es a fajlkuldes, vegezetul pedig a Station leiratkozasa is
	 * ebben a metodusban tortenik meg, ha nincs mar tobb elvegzendo feladata az adott objektumnak.
	 */
	@Override
	public void tick(long fires) {
		// a meres a megadott ideig tart csak
		if (Timed.getFireCount() < (sd.lifetime+this.time) && Timed.getFireCount() >= (sd.starttime+this.time)
				&& Timed.getFireCount() <= (sd.stoptime+this.time)) {
		
			for (int i = 0; i < sd.sensornumber; i++) {
				if(this.randommetering==true){
					
					Random randomGenerator = new Random();
					int randomInt = randomGenerator.nextInt(60)+1;		
					new Metering(this, i, sd.filesize,1000*randomInt);
					
				}else{
					
					new Metering(this, i, sd.filesize,1);
				}
				

			}
		} 
		// de a station mukodese addig amig az osszes SO el nem lett kuldve
		if (this.repo.getFreeStorageCapacity() == reposize && Timed.getFireCount() > (sd.lifetime+this.time)) {
			this.stopMeter();
		}

		// kozponti tarolo a cel repo
		if ( this.cloud.getIaas().repositories.contains(this.torepo)) {
			// megkeresi a celrepo-t es elkuldeni annak
			try {
				if (this.torepo != null) {
					if ((this.repo.getMaxStorageCapacity() - this.repo.getFreeStorageCapacity()) >= sd.ratio
							* sd.filesize || isSubscribed() == false) {
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
						this.vm = this.pm.requestVM( this.cloud.getVa(),  this.cloud.getArc(),
								 this.cloud.getIaas().repositories.get(0), 1)[0];
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
						|| isSubscribed() == false) {
					if (this.vm != null) {
						if (vm.getState().equals(VirtualMachine.State.RUNNING)) {
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
	 * toString metodus a Station fontosabb adatainak kiiratashoz,debugolashoz
	 */
	@Override
	public String toString() {
		return "Station [" + sd + ", reposize:" + this.repo.getMaxStorageCapacity() + ",fajlmeret "
				+ this.generatedfilesize + "]";
	}
}