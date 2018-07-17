package hu.uszeged.xml.model;

import java.io.File;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import hu.uszeged.inf.iot.simulator.entities.Application;

@XmlRootElement( name = "application" )
public class ApplicationModel {

		long tasksize;
		String cloud;
		String instance;
		String iotPricing;

		@Override
		public String toString() {
			return "ApplicationModel [tasksize=" + tasksize + ", cloud=" + cloud + ", instance=" + instance
					+ ", iotPricing=" + iotPricing + "]";
		}

		@XmlAttribute( name = "tasksize", required = true )
		public void setTasksize(long tasksize) {
			this.tasksize = tasksize;
		}

		@XmlElement( name = "cloud" )
		public void setCloud(String cloud) {
			this.cloud = cloud;
		}

		@XmlElement( name = "instance" )
		public void setInstance(String instance) {
			this.instance = instance;
		}

		@XmlElement( name = "iot-pricing" )
		public void setIotPricing(String iotPricing) {
			this.iotPricing = iotPricing;
		}
		
		public static ArrayList<Application> loadApplicationXML(String appfile)throws JAXBException{
				 File file = new File( appfile);
				 JAXBContext jaxbContext = JAXBContext.newInstance( ApplicationsModel.class );
				 Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				 ApplicationsModel app = (ApplicationsModel)jaxbUnmarshaller.unmarshal( file );
				 // System.out.println( app );
				 
				 return app.applicationList;
		}
			
			 

		
}
