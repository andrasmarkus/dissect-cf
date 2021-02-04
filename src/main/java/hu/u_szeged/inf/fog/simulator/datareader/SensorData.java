package hu.u_szeged.inf.fog.simulator.datareader;

import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;

import java.util.Date;

public class SensorData extends StorageObject {
    private final long date;

    public SensorData(long date, String iD, long size)  {
        super(iD, size, false);
        this.date = date;
    }

    @Override
    public String toString() {
        Date d = new Date(date);
        return "SensorData{" +
                "date='" + d + '\'' +
                ", id='" + id + '\'' +
                ", size=" + size +
                '}';
    }

    public long getDate() {
        return date;
    }

    public String getiD() {
        return id;
    }

    public long getSize() {
        return size;
    }
}
