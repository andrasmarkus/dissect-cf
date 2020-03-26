package hu.u_szeged.inf.fog.simulator.demo;

import java.io.File;
import java.util.concurrent.TimeUnit;

import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.application.Application.VmCollector;
import hu.u_szeged.inf.fog.simulator.iot.Device;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;


public abstract class ScenarioBase {
	
	public final static String resourcePath = new StringBuilder(System.getProperty("user.dir")).
			append(File.separator).
			append("src").
			append(File.separator).
			append("main").
			append(File.separator).
			append("resources").
			append(File.separator).
			append("demo").
			append(File.separator).
			toString();
	
	  static void printInformation(long t,boolean iotpricing) {
		System.out.println("~~Informations about the simulation:~~");
		double totalCost=0.0;
		long generatedData=0,processedData=0,arrivedData=0;
		int usedVM = 0;
		int tasks = 0;
		long timeout=Long.MIN_VALUE;
		long highestApplicationStopTime = Long.MIN_VALUE;
		long highestStationStoptime = Long.MIN_VALUE;
		double bluemix=0;
		double amazon=0;
		double azure=0;
		double oracle=0;
		
		for (ComputingAppliance c : ComputingAppliance.allComputingAppliance) {
			System.out.println("computingAppliance: " + c.name);
			//long highestStationStoptime=Long.MIN_VALUE;
			for (Application a : c.applicationList) {
				System.out.println(a.name);
				totalCost+=a.instance.calculateCloudCost(a.sumOfWorkTime);
				processedData+=a.sumOfProcessedData;
				arrivedData+=a.sumOfArrivedData;
				usedVM+=a.vmManagerlist.size();
				
				
				for (VmCollector vmcl : a.vmManagerlist) {
						tasks += vmcl.taskCounter;
						System.out.println(vmcl.id +" "+vmcl.vm + " tasks: " + vmcl.taskCounter + " worktime: " + vmcl.workingTime + " installed at: "
								+ vmcl.installed+" restarted: "+vmcl.restarted);
				}
				
				
				for(Device d : a.deviceList) {
					generatedData+=d.getSumOfGeneratedData();
						
					if(d.stopTime>highestStationStoptime) {
						highestStationStoptime=d.stopTime;
					}
				}
				
				if (a.stopTime > highestApplicationStopTime) {
					highestApplicationStopTime = a.stopTime;
				}
				
				System.out.println(a.name+" stations: " + a.deviceList.size()+ " cost:"+a.instance.calculateCloudCost(a.sumOfWorkTime));
				if(iotpricing) {
					System.out.println(a.providers);
					bluemix+=a.providers.get(0).cost;	
					amazon+=a.providers.get(1).cost;	
					azure+=a.providers.get(2).cost;	
					oracle+=a.providers.get(3).cost;	
				}

			}
			
			
			
			System.out.println();
		}
		timeout = highestApplicationStopTime - highestStationStoptime;
		
		System.out.println("VMs " + usedVM + " tasks: " + tasks);
		System.out.println("Generated/processed/arrived data: " + generatedData + "/" + processedData+ "/"+arrivedData+ " bytes (~"+(arrivedData/1024/1024)+" MB)");
		System.out.println("Cloud cost: "+totalCost);
		System.out.println("IoT cost: Bluemix: "+bluemix+ " Amazon: "+ amazon +" Azure: "+azure+ " Oracle: "+ oracle);
		System.out.println("Network: "+TimeUnit.SECONDS.convert(Application.sumOfOnNetwork, TimeUnit.MILLISECONDS)+ " seconds");
		System.out.println("Timeout: "+((double)timeout/1000/60) +" minutes");
		System.out.println("Runtime: "+TimeUnit.SECONDS.convert(t, TimeUnit.NANOSECONDS)+ " seconds");
		

	}
}