/*
 *  ========================================================================
 *  DIScrete event baSed Energy Consumption simulaTor
 *    					             for Clouds, Federations and Fog(DISSECT-CF-Fog)
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
 *  (C) Copyright 2019, Andras Markus (markusa@inf.u-szeged.hu), Peter Gacsi (gacsi.peti95@gmail.com)
 */

package hu.u_szeged.inf.fog.simulator.iot;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;
import hu.mta.sztaki.lpds.cloud.simulator.util.SeedSyncer;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The Station is an realization of the Device class, it represents a simple, one-way entity which operates only on sensors.
 * The goal of the class is managing of the data generation and data sending.
 *
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 * @author Peter Gacsi (gacsi.peti95@gmail.com)
 */
public class Station extends Device {

    /**
     * Number of the sensors. A sensor can represent for example a wind speed meter or rain sensor.
     */
    private int sensorNum;

    /**
     * The frequency of the data measurement and sending process (e.g. in ms).
     */
    private long freq;

    private long delay;
    /**
     * Getter for the number of the sensors.
     */
    public int getSensorNum() {
        return sensorNum;
    }

    private int reInstall;

    private int eventSize;

    private Actuator actuator;

    public long maxFreqLimit;
    public long minFreqLimit;

    private int arrivedActuatorEvents;
    /**
     * The constructor is to create new static, one-way entity for data generation.
     *  @param dn        The network settings including the local repository.
     * @param actuator
     * @param startTime The simulated time when the data generation starts (e.g. in ms).
     * @param stopTime  The simulated time when the data generation stops (e.g. in ms).
     * @param filesize  The size of the generated data (e.g. in byte).
     * @param strategy  The application choosing strategy (see possibilities here: InstallationStrategy.java).
     * @param sensorNum The number of sensors.
     * @param freq      The frequency of the data generation and sending (e.g. in ms).
     * @param x         The X coordinate of the position.
     * @param y         The Y coordinate of the position.
     */
    public Station(int reInstall, int evenSize, DeviceNetwork dn, Actuator actuator, long startTime, long stopTime, long filesize, String strategy, int sensorNum,
                   long freq, double x, double y) {
        this.actuator = actuator;
        // TODO: fix this delay value
        this.delay = Math.abs(SeedSyncer.centralRnd.nextLong() % 20) * 60 * 1000;
        this.eventSize = evenSize;
        this.startTime = startTime + delay;
        this.stopTime = stopTime + delay;
        this.filesize = filesize * sensorNum;
        this.strategy = strategy;
        this.dn = dn;
        this.sensorNum = sensorNum;
        this.reInstall=reInstall;
        this.freq = freq;
        this.maxFreqLimit = freq * 2;
        this.minFreqLimit = freq / 3;
        this.sumOfGeneratedData = 0;
        this.arrivedActuatorEvents = 0;
        this.x = x;
        this.y = y;
        installionProcess(this);
        this.startMeter();
        this.setMessageCount(0);
    }

    /**
     * This method sends all of the generated data (called StorageObject) to the node repository.
     */
    private void startCommunicate() throws NetworkException {
        Collection<StorageObject> storageObjects = new ArrayList<StorageObject>();
        long bulkDataSize = 0;
        for (StorageObject so : this.dn.localRepository.contents()) {
            bulkDataSize += so.size;
            storageObjects.add(so);
        }
        if (bulkDataSize > 0) {
            DataCapsule bulkDataCapsule = new DataCapsule(Timed.getFireCount() + " - data capsule", bulkDataSize, false, this, null, this.eventSize);
            bulkDataCapsule.setBulkStorageObject(storageObjects);
            StorObjEvent soe = new StorObjEvent(bulkDataCapsule);
            NetworkNode.initTransfer(bulkDataCapsule.size, ResourceConsumption.unlimitedProcessing, this.dn.localRepository, this.nodeRepository, soe);
        }
    }

    /**
     * This method starts the station if it doesn't work yet.
     */
    public void startMeter() {
        if (!this.isSubscribed()) {
            new DeferredEvent(this.startTime) {

                @Override
                protected void eventAction() {
                    subscribe(freq);
                    nodeRepository = app.computingAppliance.iaas.repositories.get(0);
                }
            };
        }
    }

    /**
     * It stops the station.
     */
    public void stopMeter() {
        unsubscribe();
    }

    public void restart() {
        stopMeter();
        long time = Timed.getFireCount();
        new DeferredEvent(time + delay) {

            @Override
            protected void eventAction() {
                subscribe(freq);
            }
        };
    }
    
    private void reInstall(final Station s) {
    	new DeferredEvent(this.reInstall+this.delay) {
			
			@Override
			protected void eventAction() {
				if((Timed.getFireCount()+reInstall+delay)<s.stopTime) {
					installionProcess(s);
				}
			}
		};
    }

    /**
     * This method is called when time elapsed defined in the freq variable.
     * The task of the method is control data generating and sending.
     */
    @Override
    public void tick(long fires) {
        if (Timed.getFireCount() < (stopTime) && Timed.getFireCount() >= (startTime)) {
            // TODO: fix this delay value
            new Sensor(this, 1);
        }

        if (this.dn.localRepository.getFreeStorageCapacity() == this.dn.localRepository.getMaxStorageCapacity() && Timed.getFireCount() > stopTime) {
            this.stopMeter();
        }

        try {
            if (this.nodeRepository.getCurrState().equals(Repository.State.RUNNING)) {
                this.startCommunicate();
            }
        } catch (NetworkException e) {
            e.printStackTrace();
        }
        if (!this.app.isSubscribed()) {
            try {
                this.app.restartApplication();

            } catch (VMManagementException e) {

                e.printStackTrace();
            } catch (NetworkException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Load the defined devices from XML file.
     * @param file The path of the XML file.
     */
    /*public static void loadDevice(String stationfile) throws Exception {
        for (DeviceModel dm: DeviceModel.loadDeviceXML(stationfile)) {
            for (int i = 0; i < dm.number; i++) {
                DeviceNetwork dn = new DeviceNetwork(dm.latency, dm.maxinbw, dm.maxoutbw, dm.diskbw, dm.reposize, dm.name + i, null, null);
                new Station(dn, dm.starttime, dm.stoptime, dm.filesize, dm.strategy, dm.sensor, dm.freq, dm.xCoord, dm.yCoord);
            }

        }
    }*/

    /**
     * The installation process depends on the value defined in the strategy variable.
     * Based on the chosen strategy different application would processes the generated data.
     *
     * @param s The Station which goes through the installation.
     */
    public void installionProcess(final Station s) {

        if (this.strategy.equals("load")) {
            new RuntimeDeviceStrategy(this);
        } else if (this.strategy.equals("random")) {
            new RandomDeviceStrategy(this);
        } else if (this.strategy.equals("distance")) {
            new DistanceDeviceStrategy(this);
        } else if (this.strategy.equals("cost")) {
            new CostDeviceStrategy(this);
        } else if (this.strategy.equals("fuzzy")) {
            new FuzzyDeviceStrategy(this);
        }else {
        	try {
				throw new Exception("This device strategy does not exist!");
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        //reInstall(s);
    }

    /**
     * This method performs the shutdown-restart process of a device.
     */
    @Override
    public void shutdownProcess() {
    }

    public Actuator getActuator() {
        return actuator;
    }


    /**
     * Private class to represent the successful or unsuccessful data sending events.
     *
     * @author Andras Markus (markusa@inf.u-szeged.hu)
     */
    private class StorObjEvent implements ConsumptionEvent {

        /**
         * File is under sending.
         */
        private DataCapsule dataCapsule;

        /**
         * Constructor is for a new event of data sending.
         *
         * @param dataCapsule File is under sending.
         */
        private StorObjEvent(DataCapsule dataCapsule) {
            this.dataCapsule = dataCapsule;

        }

        /**
         * If the sending is successful this method will be called.
         */
        @Override
        public void conComplete() {
            for (StorageObject so : this.dataCapsule.getBulkStorageObject()) {
                dn.localRepository.deregisterObject(so);
            }
            // TODO: fix this "cheat"
            app.sumOfArrivedData += this.dataCapsule.size;
            dataCapsule.setDestination(app);
            dataCapsule.addToDataPath(app);
            app.forwardDataCapsules.add(dataCapsule);
        }

        /**
         * If the sending is unsuccessful this method will be called.
         */
        @Override
        public void conCancelled(ResourceConsumption problematic) {
            try {
                // TODO: handle it
                throw new Exception("Deleting StorageObject from local repository is unsuccessful!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class ActualizationEvent implements ConsumptionEvent {

        private Station station;

        public ActualizationEvent(Station station) {
            this.station = station;
        }

        @Override
        public void conComplete() {
            station.arrivedActuatorEvents++;
            station.getActuator().executeEventOn(station);

        }

        @Override
        public void conCancelled(ResourceConsumption problematic) {

        }
    }
}