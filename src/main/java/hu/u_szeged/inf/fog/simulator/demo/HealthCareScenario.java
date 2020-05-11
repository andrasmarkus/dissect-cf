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
import hu.u_szeged.inf.fog.simulator.iot.mobility.RandomMobilityStrategy;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;
import hu.u_szeged.inf.fog.simulator.providers.*;
import hu.u_szeged.inf.fog.simulator.task_schedule.PrioritizedTaskScheduler;
import hu.u_szeged.inf.fog.simulator.util.TimelineGenerator;

import java.util.ArrayList;
import java.util.Random;

public class HealthCareScenario {

    public static void main(String[] args) throws Exception {

        // creating vm images and its resource needs

        // for a VM, first we need a virtual machine image, it needs 100 instruction for creating a VM from it, and it needs 1 GB of free space on a PM
        VirtualAppliance va = new VirtualAppliance("va", 100, 0, false, 1073741824L);

        // we have to define the resource needs of the VM, we use 4-8 CPU cores, 0.001 core processing power and 4 GB RAM
        AlterableResourceConstraints arc1 = new AlterableResourceConstraints(8,0.001,4294967296L);
        AlterableResourceConstraints arc2 = new AlterableResourceConstraints(4,0.001,4294967296L);

        // and now we make join them into 1 object called Instance with different hourly price (~0.036$)
        new Instance(va,arc1,0.00000001,"instance1");
        new Instance(va,arc2,0.000000015,"instance2");

        String cloudfile = ScenarioBase.resourcePath+"LPDS_original.xml";
        String fogfile = ScenarioBase.resourcePath+"LPDS_Fog_T1.xml";

        // we create our clouds using predefined cloud schema
        ComputingAppliance cloud1 = new ComputingAppliance(cloudfile, "cloud1",new GeoLocation(47.497913, 19.040236),500*1000);
        //ComputingAppliance cloud2 = new ComputingAppliance(cloudfile, "cloud2",new GeoLocation(44.2131, 87.3345),500*1000);

        // creating the cloud application modules: 5 minutes frequency, 175kB task size and max. 2400 instruction / task
        Application ca1 = new Application(5*60*1000, 256000, "instance1", "Cloud-app1", 2400.0, 1, "random", false);
        //Application ca2 = new Application(5*60*1000, 256000, "instance1", "Cloud-app2", 2400.0, 1, "random", false);


        // we create our fog nodes using predefined fog schema
        ComputingAppliance fog1 = new ComputingAppliance(fogfile, "fog1",new GeoLocation(46.244935, 20.149449),200);
        ComputingAppliance fog2 = new ComputingAppliance(fogfile, "fog2",new GeoLocation(46.245289, 20.148821),150);
        ComputingAppliance fog3 = new ComputingAppliance(fogfile, "fog3",new GeoLocation(46.245902, 20.150141),500);
        ComputingAppliance fog4 = new ComputingAppliance(fogfile, "fog4",new GeoLocation(46.247065, 20.150464),50);
        ComputingAppliance fog5 = new ComputingAppliance(fogfile, "fog5",new GeoLocation(46.245927, 20.148240),150);
        ComputingAppliance largeFog1 = new ComputingAppliance(fogfile, "largerFog1",new GeoLocation(46.247828, 20.145182),1000*3);
        ComputingAppliance largeFog2 = new ComputingAppliance(fogfile, "largerFog2",new GeoLocation(46.243685, 20.138371),1000*3);

        System.out.println(fog1.iaas.repositories.get(0).getLatencies());
        System.out.println(cloud1.iaas.repositories.get(0).getLatencies());

        fog1.setLatency(cloud1, 8);
        fog2.setLatency(cloud1, 8);
        fog3.setLatency(cloud1, 8);
        fog4.setLatency(cloud1, 8);
        fog5.setLatency(cloud1, 8);
        largeFog1.setLatency(cloud1, 8);
        largeFog2.setLatency(cloud1, 8);

        System.out.println(fog1.iaas.repositories.get(0).getLatencies());
        System.out.println(cloud1.iaas.repositories.get(0).getLatencies());

        fog1.setParentNode(cloud1);
        fog1.addNeighbour(fog2, fog3, fog4, fog5, largeFog1, largeFog2);
        fog2.addNeighbour(fog3);
        fog3.addNeighbour(fog4);
        fog4.addNeighbour(fog5);
        fog1.addNeighbour(largeFog1, largeFog2);
        fog2.addNeighbour(largeFog1, largeFog2);
        fog3.addNeighbour(largeFog1, largeFog2);
        fog4.addNeighbour(largeFog1, largeFog2);
        largeFog1.addNeighbour(largeFog2);
        largeFog1.setParentNode(cloud1);
        largeFog2.setParentNode(cloud1);

        // creating the fog application modules: 5 minutes frequency, 175kB task size and max. 2400 instruction / task
        Application fa1 = new Application(1*60*1000, 179200, "instance2", "Fog-app3", 2400.0, 1, "random", true);
        Application fa2 = new Application(1*60*1000, 179200, "instance2", "Fog-app4", 2400.0, 1, "random", true);
        Application fa3 = new Application(2*60*1000, 179200, "instance2", "Fog-app5", 2400.0, 1, "random", true);
        Application fa4 = new Application(1*60*1000, 179200, "instance2", "Fog-app6", 2400.0, 1, "random", true);
        Application fa5 = new Application(2*60*1000, 179200, "instance2", "Fog-app6", 2400.0, 1, "random", true);
        Application largeFa1 = new Application(5*60*1000, 179200, "instance2", "Fog-app6", 2400.0, 1, "random", true);
        Application largeFa2 = new Application(5*60*1000, 179200, "instance2", "Fog-app6", 2400.0, 1, "random", true);

//        fa1.setTaskScheduler(new PrioritizedTaskScheduler(fa1, 2, 4));
//        fa2.setTaskScheduler(new PrioritizedTaskScheduler(fa2, 2, 4));
//        fa3.setTaskScheduler(new PrioritizedTaskScheduler(fa3, 2, 4));
//        fa4.setTaskScheduler(new PrioritizedTaskScheduler(fa4, 2, 4));
//        ca1.setTaskScheduler(new PrioritizedTaskScheduler(ca1, 2, 4));
        //ca2.setTaskScheduler(new PrioritizedTaskScheduler(ca2, 2, 4));



        cloud1.addApplication(ca1);
        //cloud2.addApplication(ca2);
        fog1.addApplication(fa1);
        fog2.addApplication(fa2);
        fog3.addApplication(fa3);
        fog4.addApplication(fa4);
        fog5.addApplication(fa5);
        largeFog1.addApplication(largeFa1);
        largeFog2.addApplication(largeFa2);



        // we create 1000 smart device with random installation strategy, 10kB storage, 10000 bandwidth,
        // 24 hours long running time, 50 bytes of generated data by each sensor, each smart device has 5 sensor,
        // and the frequency is 1 minute, last 3 zero parameters are for the geolocation, but it is now irrelevant for us
        //static devices
        for(int i=0;i<250;i++) {


            if(i<70) {
                Device.DeviceNetwork dn = new Device.DeviceNetwork(10, 10240, 10000, 10000, 200000000, "dnRepository" + i, null, null);
                Station s = new Station(10 * 60 * 1000, 55, dn, 0, 24 * 60 * 60 * 1000, 100, "random", new SensorCharacteristics(4, 1000L * 60 * 60 * 24 * 365, 6000, 30000, 0.7, 0.1, 10, 1), 6 * 1000, new GeoLocation(46.245286, 20.149618), null);
                s.setActuator(new Actuator(new ActuatorRandomStrategy(), 10, s));
            } else if(i < 140) {
                Device.DeviceNetwork dn = new Device.DeviceNetwork(10, 10240, 10000, 10000, 200000000, "dnRepository" + i, null, null);
                Station s = new Station(10 * 60 * 1000, 50, dn, 0, 24 * 60 * 60 * 1000, 200, "random", new SensorCharacteristics(2, 1000L * 60 * 60 * 24 * 365 * 2, 30000, 60000, 0.7, 0.5, 30, 1), 30 * 1000, new GeoLocation(46.245308, 20.148848), null);
                s.setActuator(new Actuator(new ActuatorRandomStrategy(), 10, s));
            } else if( i < 200 ) {
                Device.DeviceNetwork dn = new Device.DeviceNetwork(10, 10240, 10000, 10000, 200000000, "dnRepository" + i, null, null);
                Station s = new Station(5 * 60 * 1000, 27, dn, 0, 24 * 60 * 60 * 1000, 250, "random", new SensorCharacteristics(3, 1000L * 60 * 60 * 24 * 182, 60000, 90000, 0.7, 0.8, 10, 1), 60 * 1000, new GeoLocation(46.245946, 20.150157), null);
                s.setActuator(new Actuator(new ActuatorRandomStrategy(), 10, s));
            } else {
                Device.DeviceNetwork dn = new Device.DeviceNetwork(10, 10240, 10000, 10000, 200000000, "dnRepository" + i, null, null);
                Station s = new Station(10 * 60 * 1000, 60, dn, 0, 24 * 60 * 60 * 1000, 150, "random", new SensorCharacteristics(3, 1000L * 60 * 60 * 24 * 365 * 3, 30000, 60000, 0.7, 0.5, 10, 1), 30 * 1000, new GeoLocation(46.247058, 20.150491), null);
                s.setActuator(new Actuator(new ActuatorRandomStrategy(), 10, s));
            }

        }
        double rangeMin = -0.008;
        double rangeMax = 0.008;
        //mobile devices
        for(int i=0; i<120; i++) {

            if (i < 50) {
                Device.DeviceNetwork dn = new Device.DeviceNetwork(10, 10240, 10000, 10000, 200000000, "dnRepository" + i, null, null);
                Station s = new Station(10 * 60 * 1000, 55, dn, 0, 24 * 60 * 60 * 1000, 100, "random", new SensorCharacteristics(4, 1000L * 60 * 60 * 24 * 365, 6000, 30000, 0.7, 0.3, 40, 1), 30 * 1000, new GeoLocation(46.245286, 20.149618), new RandomMobilityStrategy(new GeoLocation(46.245286, 20.149618), 500, 0.0008));
                s.setActuator(new Actuator(new ActuatorRandomStrategy(), 5, s));


            } else if(i<100) {
                Device.DeviceNetwork dn = new Device.DeviceNetwork(10, 10240, 10000, 10000, 200000000, "dnRepository" + i, null, null);
                Station s = new Station(10 * 60 * 1000, 55, dn, 0, 24 * 60 * 60 * 1000, 100, "random", new SensorCharacteristics(4, 1000L * 60 * 60 * 24 * 365, 6000, 30000, 0.7, 0.3, 40, 1), 30 * 1000, new GeoLocation(46.245286, 20.149618), new RandomMobilityStrategy(new GeoLocation(46.245286, 20.149618), 500, 0.0008));
                s.setActuator(new Actuator(new ActuatorRandomStrategy(), 5, s));
            } else {
                Random r = new Random();
                double randLat = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
                double randLon = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
                GeoLocation startPos = new GeoLocation(46.245286, 20.149618);
                GeoLocation dest = new GeoLocation(startPos.getLatitude() + randLat, startPos.getLongitude()+randLon);
                Device.DeviceNetwork dn = new Device.DeviceNetwork(10, 10240, 10000, 10000, 200000000, "dnRepository" + i, null, null);
                Station s = new Station(10 * 60 * 1000, 55, dn, 0, 24 * 60 * 60 * 1000, 100, "random", new SensorCharacteristics(4, 1000L * 60 * 60 * 24 * 365, 6000, 30000, 0.7, 0, 40, 1), 3 * 1000, startPos, new LinearMobilityStrategy(startPos, 0.166, dest));
                s.setActuator(new Actuator(new ActuatorRandomStrategy(), 5, s));
            }

        }

        // Setting up the IoT pricing
        ArrayList<BluemixProvider.Bluemix> bmList = new ArrayList<BluemixProvider.Bluemix>();
        bmList.add(new BluemixProvider.Bluemix(0,499999,0.00097));
        bmList.add(new BluemixProvider.Bluemix(450000,6999999,0.00068));
        bmList.add(new BluemixProvider.Bluemix(7000000,Long.MAX_VALUE,0.00014));

        new BluemixProvider(bmList,ca1); new BluemixProvider(bmList,fa1);
        new BluemixProvider(bmList,fa2); new BluemixProvider(bmList,fa3); new BluemixProvider(bmList,fa4);

        new AmazonProvider(5,1000000,512,ca1);  new AmazonProvider(5,1000000,512,fa1);
        new AmazonProvider(5,1000000,512,fa2); new AmazonProvider(5,1000000,512,fa3); new AmazonProvider(5,1000000,512,fa4);

        new AzureProvider(86400000,421.65,6000000,4,ca1);  new AzureProvider(86400000,421.65,6000000,4,fa1);
        new AzureProvider(86400000,421.65,6000000,4,fa2);  new AzureProvider(86400000,421.65,6000000,4,fa3); new AzureProvider(86400000,421.65,6000000,4,fa4);

        new OracleProvider(2678400000L,0.93,15000,0.02344,1000, ca1);
        new OracleProvider(2678400000L,0.93,15000,0.02344,1000, fa1); new OracleProvider(2678400000L,0.93,15000,0.02344,1000, fa2);
        new OracleProvider(2678400000L,0.93,15000,0.02344,1000, fa3); new OracleProvider(2678400000L,0.93,15000,0.02344,1000, fa4);

        // we start the simulation
        long starttime = System.nanoTime();
        Timed.simulateUntilLastEvent();
        long stopttime = System.nanoTime();

        // Print some information to the monitor / in file
        TimelineGenerator.generate();
        ScenarioBase.printInformation((stopttime-starttime),false);

    }
}
