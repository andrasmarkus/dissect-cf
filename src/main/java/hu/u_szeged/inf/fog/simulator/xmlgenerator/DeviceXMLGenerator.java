package hu.u_szeged.inf.fog.simulator.xmlgenerator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Random;

public class DeviceXMLGenerator {

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		
		PrintWriter writer = new PrintWriter("stations.xml", "UTF-8");
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		writer.println("<devices>");
		
		for(int j=0;j<6;j++) {
			long starttime = j*4*60*60*1000;
					
			for(int i=0;i<2000;i++) {
				int x,y;
				Random holdGenerator = new Random();
				x = holdGenerator.nextInt(49)-8;
				y = holdGenerator.nextInt(21)+40;
							
				writer.println("	<device starttime='"+starttime+"' stoptime='"+86400000+"' number='1' filesize='50'>");
				writer.println("		<name>station-"+i+j+"</name>");
				writer.println("		<latency>50</latency>");
				writer.println("		<freq>60000</freq>");
				writer.println("		<sensor>5</sensor>");
				writer.println("		<maxinbw>10240</maxinbw>");
				writer.println("		<maxoutbw>10240</maxoutbw>");
				writer.println("		<diskbw>10240</diskbw>");
				writer.println("		<reposize>10240</reposize>");
				writer.println("		<strategy>distance</strategy>");
				writer.println("		<xCoord>"+x+"</xCoord>");
				writer.println("		<yCoord>"+y+"</yCoord>");
				writer.println("	</device>");
			}
		}
		
		
		
		writer.println("</devices>");
		writer.close();
	}
}
