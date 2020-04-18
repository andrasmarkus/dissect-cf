package hu.u_szeged.inf.fog.simulator.task_schedule;

import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.iot.DataCapsule;

public interface TaskScheduler {

    void assign(DataCapsule dc);
    void process();
    void moveDataCapsule(long size, Application source, Application destination, int direction);
    void afterProcess(DataCapsule dc);
}
