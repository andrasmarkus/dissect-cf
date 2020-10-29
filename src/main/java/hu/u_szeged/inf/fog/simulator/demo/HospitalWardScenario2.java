package hu.u_szeged.inf.fog.simulator.demo;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.iot.Device;
import hu.u_szeged.inf.fog.simulator.iot.SensorCharacteristics;
import hu.u_szeged.inf.fog.simulator.iot.Station;
import hu.u_szeged.inf.fog.simulator.iot.actuator.*;
import hu.u_szeged.inf.fog.simulator.iot.mobility.GeoLocation;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;
import hu.u_szeged.inf.fog.simulator.providers.Instance;
import hu.u_szeged.inf.fog.simulator.util.TimelineGenerator;

import java.util.Random;

public class HospitalWardScenario2 {

    public static void main(String[] args) throws Exception {

        VirtualAppliance va = new VirtualAppliance("va", 100, 0, false, 1073741824L);

        AlterableResourceConstraints arc1 = new AlterableResourceConstraints(8,0.001,4294967296L);
        AlterableResourceConstraints arc2 = new AlterableResourceConstraints(4,0.001,4294967296L);

        // AWS pricing
        new Instance(va,arc1,0.0000000566666667,"instance1");
        new Instance(va,arc2,0.0000000283333333,"instance2");

        String cloudfile = ScenarioBase.resourcePath+"\\fuzzy\\LPDS_frankfurt.xml";
        String fogfile = ScenarioBase.resourcePath+"\\fuzzy\\LPDS_budapest.xml";

        // Frankfurt
        ComputingAppliance cloud1 = new ComputingAppliance(cloudfile, "cloud1",new GeoLocation(50.1213479,8.4964818),2500*1000);

        Application ca1 = new Application(30 * 1000, 256000, "instance1", "Cloud-app1", 2400.0, 1, "random", true);

        
        //ComputingAppliance fog1 = new ComputingAppliance(fogfile, "fog1", new GeoLocation(46.145286, 20.149618), 5 * 1000);
        //ComputingAppliance  fog2 = new ComputingAppliance(fogfile, "fog2", new GeoLocation(46.245486, 20.249818), 5 * 1000);
        //ComputingAppliance fog3 = new ComputingAppliance(fogfile, "fog3", new GeoLocation(46.345486, 20.349818), 5 * 1000);

       // Application fa1 = new Application(30 * 1000, 256000, "instance2", "Fog-app1", 2400.0, 1, "random", true);
      //  Application fa2 = new Application(60 * 1000, 256000, "instance2", "Fog-app2", 2400.0, 1, "random", true);
      // Application fa3 = new Application(60 * 1000, 256000, "instance2", "Fog-app3", 2400.0, 1, "random", true);

        cloud1.addApplication(ca1);
     	//fog1.addApplication(fa1);
     //   fog2.addApplication(fa2);
    //    fog3.addApplication(fa3);

   // 	 fog1.setLatency(cloud1, 63);
   //     fog1.setParentNode(cloud1);
   //     fog2.setLatency(cloud1, 112);
    //    fog2.setParentNode(cloud1);
    //    fog3.setLatency(cloud1, 108);
   //    fog3.setParentNode(cloud1);

    //   fog1.addNeighbour(fog2);
   //    fog2.addNeighbour(fog3);


        final Random randomValueGenerator = new Random();

        int numberOfDevices=10000;

        for(int i=0; i<numberOfDevices;i++){
            long mttf = 1000L*60*60*24*365*15;
            int minFreq = 1000 * 60 * 5;
            int maxFreq = 1000 * 60 * 60;
            int actuatorResponseSize = 120;
            long defaultFreq = 1000 * 60 * 30 ;
            int filesize = 300;
            int numberOfSensors = 2;
            double actuatorRatio = 1.0;
            double fogRatio = 1.0;

            Device.DeviceNetwork dn = new Device.DeviceNetwork(50, 10000, 10000, 10000, 200000000, "dnRepository" + i, null, null);

            Station s = new Station(10 * 60 * 1000, actuatorResponseSize, dn, 0, 1000L * 60 * 60 * 24, filesize, "distance",
                    new SensorCharacteristics(numberOfSensors, mttf, minFreq, maxFreq, fogRatio, actuatorRatio, 40, 1),
                    defaultFreq, new GeoLocation(46+randomValueGenerator.nextDouble(), 20+randomValueGenerator.nextDouble()), null);
            	
            s.setActuator(new Actuator(new ActuatorStrategy() {
                @Override
                public ActuatorEvent selectEvent(Station station) {
                    //Random Blood pressure value bw [80;220]
                    int lowBP = 80;
                    int highBP = 220;
                    int currentBP = randomValueGenerator.nextInt(highBP-lowBP) + lowBP;

                    //Random Pulse value bw [40;140]
                    int lowPulse = 40;
                    int highPulse = 140;
                    int currentPulse = randomValueGenerator.nextInt(highPulse-lowPulse) + lowPulse;

                    boolean normalBP = 90 <= currentBP && currentBP <= 140;
                    boolean normalPulse = 60 <= currentPulse && currentPulse <= 100;

                    if(!normalBP || !normalPulse) {
                        return new ReduceFrequencyEvent(1000 * 60 * 5);
                    }
                    else {
                        return new IncreaseFrequencyEvent(1000 * 60 * 5);
                    }

                }
            }, 5, s));
        }


        long starttime = System.nanoTime();
        Timed.simulateUntilLastEvent();
        long stopttime = System.nanoTime();

        TimelineGenerator.generate();
        ScenarioBase.printInformation((stopttime-starttime),false);
    }



}

