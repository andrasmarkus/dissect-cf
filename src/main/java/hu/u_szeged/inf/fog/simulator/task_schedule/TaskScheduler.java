package hu.u_szeged.inf.fog.simulator.task_schedule;

import hu.u_szeged.inf.fog.simulator.iot.DataCapsule;

public interface TaskScheduler {

    void assign(DataCapsule dc);
}
