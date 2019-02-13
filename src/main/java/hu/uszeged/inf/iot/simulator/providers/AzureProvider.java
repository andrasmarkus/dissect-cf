package hu.uszeged.inf.iot.simulator.providers;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.uszeged.inf.iot.simulator.entities.Application;
import hu.uszeged.inf.iot.simulator.entities.Device;
import hu.uszeged.inf.iot.simulator.entities.Station;
import hu.uszeged.inf.xml.model.ProvidersModel;

public class AzureProvider extends Provider{
	double AZURE;
	private long usedMessage;
	@Override
	public String toString() {
		return "[AZURE=" + AZURE + " "+this.getFrequency()+"]";
	}


	public AzureProvider(Application app,String providerfile) {
		this.usedMessage=0;
		this.app=app;
		try {
			ProvidersModel.loadProviderXML(providerfile,this);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		subscribe(this.getHighestStopTime(this.azureFreq));
	}
	
	public long avarageFileSize() {
		long tmp=0;
		for(Device s : this.app.stations) {
			tmp+=s.getFilesize();
		}
		return tmp/this.app.stations.size();
	}
	
	public void tick(long fires) {
		if(this.app.isSubscribed()==false) {
			unsubscribe();
		}
		
		if(this.messagesPerDay>0 && this.avarageFileSize()<=(this.messagesizePerKB*1024)){
			long totalMassages=this.app.sumOfProcessedData / this.avarageFileSize();
			long msg = totalMassages - usedMessage;
			usedMessage= msg;
			
			if(msg<=this.messagesPerDay){
				long month = Timed.getFireCount()/this.getFrequency();
				if(month==0){
					month=1;
					this.AZURE=this.pricePerMonth*month;
				}else if(Timed.getFireCount()%(this.getFrequency())!=0){
					this.AZURE=this.pricePerMonth*(month+1);
				}else{
					this.AZURE=this.pricePerMonth*month;
				}
			}else{
				this.AZURE=-1;
			}
}
	}
}
