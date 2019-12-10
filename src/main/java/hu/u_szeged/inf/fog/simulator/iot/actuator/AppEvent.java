package hu.u_szeged.inf.fog.simulator.iot.actuator;

public class AppEvent {

    //Just a dummy event type, real ones should be implemented later
    public static final class BasicActuatorEvent implements ActuatorEvent {

        @Override
        public ActuatorEventType getType() {
            return ActuatorEventType.BASIC_EVENT_TYPE;
        }
    }
}
