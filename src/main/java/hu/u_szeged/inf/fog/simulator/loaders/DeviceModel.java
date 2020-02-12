package hu.u_szeged.inf.fog.simulator.loaders;

import java.io.File;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "device" )
@XmlAccessorType(XmlAccessType.PROPERTY) 
public class DeviceModel {

	public String name;
	public int number;
	public long freq;
	public int sensor;
	public long filesize;
	public long starttime;
	public long stoptime;
	public long maxinbw;
	public long maxoutbw;
	public long diskbw;
	public long reposize;
	public int latency;
	public String strategy;
	public double xCoord;
	public double yCoord;
		


		@Override
	public String toString() {
		return "DeviceModel [name=" + name + ", number=" + number + ", freq=" + freq + ", sensor=" + sensor
				+ ", filesize=" + filesize + ", starttime=" + starttime + ", stoptime=" + stoptime + ", maxinbw="
				+ maxinbw + ", maxoutbw=" + maxoutbw + ", diskbw=" + diskbw + ", reposize=" + reposize + ", latency="
				+ latency + ", strategy=" + strategy + ", xCoord=" + xCoord + ", yCoord=" + yCoord + "]";
	}

		@XmlElement( name = "latency" )
		public void setLatency(int latency) {
			this.latency = latency;
		}

		@XmlElement( name = "name" )
		public void setName(String name) {
			this.name = name;
		}


		@XmlElement( name = "freq" )
		public void setFreq(long freq) {
			this.freq = freq;
		}
		
		@XmlElement( name = "sensor" )
		public void setSensor(int sensor) {
			this.sensor = sensor;
		}
		
		@XmlElement( name = "maxinbw" )
		public void setMaxinbw(long maxinbw) {
			this.maxinbw = maxinbw;
		}
		
		@XmlElement( name = "maxoutbw" )
		public void setMaxoutbw(long maxoutbw) {
			this.maxoutbw = maxoutbw;
		}
		
		@XmlElement( name = "diskbw" )
		public void setDiskbw(long diskbw) {
			this.diskbw = diskbw;
		}
		
		
		@XmlElement( name = "reposize" )
		public void setReposize(long reposize) {
			this.reposize = reposize;
		}
		
		
		@XmlElement( name = "strategy" )
		public void setStrategy(String strategy) {
			this.strategy = strategy;
		}
		
		@XmlElement( name = "xCoord" )
		public void setXCoord(double xCoord) {
			this.xCoord = xCoord;
		}
		
		@XmlElement( name = "yCoord" )
		public void setYCoord(double yCoord) {
			this.yCoord = yCoord;
		}

		@XmlAttribute( name = "filesize", required = true )
		public void setfilesize(long filesize) {
			this.filesize = filesize;
		}

		@XmlAttribute( name = "number" )
		public void setNumber(int number) {

			this.number = number;
		}
		@XmlAttribute( name = "starttime", required = true )
		public void setStarttime(long starttime) {
			this.starttime =starttime;
		}

		@XmlAttribute( name = "stoptime", required = true )
		public void setStoptime(long stoptime) {
			this.stoptime = stoptime;
		}
		
		public static ArrayList<DeviceModel>  loadDeviceXML(String stationfile) throws JAXBException {
			  File file = new File( "/home/markus/Documents/dissect-cf/src/main/resources/demo/Stations.xml" );
			  JAXBContext jaxbContext = JAXBContext.newInstance( DevicesModel.class );
			  Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			  DevicesModel device = (DevicesModel)jaxbUnmarshaller.unmarshal( file );
			  return device.deviceList;
	
		}
}
