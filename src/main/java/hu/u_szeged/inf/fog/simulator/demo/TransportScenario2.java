package hu.u_szeged.inf.fog.simulator.demo;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.iot.Device;
import hu.u_szeged.inf.fog.simulator.iot.SensorCharacteristics;
import hu.u_szeged.inf.fog.simulator.iot.Station;
import hu.u_szeged.inf.fog.simulator.iot.actuator.Actuator;
import hu.u_szeged.inf.fog.simulator.iot.actuator.ActuatorRandomStrategy;
import hu.u_szeged.inf.fog.simulator.iot.mobility.GeoLocation;
import hu.u_szeged.inf.fog.simulator.iot.mobility.LinearMobilityStrategy;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;
import hu.u_szeged.inf.fog.simulator.providers.Instance;
import hu.u_szeged.inf.fog.simulator.util.TimelineGenerator;

import java.util.Random;

public class TransportScenario2 {

    public static void main(String[] args) throws Exception {

        VirtualAppliance va = new VirtualAppliance("va", 100, 0, false, 1073741824L);

        AlterableResourceConstraints arc1 = new AlterableResourceConstraints(8,0.001,4294967296L);
        AlterableResourceConstraints arc2 = new AlterableResourceConstraints(4,0.001,4294967296L);

        // AWS pricing
        new Instance(va,arc1,0.0000000566666667,"instance1");
        new Instance(va,arc2,0.0000000283333333,"instance2");

        String cloudfile = ScenarioBase.resourcePath+"\\fuzzy\\LPDS_frankfurt.xml";
        String fogfile = ScenarioBase.resourcePath+"\\fuzzy\\LPDS_athen.xml";

        double range = 25*1000;
        int numberOfDevices=2;
        
     // Frankfurt
        ComputingAppliance cloud1 = new ComputingAppliance(cloudfile, "cloud1",new GeoLocation(50.1213479,8.4964818),2500*1000);

        // Bekescsaba
        ComputingAppliance fog1 = new ComputingAppliance(fogfile, "fog1",new GeoLocation(46.68333,21.1),range);

        // Szolnok
        ComputingAppliance fog2 = new ComputingAppliance(fogfile, "fog2",new GeoLocation(47.18333,20.2),range);

        // Kecskemet
        ComputingAppliance fog3 = new ComputingAppliance(fogfile, "fog3",new GeoLocation(46.90618,19.69128),range);

        // Budapest
        ComputingAppliance fog4 = new ComputingAppliance(fogfile, "fog4",new GeoLocation(47.49801,19.03991),range);

        // Tatabanya
        ComputingAppliance fog5 = new ComputingAppliance(fogfile, "fog5",new GeoLocation(47.58494,18.39325), range);

        // Veszprem
        ComputingAppliance fog6 = new ComputingAppliance(fogfile, "fog6",new GeoLocation(47.09327,17.91149), range);

        // Kaposvar
        ComputingAppliance fog7 = new ComputingAppliance(fogfile, "fog7",new GeoLocation(46.36667,17.8), range);

        // Baja
        ComputingAppliance fog8 = new ComputingAppliance(fogfile, "fog8",new GeoLocation(46.17496,18.95639), range);

        // Szeged
        ComputingAppliance fog9 = new ComputingAppliance(fogfile, "fog9",new GeoLocation(46.253,20.14824), range);
        
        fog1.setLatency(cloud1, 34);
        fog2.setLatency(cloud1, 43);
        fog3.setLatency(cloud1, 32);
        fog4.setLatency(cloud1, 40);
        fog5.setLatency(cloud1, 27);
        fog6.setLatency(cloud1, 63);
        fog7.setLatency(cloud1, 22);
        fog8.setLatency(cloud1, 26);
        fog9.setLatency(cloud1, 31);

        fog1.setParentNode(cloud1);
        fog2.setParentNode(cloud1);
        fog3.setParentNode(cloud1);
        fog4.setParentNode(cloud1);
        fog5.setParentNode(cloud1);
        fog6.setParentNode(cloud1);
        fog7.setParentNode(cloud1);
        fog8.setParentNode(cloud1);
        fog9.setParentNode(cloud1);
        fog1.addNeighbour(fog2, fog9);
        fog2.addNeighbour(fog3);
        fog3.addNeighbour(fog4);
        fog4.addNeighbour(fog5);
        fog5.addNeighbour(fog6);
        fog6.addNeighbour(fog7);
        fog7.addNeighbour(fog8);
        fog8.addNeighbour(fog9);
		
		
        // 5 min freq., 250 kilobytes = 5 minutes or 10 minutes
        Application ca1 = new Application(5*60*1000, 256000, "instance1", "Cloud-app1", 2400.0, 1, "random", false);
        Application fa1 = new Application(5*60*1000, 256000, "instance2", "Fog-app1", 2400.0, 1, "random", true);
        Application fa2 = new Application(5*60*1000, 256000, "instance2", "Fog-app2", 2400.0, 1, "random", true);
        Application fa3 = new Application(5*60*1000, 256000, "instance2", "Fog-app3", 2400.0, 1, "random", true);
        Application fa4 = new Application(5*60*1000, 256000, "instance2", "Fog-app4", 2400.0, 1, "random", true);
        Application fa5 = new Application(5*60*1000, 256000, "instance2", "Fog-app5", 2400.0, 1, "random", true);
        Application fa6 = new Application(5*60*1000, 256000, "instance2", "Fog-app6", 2400.0, 1, "random", true);
        Application fa7 = new Application(5*60*1000, 256000, "instance2", "Fog-app7", 2400.0, 1, "random", true);
        Application fa8 = new Application(5*60*1000, 256000, "instance2", "Fog-app8", 2400.0, 1, "random", true);
        Application fa9 = new Application(5*60*1000, 256000, "instance2", "Fog-app9", 2400.0, 1, "random", true);

        cloud1.addApplication(ca1);
        fog1.addApplication(fa1);
        fog2.addApplication(fa2);
        fog3.addApplication(fa3);
        fog4.addApplication(fa4);
        fog5.addApplication(fa5);
        fog6.addApplication(fa6);
        fog7.addApplication(fa7);
        fog8.addApplication(fa8);
        fog9.addApplication(fa9);

        GeoLocation pos1 = new GeoLocation(46.673491, 21.084472);
        


      

        Random random = new Random();
        for (int i = 0; i < 2 * 365; i++) {

            for(int j = 0 ; j <numberOfDevices; j++) {
            	
            	// par perccel toljuk el a j darab inditasat
            	long startTime = (i * 12 * 60* 60* 1000L) + (j*1000*60);
                long stopTime = startTime + 12 * 60 * 60 * 1000L;
                double actuatorRatio = 0.5;
                double fogRatio = random.nextDouble();
                
                Device.DeviceNetwork dn = new Device.DeviceNetwork(50, 10000, 10000, 10000, 200000000, "dnRepository" + i+"-"+j, null, null);
                if(j%2==0) {
                	// visszakuldott esemeny: 50 byte, 150 byte sensor, 
                	Station s = new Station(10 * 60 * 1000, 50, dn, startTime, stopTime, 150, "distance",
                            new SensorCharacteristics(3, 1000L * 60 * 60 * 24 * 328, 1 * 60 * 1000, 15 * 60 * 1000, fogRatio, actuatorRatio, 50, 1),
                            5 * 60 * 1000, pos1,
                			
                			new LinearMobilityStrategy(new GeoLocation(46.6795384,21.0128544), 0.0202777,

                					new GeoLocation(46.8471995,20.5370569),
                                    new GeoLocation(47.0259811,20.2621087),           
                                    new GeoLocation(47.1804973,20.1136325),
                                    new GeoLocation(47.2025217,19.7341719),
                                    new GeoLocation(46.8857078,19.5389746),
                                    new GeoLocation(47.4813602,18.9902207),
                                    new GeoLocation(47.5055901,18.5203231),
                                    new GeoLocation(47.5481325,18.347633),
                                    new GeoLocation(47.1946298,18.3712124),
                                    new GeoLocation(47.1258945,17.837209),
                                    new GeoLocation(46.9717908,18.1009292), 
                                    new GeoLocation(46.7450279,17.7097287),
                                    new GeoLocation(46.3705281,17.7461179),
                                    new GeoLocation(46.3492927,18.6611945),
                                    new GeoLocation(46.1736076,18.6581572),
                                    new GeoLocation(46.1838216,19.2688341),
                                    new GeoLocation(46.2870984,19.4942792),
                                    new GeoLocation(46.232941,20.0003862),                                 
                                    new GeoLocation(46.6795384,21.0128544)
                                    ));
                	
                			s.setActuator(new Actuator(new ActuatorRandomStrategy(), 5, s));
                }else {
                	Station s = new Station(10 * 60 * 1000, 50, dn, startTime, stopTime, 150, "distance", 
                			new SensorCharacteristics(3, 1000L * 60 * 60 * 24 * 328, 5 * 60 *1000, 5 * 60 * 10000, fogRatio, actuatorRatio, 50, 1), 
                			5 * 60 * 1000, pos1, 
                			
                			new LinearMobilityStrategy(new GeoLocation(46.6795384,21.0128544), 0.0202777,

                					new GeoLocation(46.232941,20.0003862),
                  					 new GeoLocation(46.2870984,19.4942792),
                  					 new GeoLocation(46.1838216,19.2688341),
                  					 new GeoLocation(46.1736076,18.6581572),
                  					 new GeoLocation(46.3492927,18.6611945),
                  					 new GeoLocation(46.3705281,17.7461179),
                  					 new GeoLocation(46.7450279,17.7097287),
                  					 new GeoLocation(46.9717908,18.1009292), 
                  					 new GeoLocation(47.1258945,17.837209),
                  					 new GeoLocation(47.1946298,18.3712124),
                  					 new GeoLocation(47.5481325,18.347633),
                  					 new GeoLocation(47.5055901,18.5203231),
                  					 new GeoLocation(47.4813602,18.9902207),
                  					 new GeoLocation(46.8857078,19.5389746),
                  					 new GeoLocation(47.2025217,19.7341719),
                  					 new GeoLocation(47.1804973,20.1136325),
                  					 new GeoLocation(47.0259811,20.2621087),    
                                      new GeoLocation(46.8471995,20.5370569),
                                      new GeoLocation(46.6795384,21.0128544) 
                                    ));
                			s.setActuator(new Actuator(new ActuatorRandomStrategy(), 5, s));
                }
                
                
                
            
            }

        }

        long starttime = System.nanoTime();
        Timed.simulateUntilLastEvent();
        long stopttime = System.nanoTime();

        // Print some information to the monitor / in file
        TimelineGenerator.generate();
        ScenarioBase.printInformation((stopttime-starttime),false);
    }


}
