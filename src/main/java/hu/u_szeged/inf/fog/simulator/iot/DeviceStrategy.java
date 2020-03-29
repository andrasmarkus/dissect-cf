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
 *  (C) Copyright 2019, Andras Markus (markusa@inf.u-szeged.hu), Jozsef Daniel Dombi (dombijd@inf.u-szeged.hu),
 *  					Peter Gacsi (gacsi.peti95@gmail.com)
 */

package hu.u_szeged.inf.fog.simulator.iot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.pliant.FuzzyIndicators;
import hu.u_szeged.inf.fog.simulator.pliant.Kappa;
import hu.u_szeged.inf.fog.simulator.pliant.Sigmoid;

/**
 * The goal of this class to pair a device to an application. A few default strategy has been implemented, 
 * but you can define0 your strategy, for this, you should implement the InstallionStrategy interface. 
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 * @author Jozsef Daniel Dombi (dombijd@inf.u-szeged.hu)
 * @author Peter Gacsi (gacsi.peti95@gmail.com)
 */
public abstract class DeviceStrategy {

    /**
     * This method needs to be overrode. It should pair an application with a device.
     * @param s The device which to be paired.
     */
    public abstract void install(final Device s);

    /**
     * This function makes connection between a device and a smart devices.
     * @param d The device which has to be paired.
     * @param app The application which has to be paired.
     */
    public void makeConnection(Device d, Application app) {
        d.setApp(app);
        app.deviceList.add(d);
        d.dn.lmap.put(d.getDn().repoName, d.dn.latency);
        d.dn.lmap.put(d.app.computingAppliance.iaas.repositories.get(0).getName(), d.dn.latency);
    }

}


/**
 * Random strategy randomly chooses one available application located at any node.
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 * @author Peter Gacsi (gacsi.peti95@gmail.com)
 */
class RandomDeviceStrategy extends DeviceStrategy {

    /**
     * The constructor calls installation process.
     * @param d The device which needs to be installed.
     */
    public RandomDeviceStrategy(Device d) {
        this.install(d);
    }

    /**
     * The method prefers the application chosen from the fog nodes first,
     * then it chooses between the cloud applications
     */
    @Override
    public void install(Device d) {
        int rnd;
        Random randomGenerator = new Random();

        List < Application > possibleApplications = new ArrayList < Application > ();

        for (Application app: Application.allApplication) {
            if (app.canJoin) {
            	possibleApplications.add(app);
            }
        }

        if (possibleApplications.size() > 0) {
            rnd = randomGenerator.nextInt(possibleApplications.size());
            makeConnection(d, possibleApplications.get(rnd));
        } else {
            try {
				throw new Exception("There is no possible application for the data transfer!");
			} catch (Exception e) {
				e.printStackTrace();
			}
        }

        if (!d.app.isSubscribed()) {
            try {
                d.app.restartApplication();

            } catch (VMManagementException e) {
                e.printStackTrace();
            } catch (NetworkException e) {
                e.printStackTrace();
            }
        }

    }
}


/**
 * Distance-aware strategy always installs station to the nearest application.
 * @author Peter Gacsi (gacsi.peti95@gmail.com)
 */
class DistanceDeviceStrategy extends DeviceStrategy {

    /**
     * The constructor calls installation process.
     * @param d The device which needs to be installed.
     */
    public DistanceDeviceStrategy(Device d) {
        this.install(d);
    }

    /**
     * The method finds the closest application available.
     * @param d The reference of the actual device.
     */
    public Application getNearestDevice(Device d) {
        double minDistance = Double.MAX_VALUE;
        Application nearestApplication = null;
        for (Application app: Application.allApplication) {
            if (minDistance >= d.calculateDistance(app) && app.canJoin) {
                minDistance = d.calculateDistance(app);
                nearestApplication = app;
            }
        }
        if(nearestApplication==null) {
        	try {
				throw new Exception("There is no possible application for the data transfer!");
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        return nearestApplication;
    }

    /**
     * The method prefers the closest application.
     */
    @Override
    public void install(Device s) {
        Application nearestApp = getNearestDevice(s);

        makeConnection(s, nearestApp);

        if (!s.app.isSubscribed()) {
            try {
                s.app.restartApplication();

            } catch (VMManagementException e) {

                e.printStackTrace();
            } catch (NetworkException e) {

                e.printStackTrace();
            }
        }

    }

}

/**
 * Cost-aware strategy prefers the cheapest application.
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
class CostDeviceStrategy extends DeviceStrategy {

    /**
     * The constructor calls installation process.
     * @param d The device which needs to be installed.
     */
    public CostDeviceStrategy(Device d) {
        this.install(d);
    }

    /**
     * The method prefers the cheapest application.
     */
    @Override
    public void install(Device s) {
        double min = Integer.MAX_VALUE - 1.0;
        int choosen = -1;
        for (int i = 0; i < Application.allApplication.size(); ++i) {
            if (Application.allApplication.get(i).instance.getPricePerTick() < min && Application.allApplication.get(i).canJoin) {
                min = Application.allApplication.get(i).instance.getPricePerTick();
                choosen = i;
            }
        }
        
        if(Application.allApplication.get(choosen)==null) {
        	try {
				throw new Exception("There is no possible application for the data transfer!");
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        makeConnection(s, Application.allApplication.get(choosen));

        if (!s.app.isSubscribed()) {
            try {
                s.app.restartApplication();

            } catch (VMManagementException e) {

                e.printStackTrace();
            } catch (NetworkException e) {

                e.printStackTrace();
            }
        }
    }

}

/**
 * Runtime-aware strategy calculates the ratio of the number of connected devices and the number of physical machines and chooses the less loaded application. 
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
class RuntimeDeviceStrategy extends DeviceStrategy {

    /**
     * The constructor calls installation process.
     * @param d The device which needs to be installed.
     */
    public RuntimeDeviceStrategy(Device s) {
        this.install(s);
    }

    /**
     * The method prefers the less loaded application.
     */
    @Override
    public void install(final Device s) {
        new DeferredEvent(s.startTime) {

            @Override
            protected void eventAction() {
                double min = Double.MAX_VALUE - 1.0;
                int choosen = -1;
                for (int i = 0; i < Application.allApplication.size(); i++) {
                    double loadRatio = (Application.allApplication.get(i).deviceList.size()) / (Application.allApplication.get(i).computingAppliance.iaas.machines.size());
                    if (loadRatio < min) {
                        min = loadRatio;
                        choosen = i;
                    }
                }

                if(Application.allApplication.get(choosen)==null) {
                	try {
        				throw new Exception("There is no possible application for the data transfer!");
        			} catch (Exception e) {
        				e.printStackTrace();
        			}
                }
                makeConnection(s, Application.allApplication.get(choosen));


                if (!s.app.isSubscribed()) {
                    try {
                        s.app.restartApplication();

                    } catch (VMManagementException e) {

                        e.printStackTrace();
                    } catch (NetworkException e) {

                        e.printStackTrace();
                    }
                }
            }
        };


    }

}

/**
 * Fuzzy-based strategy..
 * TODO: refactoring needed! 
 * @author Jozsef Daniel Dombi (dombijd@inf.u-szeged.hu)
 */
class FuzzyDeviceStrategy extends DeviceStrategy {

    /**
     * The local copy of the device which needs to be installed.
     */
    Device d;

    /**
     * The constructor calls installation process.
     * @param d The device which needs to be installed.
     */
    public FuzzyDeviceStrategy(Device d) {
        this.d = d;
        this.install(d);
    }

    @Override
    public void install(final Device s) {
        new DeferredEvent(s.startTime) {

            @Override
            protected void eventAction() {
                int rsIdx = fuzzyDecision(d);

                if(Application.allApplication.get(rsIdx)==null) {
                	try {
        				throw new Exception("There is no possible application for the data transfer!");
        			} catch (Exception e) {
        				e.printStackTrace();
        			}
                }
                makeConnection(s, Application.allApplication.get(rsIdx));


                if (!s.app.isSubscribed()) {
                    try {
                        s.app.restartApplication();

                    } catch (VMManagementException e) {

                        e.printStackTrace();
                    } catch (NetworkException e) {

                        e.printStackTrace();
                    }
                }

            }


        };


    }

    private int fuzzyDecision(Device s) {


        Kappa kappa = new Kappa(3.0, 0.4);

        Sigmoid<Object> sig = new Sigmoid<Object>(Double.valueOf(-1.0 / 96.0), Double.valueOf(15));
        Vector < Double > price = new Vector < Double > ();
        for (int i = 0; i < Application.allApplication.size(); ++i) {
            price.add(kappa.getAt(sig.getat(Application.allApplication.get(i).instance.getPricePerTick() * 1000000000)));

        }

        double minprice = Double.MAX_VALUE;
        double maxprice = Double.MIN_VALUE;
        for (int i = 0; i < Application.allApplication.size(); ++i) {
            double currentprice = Application.allApplication.get(i).getCurrentCost();
            if (currentprice > maxprice)
                maxprice = currentprice;
            if (currentprice < minprice)
                minprice = currentprice;
        }


        Vector < Double > currentprice = new Vector < Double > ();

        sig = new Sigmoid<Object>(Double.valueOf(-1.0), Double.valueOf((maxprice - minprice) / 2.0));
        for (int i = 0; i < Application.allApplication.size(); ++i) {
            currentprice.add(kappa.getAt(sig.getat(Application.allApplication.get(i).getCurrentCost())));
        }




        double minworkload = Double.MAX_VALUE;
        double maxworkload = Double.MIN_VALUE;
        for (int i = 0; i < Application.allApplication.size(); ++i) {
            double workload = Application.allApplication.get(i).computingAppliance.getloadOfResource();
            if (workload > maxworkload)
                maxworkload = workload;
            if (workload < minworkload)
                minworkload = workload;
        }

        Vector < Double > workload = new Vector < Double > ();

        sig = new Sigmoid<Object>(Double.valueOf(-1.0), Double.valueOf(maxworkload));
        for (int i = 0; i < Application.allApplication.size(); ++i) {
            workload.add(kappa.getAt(sig.getat(Application.allApplication.get(i).computingAppliance.getloadOfResource())));

        }



        Vector < Double > numberofvm = new Vector < Double > ();
        sig = new Sigmoid<Object>(Double.valueOf(-1.0 / 8.0), Double.valueOf(3));
        for (int i = 0; i < Application.allApplication.size(); ++i) {
            numberofvm.add(kappa.getAt(sig.getat(Double.valueOf(Application.allApplication.get(i).vmManagerlist.size()))));

        }


        double sum_stations = 0.0;
        for (int i = 0; i < Application.allApplication.size(); ++i) {
            sum_stations += Application.allApplication.get(i).deviceList.size();
        }

        Vector < Double > numberofstation = new Vector < Double > ();
        sig = new Sigmoid<Object>(Double.valueOf(-0.125), Double.valueOf(sum_stations / (Application.allApplication.size())));
        for (int i = 0; i < Application.allApplication.size(); ++i) {
            numberofstation.add(kappa.getAt(sig.getat(Double.valueOf(Application.allApplication.get(i).deviceList.size()))));

        }

        Vector < Double > numberofActiveStation = new Vector < Double > ();
        for (int i = 0; i < Application.allApplication.size(); ++i) {
            double sum = 0.0;
            for (int j = 0; j < Application.allApplication.get(i).deviceList.size(); j++) {
                Station stat = (Station) Application.allApplication.get(i).deviceList.get(j);
                long time = Timed.getFireCount();
                if (stat.startTime >= time && stat.stopTime >= time)
                    sum += 1;
            }
            numberofActiveStation.add(sum);
        }
        sum_stations = 0.0;
        for (int i = 0; i < numberofActiveStation.size(); ++i) {
            sum_stations += numberofActiveStation.get(i);
        }

        sig = new Sigmoid<Object>(Double.valueOf(-0.125), Double.valueOf(sum_stations / (numberofActiveStation.size())));
        for (int i = 0; i < numberofActiveStation.size(); ++i) {
            double a = numberofActiveStation.get(i);
            double b = sig.getat(a);
            double c = kappa.getAt(b);
            numberofActiveStation.set(i, c);
        }




        Vector < Double > preferVM = new Vector < Double > ();
        sig = new Sigmoid<Object>(Double.valueOf(1.0 / 32), Double.valueOf(3));
        for (int i = 0; i < Application.allApplication.size(); ++i) {
            preferVM.add(kappa.getAt(sig.getat(Double.valueOf(Application.allApplication.get(i).instance.getArc().getRequiredCPUs()))));
        }


        Vector < Double > preferVMMem = new Vector < Double > ();
        sig = new Sigmoid<Object>(Double.valueOf(1.0 / 256.0), Double.valueOf(350.0));
        for (int i = 0; i < Application.allApplication.size(); ++i) {
            preferVMMem.add(kappa.getAt(sig.getat(Double.valueOf(Application.allApplication.get(i).instance.getArc().getRequiredMemory() / 10000000))));
        }





        Vector < Double > score = new Vector < Double > ();
        for (int i = 0; i < price.size(); ++i) {
            Vector < Double > temp = new Vector < Double > ();
            temp.add(price.get(i));

            temp.add(numberofstation.get(i));
            temp.add(numberofActiveStation.get(i));
            temp.add(preferVM.get(i));
            temp.add(workload.get(i));
            temp.add(currentprice.get(i));

            score.add(FuzzyIndicators.getAggregation(temp) * 100);
        }
        Vector < Integer > finaldecision = new Vector < Integer > ();
        for (int i = 0; i < Application.allApplication.size(); ++i) {
            finaldecision.add(i);
        }
        for (int i = 0; i < score.size(); ++i) {
            for (int j = 0; j < score.get(i); j++) {
                finaldecision.add(i);
            }
        }
        Random rnd = new Random();
        Collections.shuffle(finaldecision);
        int temp = rnd.nextInt(finaldecision.size());
        return finaldecision.elementAt(temp);


    }
}