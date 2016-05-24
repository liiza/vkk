package fi.solita.adele.event;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@CrossOrigin
@RestController
public class EventController {

    @Resource
    private EventRepository eventRepository;

    @RequestMapping(value = "/v1/event", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    List<Event> getAllEvents() {
        return eventRepository.all();
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
        if(ex.getCause() != null) {
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
