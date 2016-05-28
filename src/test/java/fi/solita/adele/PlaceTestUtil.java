package fi.solita.adele;

import fi.solita.adele.place.CreatePlaceCommand;
import fi.solita.adele.place.Place;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PlaceTestUtil {
    private static final RestTemplate restTemplate = new RestTemplate();
    public static final double LOCATION_COMPARISON_DELTA = 0.00001;

    private final int testServerPort;

    public PlaceTestUtil(int testServerPort) {
        this.testServerPort = testServerPort;
    }

    private String url(String suffix) {
        return "http://localhost:" + testServerPort + suffix;
    }

    public List<Place> getAllPlaces() {
        return Arrays.asList(restTemplate.getForObject(url("/v1/place"), Place[].class));
    }

    public int addPlace() {
        CreatePlaceCommand place = new CreatePlaceCommand();
        place.setName("Paikka 2");
        place.setLatitude(875.99856);
        place.setLongitude(984.98449);
        ResponseEntity<Integer> result = restTemplate.postForEntity(url("/v1/place"), place, Integer.class);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        return result.getBody();
    }

    public int addPlace(CreatePlaceCommand place) {
        ResponseEntity<Integer> result = restTemplate.postForEntity(url("/v1/place"), place, Integer.class);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        return result.getBody();
    }

    public Place getPlace(int id) {
        return restTemplate.getForEntity(url("/v1/place/" + id), Place.class).getBody();
    }
}
