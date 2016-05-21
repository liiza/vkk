package fi.solita.adele;

import fi.solita.adele.status.PlaceStatus;
import fi.solita.adele.status.StatusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@CrossOrigin
@RestController
public class WebController {

    @Resource
    private DataService dataService;

    @Resource
    private StatusService statusService;

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    String hello() {
        return "ok";
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    List<Event> getAllData() {
        return dataService.all();
    }

    @RequestMapping(value = "/v1/status/current", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    List<PlaceStatus> getCurrentStatusForAllPlaces() {
        return statusService.getCurrentStatusForAllPlaces();
    }

    @RequestMapping(value = "/v1/add/event", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity getCurrentStatusForAllPlaces(@RequestBody Event event) {
        if (dataService.addEvent(event) > 0) {
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
