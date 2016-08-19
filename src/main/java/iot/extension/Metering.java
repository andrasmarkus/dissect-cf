package iot.extension;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import hu.mta.sztaki.lpds.cloud.simulator.io.*;
import hu.mta.sztaki.lpds.cloud.simulator.*;

public class Metering extends DeferredEvent {
	private int i;
	private String sName;
	private int filesize;

	public Metering(String sName, int i, int filesize) {
		super(1);
		this.i = i;
		this.filesize = filesize;
		this.sName = sName;
	}

	@Override
	protected void eventAction() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yy:MM:dd:HH:mm:ss:SS");
		StorageObject so = new StorageObject(this.sName + " " + this.filesize + " " + this.i + " "
				+ Timed.getFireCount() + " " + sdf.format(cal.getTime()), this.filesize, false);
		for (Station s : Station.stations) {
			if (s.getName().equals(this.sName)) {
				s.getRepo().registerObject(so);
			}
		}
	}
}
