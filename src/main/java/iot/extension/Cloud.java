package iot.extension;

import hu.mta.sztaki.lpds.cloud.simulator.util.*;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.*;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;

public class Cloud {
	public static IaaSService iaas;
	private static VirtualAppliance va;
	public static VirtualAppliance v = new VirtualAppliance("BaseVA", 100, 0, false, 1000000); // default static VA to reach from everywhere
	private static AlterableResourceConstraints arc = new AlterableResourceConstraints(8, 0.001, 4294967296l); // 8 magos,4gb memoria
	
	public static VirtualAppliance getVa() {
		return va;
	}
	public static AlterableResourceConstraints getArc() {
		return arc;
	}

	public Cloud(VirtualAppliance va,String cloudfile) throws IOException, SAXException, ParserConfigurationException {
		Cloud.va = va;
		iaas = CloudLoader.loadNodes(cloudfile);
		iaas.repositories.get(0).registerObject(Cloud.getVa());
	}
}
