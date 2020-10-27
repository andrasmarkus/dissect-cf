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

public class TransportScenario {

    public static void main(String[] args) throws Exception {

        VirtualAppliance va = new VirtualAppliance("va", 100, 0, false, 1073741824L);

        AlterableResourceConstraints arc1 = new AlterableResourceConstraints(8,0.001,4294967296L);
        AlterableResourceConstraints arc2 = new AlterableResourceConstraints(4,0.001,4294967296L);

        new Instance(va,arc1,0.00000001,"instance1");
        new Instance(va,arc2,0.000000015,"instance2");

        String cloudfile = ScenarioBase.resourcePath+"\\fuzzy\\LPDS_frankfurt.xml";
        String fogfile = ScenarioBase.resourcePath+"\\fuzzy\\LPDS_athen.xml";

        ComputingAppliance cloud1 = new ComputingAppliance(cloudfile, "cloud1",new GeoLocation(47.497913, 19.040236),500*1000);

        Application ca1 = new Application(5*60*1000, 256000, "instance1", "Cloud-app1", 2400.0, 1, "random", false);


        ComputingAppliance fog1 = new ComputingAppliance(fogfile, "fog1",new GeoLocation(46.670408, 21.034976),50 * 1000);
        ComputingAppliance fog2 = new ComputingAppliance(fogfile, "fog2",new GeoLocation(47.118845, 20.213303),50 * 1000);
        ComputingAppliance fog3 = new ComputingAppliance(fogfile, "fog3",new GeoLocation(46.9332269, 19.659343),50 * 1000);
        ComputingAppliance fog4 = new ComputingAppliance(fogfile, "fog4",new GeoLocation(47.209831, 19.402650),50 * 1000);
        ComputingAppliance fog5 = new ComputingAppliance(fogfile, "fog5",new GeoLocation(47.476262, 19.025969),50 * 1000);
        ComputingAppliance fog6 = new ComputingAppliance(fogfile, "fog6",new GeoLocation(47.087735, 17.966967),50 * 1000);
        ComputingAppliance fog7 = new ComputingAppliance(fogfile, "fog7",new GeoLocation(46.363608, 17.843199),50 * 1000);
        ComputingAppliance fog8 = new ComputingAppliance(fogfile, "fog8",new GeoLocation(46.173927, 18.994432),50 * 1000);
        ComputingAppliance fog9 = new ComputingAppliance(fogfile, "fog9",new GeoLocation(46.245436, 20.098563),50 * 1000);


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

        Application fa1 = new Application(60*1000, 179200, "instance2", "Fog-app3", 2400.0, 1, "random", true);
        Application fa2 = new Application(60*1000, 179200, "instance2", "Fog-app4", 2400.0, 1, "random", true);
        Application fa3 = new Application(2*60*1000, 179200, "instance2", "Fog-app5", 2400.0, 1, "random", true);
        Application fa4 = new Application(60*1000, 179200, "instance2", "Fog-app6", 2400.0, 1, "random", true);
        Application fa5 = new Application(2*60*1000, 179200, "instance2", "Fog-app6", 2400.0, 1, "random", true);
        Application fa6 = new Application(2*60*1000, 179200, "instance2", "Fog-app6", 2400.0, 1, "random", true);
        Application fa7 = new Application(2*60*1000, 179200, "instance2", "Fog-app6", 2400.0, 1, "random", true);
        Application fa8 = new Application(2*60*1000, 179200, "instance2", "Fog-app6", 2400.0, 1, "random", true);
        Application fa9 = new Application(2*60*1000, 179200, "instance2", "Fog-app6", 2400.0, 1, "random", true);




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
        GeoLocation pos2 = new GeoLocation(46.682789, 21.079223);
        GeoLocation pos3 = new GeoLocation(46.692550, 21.017737);
        GeoLocation pos4 = new GeoLocation(46.705530, 21.009643);
        GeoLocation pos5 = new GeoLocation(46.715346, 20.938908);
        GeoLocation pos6 = new GeoLocation(46.739226, 20.887427);
        GeoLocation pos7 = new GeoLocation(46.739293, 20.781757);
        GeoLocation pos8 = new GeoLocation(46.809668, 20.683342);
        GeoLocation pos9 = new GeoLocation(46.825378, 20.460188);
        GeoLocation pos10 = new GeoLocation(46.813190, 20.398357);
        GeoLocation pos11 = new GeoLocation(46.870029, 20.276358);
        GeoLocation pos12 = new GeoLocation(47.041881, 20.285051);
        GeoLocation pos13 = new GeoLocation(47.152297, 20.190951);
        GeoLocation pos14 = new GeoLocation(46.917578, 19.838919);
        GeoLocation pos15 = new GeoLocation(46.921523, 19.675426);
        GeoLocation pos16 = new GeoLocation(47.459412, 19.105890);
        GeoLocation pos17 = new GeoLocation(47.494533, 18.576623);
        GeoLocation pos18 = new GeoLocation(47.577666, 18.460920);
        GeoLocation pos19 = new GeoLocation(47.505288, 18.334970);
        GeoLocation pos20 = new GeoLocation(47.214352, 18.397256);
        GeoLocation pos21 = new GeoLocation(47.102618, 17.950843);
        GeoLocation pos22 = new GeoLocation(46.998067, 18.179811);
        GeoLocation pos23 = new GeoLocation(46.880423, 17.971525);
        GeoLocation pos24 = new GeoLocation(46.815442, 17.904499);
        GeoLocation pos25 = new GeoLocation(46.785883, 17.752671);
        GeoLocation pos26 = new GeoLocation(46.372119, 17.778896);
        GeoLocation pos27 = new GeoLocation(46.400044, 18.158641);
        GeoLocation pos28 = new GeoLocation(46.490723, 18.403493);
        GeoLocation pos29 = new GeoLocation(46.385293, 18.725286);
        GeoLocation pos30 = new GeoLocation(46.348368, 18.971698);
        GeoLocation pos31 = new GeoLocation(46.185693, 18.984941);
        GeoLocation pos32 = new GeoLocation(46.177000, 19.283765);
        GeoLocation pos33 = new GeoLocation(46.276617, 19.5572250);
        GeoLocation pos34 = new GeoLocation(46.213740, 19.786595);
        GeoLocation pos35 = new GeoLocation(46.253738, 20.117115);
        GeoLocation pos36 = new GeoLocation(46.384680, 20.255177);
        GeoLocation pos37 = new GeoLocation(46.451932, 20.309972);
        GeoLocation pos38 = new GeoLocation(46.448517, 20.384458);
        GeoLocation pos39 = new GeoLocation(46.565530, 20.643327);
        GeoLocation pos40 = new GeoLocation(46.678076, 21.047313);

        int numberOfDevices=0;

        try {
            if(args.length > 0) {
                numberOfDevices = Integer.parseInt(args[0]);
            }
        }catch (NumberFormatException e){
            e.printStackTrace();
        }

        Random random = new Random();
        for (int i = 0; i < 2* 365; i++ ) {
            long startTime = i * 12 * 60* 60* 1000L;
            long stopTime = startTime + 12 * 60 * 60 * 1000L;
            for(int j = 0 ; j <numberOfDevices; j++) {
                double actuatorRatio = 0.2;
                double fogRatio = random.nextDouble();
                Device.DeviceNetwork dn = new Device.DeviceNetwork(10, 10240, 10000, 10000, 200000000, "dnRepository" + i+"-"+j, null, null);
                Station s = new Station(10 * 60 * 1000, 50, dn, startTime, stopTime, 150, "distance", new SensorCharacteristics(3, 1000L * 60 * 60 * 24 * 365, 5 * 60 *1000, 5 * 60 * 10000, fogRatio, actuatorRatio, 50, 1), 5 * 60 * 1000, pos1, new LinearMobilityStrategy(pos1, 0.022,
                        pos2, pos3, pos4, pos5, pos6, pos6, pos7, pos8, pos9, pos10, pos11, pos12, pos13, pos14, pos15, pos16, pos17, pos18, pos19, pos20,
                        pos21, pos22, pos23, pos24, pos25, pos26, pos27, pos28, pos29, pos30, pos31, pos32, pos33, pos34, pos35, pos36, pos37, pos38, pos39, pos40, pos1));
                s.setActuator(new Actuator(new ActuatorRandomStrategy(), 5, s));
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
