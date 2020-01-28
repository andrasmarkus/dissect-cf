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

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.iot.Device;

/**
 * This class represents the Azure IoT provider which calculates its cost based a monthly price,
 * but it has a restriction for message sizes and the total messages allowed per day.
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
public class AzureProvider extends Provider{

	
	/**
	 * Helper variable to calculate the daily message limit.
	 */
	private long usedMessage;

	/**
	 * ToString method, useful for debugging.
	 */
	@Override
	public String toString() {
		return "[AZURE=" + this.cost + "]";
	}

	/**
	 * Constructor which helps create provider like Azure without XML files.
	 * @param azureFreq The frequency of the provider, it should be a day in ms.
	 * @param pricePerMonth The monthly price.
	 * @param messagesPerDay The message limit per day.
	 * @param messagesizePerKB The message size limit.
	 * @param app The application which is monitored by this provider.
	 */
	public AzureProvider(long azureFreq,double pricePerMonth,long messagesPerDay,long messagesizePerKB,Application app) {
		super(app);
		this.azureFreq=azureFreq;
		this.pricePerMonth=pricePerMonth;
		this.messagesPerDay=messagesPerDay;
		this.messagesizePerKB=messagesizePerKB;
		this.usedMessage=0;
		this.startProvider();
	}

	/**
	 * This constructor should be used only in case of XML files.
	 * @param app The application which is monitored by this provider.
	 */
	public AzureProvider(Application app) {
		super();
		this.usedMessage=0;
		this.app=app;
	}
	
	/**
	 * This method calculates the average file size if the stations generate files with different size.
	 * @return It returns with the average file size.
	 */
	public long avarageFileSize() {
		long tmp=0;
		for(Device s : this.app.deviceList) {
			tmp+=s.getFilesize();
		}
		if(this.app.deviceList.size()==0) {
			return 0;
		}
		return tmp/this.app.deviceList.size();
	}
	
	/**
	 * This method calculates the costs based on the frequency of the class.
	 */
	public void tick(long fires) {
		if(this.app.isSubscribed()==false) {
			unsubscribe();
		}
		
		if(this.messagesPerDay>0 && this.avarageFileSize()<=(this.messagesizePerKB*1024)){
			if(this.avarageFileSize()==0) {
				this.cost=-1;
			}else {
				long totalMassages=this.app.sumOfProcessedData / this.avarageFileSize();
				long msg = totalMassages - usedMessage;
				usedMessage= msg;
				
				if(msg<=this.messagesPerDay){
					long month = Timed.getFireCount()/this.getFrequency();
					if(month==0){
						month=1;
						this.cost=this.pricePerMonth*month;
					}else if(Timed.getFireCount()%(this.getFrequency())!=0){
						this.cost=this.pricePerMonth*(month+1);
					}else{
						this.cost=this.pricePerMonth*month;
					}
				}else{
					this.cost=-1;
				}
			}
			
		}
		if(this.shouldStop) {
			unsubscribe();
		}
	}

	/**
	 * This method starts the work of the provider with the subscription.
	 */
	@Override
	public void startProvider() {
		subscribe(this.azureFreq);
		shouldStop=false;
	}
}
