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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.util.PowerTransitionGenerator;
import hu.u_szeged.inf.fog.simulator.application.Application;

/**
 * This class can represent a smart object of an IoT system.
 * The main goal of the class to ensure the data generation function by sensors and
 * to store the data until forwarding to an fog/cloud node.
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 * @author Peter Gacsi (gacsi.peti95@gmail.com)
 */
public abstract class Device extends Timed {

    /**
     * Static latency value, between the the local and the cloud/fog's repository.
     */
    protected static final int latency = 11;

    /**
     * The map connects the repositories based on the latency value.
     */
    protected static HashMap < String, Integer > lmap = new HashMap < String, Integer > ();

    /**
     * It's a reference for the application which processes the data generated by a device.
     */
    protected Application app;

    /**
	 * The chosen strategy of the station. The application choosing is based on the strategy. 
	 */
	protected String strategy;
	
    /**
     * The local repository can be reached through this variable and it holds the network settings of the repository.
     */
    protected DeviceNetwork dn;

    /**
     * Contains the amount of generated data by the sensors of this device.
     */
    protected long sumOfGeneratedData;

    /**
     * The number of the generated files.
     */
    protected int messageCount;

    /**
     * A reference to the destination repository of the generated data.
     */
    protected Repository nodeRepository;

    /**
     *	The simulated time when the device starts the data generation-sending loops (e.g. in ms).
     */
    protected long startTime;

    /**
     *	The simulated time when the device stops the data generation-sending loops (e.g. in ms).
     */
    public long stopTime;

    /**
     *	The size of the generated data (e.g. in byte).
     */
    protected long filesize;

    /**
     * The X position of the device in the Coordinate-system.
     */
    protected double x;

    /**
     * The Y position of the device in the Coordinate-system.
     */
    protected double y;

    /**
     * Getter for the amount of generated data by the sensors of this device.
     */
    public long getSumOfGeneratedData() {
        return this.sumOfGeneratedData;
    }

    /**
     * Increasing method for the amount of generated data by the sensors of this device.
     */
    public void incSumOfGeneratedData(long temp) {
        this.sumOfGeneratedData += temp;
    }

    /**
     * Getter for the simulated time when the device stops the data generation-sending loops.
     */
    public long getStopTime() {
        return stopTime;
    }

    /**
     * Getter for the size of the generated data (e.g. in byte).
     */
    public long getFilesize() {
        return filesize;
    }

    /**
     * This method performs the shutdown-restart process of a device. 
     * TODO: not implemented yet.
     */
    public abstract void shutdownProcess();

    //I need to access frequency changing in the actuator
    public void changeFrequency(final long freq) {
        updateFrequency(freq);
    }

    /**
     * Getter for the local repository and its network settings.
     */
    public DeviceNetwork getDn() {
        return dn;
    }

    /**
     * Setter for the application which processes the data generated by a device.
     */
    public void setApp(Application a) {
        this.app = a;
    }

    /**
     * Getter for a counter which counts the number of the generated files by the sensors of this device.
     */
    public int getMessageCount() {
        return messageCount;
    }

    /**
     * Setter for a counter which counts the number of the generated files by the sensors of this device.
     */
    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    /**
     * This method calculates the distance between the application and this device based on the X, Y coordinates.
     * It can be useful for distance-aware algorithms.
     * @param app The application which this device can connected to.
     */
    protected double calculateDistance(Application app) {
        double result = Math.sqrt(
            Math.pow((this.x - app.computingAppliance.x), 2) +
            Math.pow((this.y - app.computingAppliance.y), 2)
        );
        return result;
    }

    /**
     * This class represents an entity which responsible for communication and storing (called Repository).
     * The goal of this class 
     */
    public static class DeviceNetwork {
        Repository localRepository;
        String repoName;

        /**
         * The constructor generates the local repository of the device with the given size and network settings. Leave the last 2 parameters to
         * null for using the default storage and network transitions.
         * @param maxinbw The maximum value of the input bandwidth (e.g. in byte).
         * @param maxoutbw The maximum value of the output bandwidth (e.g. in byte).
         * @param diskbw The bandwidth between disks (e.g. in byte).
         * @param repoSize The maximum size of the local repository (e.g. in byte).
         * @param repoName The ID of the repository.
         * @param storageTransitions Storage power behavior, leave it to null for the default value
         * @param networkTransitions Network power behavior, leave it to null for the default value
         */
        public DeviceNetwork(long maxinbw, long maxoutbw, long diskbw, long repoSize, String repoName, Map < String, PowerState > storageTransitions, Map < String, PowerState > networkTransitions) {
            if (storageTransitions == null) {
                storageTransitions = defaultTransitions("storage");
            }
            if (networkTransitions == null) {
                networkTransitions = defaultTransitions("network");
            }
            localRepository = new Repository(repoSize, repoName, maxinbw, maxoutbw, diskbw, lmap, storageTransitions, networkTransitions);
            try {
                localRepository.setState(NetworkNode.State.RUNNING);
            } catch (NetworkException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Default representative storage/network power behavior.
     * @param type Type of the default power behavior ( storage/network).
     */
    private static Map < String, PowerState > defaultTransitions(String type) {
        double minpower = 20;
        double idlepower = 200;
        double maxpower = 300;
        double diskDivider = 10;
        double netDivider = 20;
        EnumMap < PowerTransitionGenerator.PowerStateKind, Map < String, PowerState >> transitions = null;
        try {
            transitions = PowerTransitionGenerator
                .generateTransitions(minpower, idlepower, maxpower, diskDivider, netDivider);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        if (type.equals("storage")) {
            return transitions.get(PowerTransitionGenerator.PowerStateKind.storage);
        } else if (type.equals("network")) {
            return transitions.get(PowerTransitionGenerator.PowerStateKind.network);
        }
        return null;
    }

}