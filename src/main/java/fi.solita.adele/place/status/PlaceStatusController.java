package fi.solita.adele.place.status;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@CrossOrigin
@RestController
public class PlaceStatusController {

    @Resource
    private PlaceStatusRepository placeStatusRepository;

    @RequestMapping(value = "/v1/status/current", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    List<PlaceStatus> getCurrentStatusForAllPlaces() {
        return placeStatusRepository.getCurrentStatusForAllPlaces();
    }
}
