package fi.solita.adele.event;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
public class EventController {

    @Resource
    private EventRepository eventRepository;

    @RequestMapping(value = "/v1/event", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    List<Event> getAllEvents(
            @RequestParam(value = "starting", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> starting,
            @RequestParam(value = "ending", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> ending,
            @RequestParam(value = "device_id", required = false) Integer[] device_id,
            @RequestParam(value = "place_id", required = false) Integer[] place_id,
            @RequestParam(value = "type", required = false) Optional<EventType> type) {
        return eventRepository.all(starting, ending, Optional.ofNullable(device_id), Optional.ofNullable(place_id), type);
    }

    @RequestMapping(value = "/v1/event/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    Event getEventById(@PathVariable("id") int id) {
        return eventRepository.getEvent(id);
    }

    @RequestMapping(value = "/v1/event", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    int addEvent(@RequestBody CreateEventCommand event) {
        return eventRepository.addEvent(event);
    }

    @ExceptionHandler(NoPreviousEventForDeviceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    String handleNoPreviousEventForDeviceException(NoPreviousEventForDeviceException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(EventCreationFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handleEventCreationFailedException(EventCreationFailedException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handleJacksonInvalidFormatException(HttpMessageNotReadableException ex) {
        if (ex.getCause() != null) {
            return ex.getCause().getMessage();
        }
        return ex.getMessage();
    }

    @RequestMapping(value = "/v1/query/usagestats", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    UsageStats usageStats(@RequestBody GetUsageStatsCommand query) {
        return eventRepository.getUsageStats(query);
    }
}
