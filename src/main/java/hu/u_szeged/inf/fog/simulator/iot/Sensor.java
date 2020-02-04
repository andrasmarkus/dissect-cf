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
 *  (C) Copyright 2019, Andras Markus (markusa@inf.u-szeged.hu)
 */

package hu.u_szeged.inf.fog.simulator.iot;

import hu.mta.sztaki.lpds.cloud.simulator.io.*;
import hu.mta.sztaki.lpds.cloud.simulator.*;

/**
 * This class represent a data generation method. With this class we can simulate the measurement interval,
 * which influences the simulation time.
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
class Sensor extends DeferredEvent {

    /**
     * Reference for the station which stores the generated data.
     */
    private Station s;

    /**
     * The constructor calls the parent's constructor with a delay value, which notes the time when
     * the measured data will be available and stored.
     * @param s The station which stores the generated data.
     * @param delay The time delay for the data arises.
     */
    Sensor(Station s, long delay) {
        super(delay);
        this.s = s;
    }

    /**
     * The method generates a file (called StorageObject) and stores the data into the local repository.
     */
    @Override
    protected void eventAction() {
        StorageObject so = new StorageObject(this.s.getDn().repoName + " " + this.s.filesize + " " + this.s.getSensorNum() + " " + Timed.getFireCount(),
                this.s.filesize, false);

        if (this.s.dn.localRepository.registerObject(so)) {
            this.s.incSumOfGeneratedData(this.s.filesize);
            this.s.messageCount += 1;
        } else {
            try {
                throw new Exception("Saving data into the local repository is unsuccessful!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}