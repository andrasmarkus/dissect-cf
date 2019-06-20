/*
 *  ========================================================================
 *  DIScrete event baSed Energy Consumption simulaTor 
 *    					             for Clouds and Federations (DISSECT-CF)
 *  ========================================================================
 *  
 *  This file is part of DISSECT-CF.
 *  
 *  DISSECT-CF is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or (at
 *  your option) any later version.
 *  
 *  DISSECT-CF is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *  General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with DISSECT-CF.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  (C) Copyright 2019, Andras Markus (markusa@inf.u-szeged.hu)
 */

package hu.uszeged.inf.iot.simulator.providers;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.uszeged.inf.iot.simulator.providers.BluemixProvider.Bluemix;
import hu.uszeged.inf.iot.simulator.system.Application;
import hu.uszeged.inf.xml.model.ProvidersModel;

/**
 * This class lets to create IoT providers which can calculate the costs based on many possible parameters.
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
public abstract class Provider extends Timed{
		
	/**
	 * The path of the provider file.
	 */
	public static String PROVIDERFILE;
	
	// These variables are detailed in the corresponding provider.
	public ArrayList<Bluemix> bmList;
	Application app;
	public boolean shouldStop;
	
	public long blockSize;
	public long messageCount;
	public double blockPrice;

	public double devicepricePerMonth;
	public long messagesPerMonthPerDevice;
	public double amDevicepricePerMonth;
	public long amMessagesPerMonthPerDevice;
	public long oracleFreq;
	
	public long azureFreq;
	public double pricePerMonth;
	public long messagesPerDay;
	public long messagesizePerKB;
		
	
	/**
	 * This method generates all 4 providers for the all applications.
	 * @param providerfile The path of the provider file.
	 */
	public static void loadProvider(String providerfile){
		Provider.PROVIDERFILE=providerfile;
		for(Application app: Application.getApplications()) {
			app.getProviders().add(new BluemixProvider(app));
			app.getProviders().add(new AmazonProvider(app));
			app.getProviders().add(new OracleProvider(app));
			app.getProviders().add(new AzureProvider(app));
		}
	}
	
	/**
	 * This constructor connects the application with the provider.
	 * @param app The application which is monitored by this provider.
	 */
	Provider(Application app){
		this.app=app;
		this.app.getProviders().add(this);
	}
	
	/**
	 * Default constructor supports the provider generation from XML file.
	 */
	Provider(){
		bmList = new ArrayList<Bluemix>();
		try {
			ProvidersModel.loadProviderXML(Provider.PROVIDERFILE,this);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.startProvider();
	}

	/**
	 * The method needs to be override to start the actual provider.
	 */
	public abstract void startProvider();
	
	/**
	 * It's for the cost calculation. Needs to override.
	 */
	@Override
	public void tick(long fires) {
	}

}
