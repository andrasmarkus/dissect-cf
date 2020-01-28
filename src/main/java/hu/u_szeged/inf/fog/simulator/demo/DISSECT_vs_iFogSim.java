package hu.u_szeged.inf.fog.simulator.demo;


import java.util.HashMap;
import java.util.Map;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.u_szeged.inf.fog.simulator.iot.Station;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;
import hu.u_szeged.inf.fog.simulator.providers.Instance;
import hu.u_szeged.inf.fog.simulator.util.TimelineGenerator;

public class DISSECT_vs_iFogSim {
	
	private static String SCENARIO = "CompareTo-iFogSim";
	
	//private static String SCENARIO = "Fog-vs-Cloud";
	
	public static void main(String[] args) throws Exception {
			
		/* Fog vs. Cloud
		String fogfile_type1=ScenarioBase.resourcePath+"/"+ SCENARIO +"/LPDSFog_type1.xml"; 
		String fogfile_type2=ScenarioBase.resourcePath+"/"+ SCENARIO +"/LPDSFog_type2.xml"; 
		String cloudfile=ScenarioBase.resourcePath+"/"+ SCENARIO +"/LPDSCloud.xml";
		String appliancefile = ScenarioBase.resourcePath+"/"+ SCENARIO +"/Scenario_3_45_160.xml";
		String stationfile = ScenarioBase.resourcePath+"/"+ SCENARIO +"/Stations.xml";
		String instancefile=ScenarioBase.resourcePath+"/"+ SCENARIO +"/InstanceIOT.xml";
		*/
		
		// iFogSim comparison
		String fogfile_type1=ScenarioBase.resourcePath+"/"+ SCENARIO +"/1CPUStrong_Fog.xml"; 
		String fogfile_type2=ScenarioBase.resourcePath+"/"+ SCENARIO +"/3CPUStrong_Fog.xml"; 
		String cloudfile=ScenarioBase.resourcePath+"/"+ SCENARIO +"/45CPUStrong_Cloud.xml";
		String appliancefile = ScenarioBase.resourcePath+"/"+ SCENARIO +"/Scenario_1_4_20.xml";
		String stationfile = ScenarioBase.resourcePath+"/"+ SCENARIO +"/Stations_80.xml";
		String instancefile=ScenarioBase.resourcePath+"/"+ SCENARIO +"/Instance.xml";

		// we map the files to the IDs
		Map<String, String> iaasloaders = new HashMap<String, String>();
		iaasloaders.put("cloud", cloudfile);
		iaasloaders.put("fog_type1", fogfile_type1);
		iaasloaders.put("fog_type2", fogfile_type2);
		
		// we call the loader functions
		Instance.loadInstance(instancefile);
		//ComputingAppliance.loadAppliances(appliancefile, iaasloaders);
		//Station.loadDevice(stationfile);
		
		// Start the simulation
		long starttime = System.nanoTime();
		Timed.simulateUntilLastEvent();
		long stopttime = System.nanoTime();
				
		// Print some information to the monitor / in file
		TimelineGenerator.generate();
		ScenarioBase.printInformation((stopttime-starttime),true);
	}

}
