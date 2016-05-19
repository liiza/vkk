package fi.solita.adele;

import fi.solita.adele.status.PlaceStatus;
import fi.solita.adele.status.StatusService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    List<Device> getAllData() {
        return dataService.all();
    }

    @RequestMapping(value = "/v1/status/current", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    List<PlaceStatus> getCurrentStatusForAllPlaces() {
        return statusService.getCurrentStatusForAllPlaces();
    }
}
