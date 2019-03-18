package hu.uszeged.inf.iot.simulator.fog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.util.CloudLoader;

public class Cloud {

	public IaaSService iaas;
	public static HashMap<String, Cloud> clouds = new HashMap<String, Cloud>();
	public ArrayList<Application> applications;
	public ArrayList<Fog> fogs;
	public String name;
	
	public static Cloud addApplication(Application app,String cloud) {
		Cloud c = clouds.get(cloud);
		c.applications.add(app);
		return c;
	}
	
	public Cloud(String cloudfile,String name)throws IOException, SAXException, ParserConfigurationException {
		if(cloudfile!=null) {
			this.iaas = CloudLoader.loadNodes(cloudfile);
			Cloud.clouds.put(name,this);
			this.name=name;
		}
		applications = new ArrayList<Application>();
		fogs = new ArrayList<Fog>();
	}
	
}
