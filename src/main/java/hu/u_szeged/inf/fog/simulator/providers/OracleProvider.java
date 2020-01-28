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

package hu.u_szeged.inf.fog.simulator.providers;

import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.iot.Device;
import hu.u_szeged.inf.fog.simulator.iot.Station;

/**
 * This class represents the Oracle IoT provider which has a restriction on how many messages
 *  can a device deliver per month. In case, the number of messages sent by a device is more than the device’s category permits, an additional
 *  price will be charged according to a predefined price per thousand of messages.
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
public class OracleProvider extends Provider {

	
	/**
	 * Helper variable to calculate the daily message limit.
	 */
	@Override
	public String toString() {
		return "[ORACLE=" + cost+"]";
	}

	/**
	 * This constructor should be used only in case of XML files.
	 * @param app The application which is monitored by this provider.
	 */
	public OracleProvider(Application app) {
		super();
		this.app=app;
	}
	
	/**
	 * Constructor which helps create provider like Oracle without XML files.
	 * @param oracleFreq The frequency of the provider, it should be a month in ms.
	 * @param devicepricePerMonth The device price per month.
	 * @param messagesPerMonthPerDevice The allowed number of messages for one device.
	 * @param amDevicepricePerMonth Additional device price.
	 * @param amMessagesPerMonthPerDevice Additional number of messages for one device.
	 * @param app The application which is monitored by this provider.
	 */
	public OracleProvider(long oracleFreq,double devicepricePerMonth,long messagesPerMonthPerDevice,
	double amDevicepricePerMonth,long amMessagesPerMonthPerDevice,Application app) {
		super(app);
		this.devicepricePerMonth=devicepricePerMonth;
		this.messagesPerMonthPerDevice=messagesPerMonthPerDevice;
		this.amDevicepricePerMonth=amDevicepricePerMonth;
		this.amMessagesPerMonthPerDevice=amMessagesPerMonthPerDevice;
		this.oracleFreq=oracleFreq;
		this.startProvider();
	}
	
	/**
	 * This method calculates the costs based on the frequency of the class.
	 */
	public void tick(long fires) {

		if(this.amMessagesPerMonthPerDevice>0){
				for(Device s : this.app.deviceList){
					long month = s.getStopTime()/(this.getFrequency());
					if(month==0){
						month=1;
						this.cost=this.cost+this.devicepricePerMonth*((Station) s).getSensorNum()*month;
					}else if(s.getStopTime()%(this.getFrequency())!=0){
						this.cost=this.cost+this.devicepricePerMonth*((Station) s).getSensorNum()*(month+1);
					}else{
						this.cost=this.cost+this.devicepricePerMonth*((Station) s).getSensorNum()*month;
					}
					// additional cost
					long device = s.getMessageCount()/((Station) s).getSensorNum();// 1 device hany uzenetet generalt
					s.setMessageCount(0); 
					if(device>this.messagesPerMonthPerDevice){
						device-=this.messagesPerMonthPerDevice;
						long whole=device/this.amMessagesPerMonthPerDevice;
						if(whole==0){
							this.cost+=this.amDevicepricePerMonth;
						}else if((device%this.amMessagesPerMonthPerDevice)!=0){
							this.cost+=this.amDevicepricePerMonth*(whole+1);
						}else{
							this.cost+=this.amDevicepricePerMonth*(whole);
						}
					} 
				}
			if(this.shouldStop) {
				unsubscribe();
			}
			
		}
	}

	/**
	 * This method starts the work of the provider with the subscription.
	 */
	@Override
	public void startProvider() {
		subscribe(oracleFreq);
		this.shouldStop=false;
	}
}
