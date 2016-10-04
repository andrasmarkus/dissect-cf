package iot.extension;

import java.io.File;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import at.ac.uibk.dps.cloud.simulator.test.simple.cloud.vmscheduler.BasicSchedulingTest.AssertFulScheduler;

import org.w3c.dom.Node;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import iot.extension.Application.VmCollector;
import iot.extension.Station.Stationdata;

public class Scenario {

	/**
	 * 
	 * @param va
	 * @param filesize
	 * @param datafile
	 * @throws Exception
	 */
	public Scenario(VirtualAppliance va, String datafile,String cloudfile,int print) throws Exception {
		long tasksize=-1;
		if (va == null) {
			new Cloud(Cloud.v,cloudfile); // IaaS letrehozasa defaulta VA-val
		} else {
			new Cloud(va,cloudfile);
		}
		if (datafile.isEmpty()) {
			System.out.println("Datafile nem lehet null");
			System.exit(0);
		} else {
			File fXmlFile = new File(datafile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			NodeList NL = doc.getElementsByTagName("gather");
			tasksize = Long.parseLong(NL.item(0).getAttributes().item(0).getNodeValue());
			 if(tasksize<=0){
				 System.out.println("rossz tasksize ertek! ");
					System.exit(0);
			 }
			NodeList nList = doc.getElementsByTagName("Station");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;

					final long freq = Long.parseLong(eElement.getElementsByTagName("freq").item(0).getTextContent());
					if (freq <= 0) {
						System.out.println("rossz freq ertek! ");
						System.exit(0);
					}
					final long time = Long.parseLong(eElement.getElementsByTagName("time").item(0).getTextContent());
					if (time < -1) {
						System.out.println("rossz time ertek! ");
						System.exit(0);
					}
					final long starttime = Long.parseLong(
							eElement.getElementsByTagName("time").item(0).getAttributes().item(0).getNodeValue());
					final long stoptime = Long.parseLong(
							eElement.getElementsByTagName("time").item(0).getAttributes().item(1).getNodeValue());
					if (starttime < 0 || (starttime > time && starttime < -1) || starttime > stoptime) {
						System.out.println("rossz starttime/stoptime ertek! ");
						System.exit(0);
					}
					final int snumber = Integer
							.parseInt(eElement.getElementsByTagName("snumber").item(0).getTextContent());
					if (snumber <= 0) {
						System.out.println("rossz snumber ertek! ");
						System.exit(0);
					}
					final int filesize=Integer.parseInt(eElement.getElementsByTagName("snumber").item(0)
							.getAttributes().item(0).getNodeValue());
					if (filesize < 1) {
						System.out.println("rossz filesize ertek! ");
						System.exit(0);
					}
					
					final long maxinbw = Long
							.parseLong(eElement.getElementsByTagName("maxinbw").item(0).getTextContent());
					if (maxinbw <= 0) {
						System.out.println("rossz maxinbw ertek! ");
						System.exit(0);
					}
					final long maxoutbw = Long
							.parseLong(eElement.getElementsByTagName("maxoutbw").item(0).getTextContent());
					if (maxoutbw <= 0) {
						System.out.println("rossz maxoutbw ertek! ");
						System.exit(0);
					}
					final long diskbw = Long
							.parseLong(eElement.getElementsByTagName("diskbw").item(0).getTextContent());
					if (diskbw <= 0) {
						System.out.println("rossz diskbw ertek! ");
						System.exit(0);
					}
					final long reposize = Long
							.parseLong(eElement.getElementsByTagName("reposize").item(0).getTextContent());
					if (reposize <= 0) {
						System.out.println("rossz reposize ertek! ");
						System.exit(0);
					}
					final int ratio = Integer.parseInt(eElement.getElementsByTagName("ratio").item(0).getTextContent());
					if (ratio < 1) {
						System.out.println("rossz ratio ertek! ");
						System.exit(0);
					}
					
					final int stationnumber=Integer.parseInt(eElement.getElementsByTagName("name")
								.item(0).getAttributes().item(0).getNodeValue());
					if (stationnumber < 1) {
						System.out.println("rossz stationnumber ertek! ");
						System.exit(0);
					}
					for(int i=0;i<stationnumber;i++){
						Stationdata sd = new Stationdata(time, starttime, stoptime, filesize, snumber, freq,
								eElement.getElementsByTagName("name").item(0).getTextContent()+" "+i,
								eElement.getElementsByTagName("torepo").item(0).getTextContent(), ratio);
						Station.stations.add(new Station(maxinbw, maxoutbw, diskbw, reposize, sd));
					}

					}
				}
			}
			
			if(tasksize!=-1){
				Application.getInstance(60000,tasksize,true,1);
				Timed.simulateUntilLastEvent();
			}
						
			
			// hasznos infok:
			if(print==1){
				
				int i=0;
				int j=0;
				for(VmCollector vmcl : Application.vmlist){
					if(vmcl.worked){
						j++;
						i+=vmcl.tasknumber;
					}
				}
				System.out.println("~~~~~~~~~~~~");
				for(VmCollector vmcl : Application.vmlist){
					if(vmcl.worked && vmcl.tasknumber>0){
						System.out.println(vmcl.vm+" : "+vmcl.tasknumber);
					}
				}
				System.out.println("~~~~~~~~~~~~");
				System.out.println("VM: "+j + " tasks: "+i);
				System.out.println("~~~~~~~~~~~~");
				System.out.println("All filesize: "+Station.allstationsize);
				System.out.println("~~~~~~~~~~~~");
				System.out.println(Cloud.iaas.repositories.toString());
				System.out.println("~~~~~~~~~~~~");
				for (PhysicalMachine p : Cloud.iaas.machines) {
					if (/* !p.isHostingVMs() */ p.localDisk.getFreeStorageCapacity() != p.localDisk
							.getMaxStorageCapacity()) {
						System.out.println(p);
					}
				}
				/*System.out.println("~~~~~~~~~~~~");
				for(Station s: Station.stations){
					System.out.println(s);
				}*/
				System.out.println("~~~~~~~~~~~~");
				PrintWriter writer = new PrintWriter("tasks.csv", "UTF-8");	
				for( Long s : Application.hmap.keySet() )
				{
					writer.println(s + "," + Application.hmap.get(s));
				}
				writer.close();
			}
			
		}

		/**
		 * 
		 * @param args Az elso argumentumkent adhato meg a Station-okat leiro XML eleresi utvonala
		 * 			masodik argumentumkent az IaaS-t leiro XML eleresi utvonala
		 * 			harmadik argumentumkent egy szam, ami ha 1-es, akkor van kiiratas
		 * @throws Exception
		 */
		public static void main(String[] args) throws Exception {
	
			String datafile=args[0];
			String cloudfile=args[1];
			int print=Integer.parseInt(args[2]);
			new Scenario(null,datafile,cloudfile,print);	
			
		}
}

	

