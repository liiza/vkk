package fi.solita.adele.event;

public class EventCreationFailedException extends RuntimeException {

    public EventCreationFailedException(String message) {
        super(message);
    }
}
