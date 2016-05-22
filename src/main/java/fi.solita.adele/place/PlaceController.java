package fi.solita.adele.place;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@CrossOrigin
@RestController
public class PlaceController {

    @Resource
    private PlaceRepository placeRepository;

    @RequestMapping(value = "/v1/place", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    List<Place> getAllPlaces() {
        return placeRepository.all();
    }

    @RequestMapping(value = "/v1/place/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    Place getPlaceById(@PathVariable("id") int id) {
        return placeRepository.getPlace(id);
    }

    @RequestMapping(value = "/v1/place", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    int addPlace(@RequestBody CreatePlaceCommand place) {
        return placeRepository.addPlace(place);
    }
}
