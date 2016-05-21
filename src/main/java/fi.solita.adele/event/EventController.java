package fi.solita.adele.event;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @RequestMapping(value = "/v1/event", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity addEvent(@RequestBody Event event) {
        if (eventRepository.addEvent(event) > 0) {
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
