package hu.u_szeged.inf.fog.simulator.demo;


import java.util.HashMap;
import java.util.Map;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.u_szeged.inf.fog.simulator.demo.ScenarioBase;
import hu.u_szeged.inf.fog.simulator.iot.Station;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;
import hu.u_szeged.inf.fog.simulator.providers.Instance;
import hu.u_szeged.inf.fog.simulator.providers.Provider;
import hu.u_szeged.inf.fog.simulator.util.TimelineGenerator;

public class ScaledFogSimulation {
	
	public static void main(String[] args) throws Exception {

		// cloud and fog nodes
		String cloudfile=ScenarioBase.resourcePath+"LPDS_original.xml";
		String fogfile1=ScenarioBase.resourcePath+"LPDS_Fog_T1.xml"; 
		String fogfile2=ScenarioBase.resourcePath+"LPDS_Fog_T2.xml"; 
		
		// application modules
		String appliancefile = ScenarioBase.resourcePath+"new.xml";
		
		// stations  (100 000 * 5 sensors)
		String stationfile = ScenarioBase.resourcePath+"Stations.xml";
		
		// instances and providers
		String instancefile=ScenarioBase.resourcePath+"Instances.xml";
		String providerfile=ScenarioBase.resourcePath+"Providers.xml";

		// we map the files to the IDs
		Map<String, String> iaasloaders = new HashMap<String, String>();
		iaasloaders.put("cloud", cloudfile);
		iaasloaders.put("fogfile1", fogfile1);
		iaasloaders.put("fogfile2", fogfile2);
		
		// we call the loader functions
		Instance.loadInstance(instancefile);
		ComputingAppliance.loadAppliance(appliancefile, iaasloaders);
		Station.loadDevice(stationfile);
		Provider.loadProvider(providerfile); 

		// Start the simulation
		long starttime = System.nanoTime();
		Timed.simulateUntilLastEvent();
		long stopttime = System.nanoTime();
		
		// Print some information to the monitor / in file
		TimelineGenerator.generate();
		ScenarioBase.printInformation((stopttime-starttime),true);
	}
}
