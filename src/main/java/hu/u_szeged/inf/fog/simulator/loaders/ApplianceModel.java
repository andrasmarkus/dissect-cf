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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;



@XmlRootElement( name = "appliance")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ApplianceModel {
	
	public String name;
	
	public double xcoord;
	
	public double ycoord;
		
	public String file;
	
	public ArrayList<ApplicationModel> applications;
	
	public ArrayList<NeigbourAppliancesModel> neighbours; 
	
	
	@Override
	public String toString() {
		return "ApplianceModel [name=" + name + ", xcoord=" + xcoord + ", ycoord=" + ycoord + ", file=" + file
				+ ", applications=" + applications + ", neighbours=" + neighbours + "]";
	}

	@XmlElement(name = "name" )
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElement(name = "xcoord" )
	public void setXcoord(double xcoord) {
		this.xcoord = xcoord;
	}
	
	@XmlElement(name = "ycoord" )
	public void setYcoord(double ycoord) {
		this.ycoord = ycoord;
	}
	
	


	public ArrayList<ApplicationModel> getApplications(){
		return applications;
	}
	
	@XmlElementWrapper( name = "applications" )
	@XmlElement( name = "application")
	public void setApplications(ArrayList<ApplicationModel> applications) {
		this.applications = applications;
	}
	
	public void add( ApplicationModel applicationModel) {
		if ( this.applications == null) {
			this.applications = new ArrayList<ApplicationModel>();
		}
		this.applications.add(applicationModel);
	}
	
	public ArrayList<NeigbourAppliancesModel> getNeighbourAppliances(){
		return neighbours;
	}
	
	@XmlElementWrapper( name = "neighbours")
	@XmlElement( name = "neighbour")
	public void setNeighbourAppliances(ArrayList<NeigbourAppliancesModel> neighbours) {
		this.neighbours = neighbours;
	}
	
	public void add ( NeigbourAppliancesModel device) {
		if (this.neighbours == null) {
			this.neighbours = new ArrayList<NeigbourAppliancesModel>();
		}
		this.neighbours.add(device);
	}
	
	@XmlElement(name = "file")
	public void setFile(String file) {
		this.file = file;
		
	}
	public static ArrayList<ApplianceModel> loadAppliancesXML(String appliancefile) throws JAXBException {
		File file = new File(appliancefile);
		JAXBContext jaxbContext = JAXBContext.newInstance( AppliancesModel.class );
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		AppliancesModel appliances = (AppliancesModel) jaxbUnmarshaller.unmarshal( file );
		return appliances.applianceList;
	}
	
}
