//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.09.01 at 11:34:30 PM CEST 
//


package hu.uszeged.inf.iot.simulator.xmlgenerator;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the generated package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Appliances }
     * 
     */
    public Appliances createAppliances() {
        return new Appliances();
    }

    /**
     * Create an instance of {@link Appliances.Appliance }
     * 
     */
    public Appliances.Appliance createAppliancesAppliance() {
        return new Appliances.Appliance();
    }

    /**
     * Create an instance of {@link Appliances.Appliance.NeighbourAppliances }
     * 
     */
    public Appliances.Appliance.NeighbourAppliances createAppliancesApplianceNeighbourAppliances() {
        return new Appliances.Appliance.NeighbourAppliances();
    }

    /**
     * Create an instance of {@link Appliances.Appliance.Applications }
     * 
     */
    public Appliances.Appliance.Applications createAppliancesApplianceApplications() {
        return new Appliances.Appliance.Applications();
    }

    /**
     * Create an instance of {@link Appliances.Appliance.NeighbourAppliances.Device }
     * 
     */
    public Appliances.Appliance.NeighbourAppliances.Device createAppliancesApplianceNeighbourAppliancesDevice() {
        return new Appliances.Appliance.NeighbourAppliances.Device();
    }

    /**
     * Create an instance of {@link Appliances.Appliance.Applications.Application }
     * 
     */
    public Appliances.Appliance.Applications.Application createAppliancesApplianceApplicationsApplication() {
        return new Appliances.Appliance.Applications.Application();
    }

}
