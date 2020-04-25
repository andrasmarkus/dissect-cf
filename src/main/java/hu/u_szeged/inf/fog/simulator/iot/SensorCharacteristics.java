package hu.u_szeged.inf.fog.simulator.iot;

public class SensorCharacteristics {

    private int sensorNum;
    //mean time to failure (eg in hours, or seconds etc.. must be similar to to frequency)
    private long mttf;
    private int minFreq, maxFreq;
    //This gives the ratio of data that has to be processed in the fog (in average)
    private double fogDataRatio;
    private double actuatorRatio;

    public SensorCharacteristics(int sensorNum, long mttf, int minFreq, int maxFreq, double fogDataRatio, double actuatorRatio) {
        this.sensorNum = sensorNum;
        this.mttf = mttf;
        this.minFreq = minFreq;
        this.maxFreq = maxFreq;
        if(actuatorRatio > 1) {
            this.actuatorRatio = 1;
        } else if(actuatorRatio < 0) {
            this.actuatorRatio = 0;
        } else {
            this.actuatorRatio = actuatorRatio;
        }
        if(fogDataRatio < 0) {
            this.fogDataRatio = 0;
        }
        else if(fogDataRatio > 1) {
            this.fogDataRatio = 1;
        }
        else {
            this.fogDataRatio = fogDataRatio;
        }
    }


    public int getSensorNum() {
        return sensorNum;
    }

    public void setSensorNum(int sensorNum) {
        this.sensorNum = sensorNum;
    }

    public long getMttf() {
        return mttf;
    }

    public void setMttf(long mttf) {
        this.mttf = mttf;
    }

    public int getMinFreq() {
        return minFreq;
    }

    public void setMinFreq(int minFreq) {
        this.minFreq = minFreq;
    }

    public int getMaxFreq() {
        return maxFreq;
    }

    public void setMaxFreq(int maxFreq) {
        this.maxFreq = maxFreq;
    }

    public double getFogDataRatio() {
        return fogDataRatio;
    }

    public void setFogDataRatio(double fogDataRatio) {
        this.fogDataRatio = fogDataRatio;
    }

    public double getActuatorRatio() {
        return actuatorRatio;
    }

    public void setActuatorRatio(double actuatorRatio) {
        this.actuatorRatio = actuatorRatio;
    }
}
