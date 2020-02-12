package hu.u_szeged.inf.fog.simulator.loaders;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement( name = "device" )
@XmlAccessorType(XmlAccessType.PROPERTY)
public class NeigbourAppliancesModel {
	 
	public String name;
	public int latency;
	public String parent;
		
	
	@Override
	public String toString() {
		return "NeigbourAppliancesModel [name=" + name + ", latency=" + latency + ", parent=" + parent + "]";
	}

	@XmlElement( name = "name")
	public void setName(String name) {
		this.name = name;
	}

	@XmlElement( name = "latency")
	public void setLatency(int latency) {
		this.latency = latency;
	}
	
	@XmlElement(name = "parent")
	public void setParentApp(String parent) {
		this.parent = parent;
	}
	
	
}
