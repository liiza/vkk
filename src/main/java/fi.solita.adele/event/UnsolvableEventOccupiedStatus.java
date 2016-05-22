package fi.solita.adele.event;

public class UnsolvableEventOccupiedStatus extends RuntimeException {

    public UnsolvableEventOccupiedStatus(EventType eventType, double value) {
        super(String.format("Can not solve occupied status to event type %s for value %", eventType, value));
    }
}
