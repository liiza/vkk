package fi.solita.adele.event;

public class EventValidator {

    public static void validateCreate(CreateEventCommand command) {
        if(command.getDevice_id() == null) {
            throw new EventCreationFailedException("Event Device Id is missing");
        }
        if(command.getType() == null) {
            throw new EventCreationFailedException("Event type is missing");
        }
        if(command.getValue() == null) {
            throw new EventCreationFailedException("Event value is missing");
        }
    }
}
