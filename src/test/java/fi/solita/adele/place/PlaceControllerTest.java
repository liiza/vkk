package fi.solita.adele.place;

import fi.solita.adele.App;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebAppConfiguration
@IntegrationTest({"server.port:0"})
public class PlaceControllerTest {
    @Value("${local.server.port}")
    int port;

    RestTemplate restTemplate = new RestTemplate();

    private String url(String suffix) {
        return "http://localhost:" + port + suffix;
    }

    private List<Place> getAllPlaces() {
        return Arrays.asList(restTemplate.getForObject(url("/v1/place"), Place[].class));
    }

    private int addPlace(CreatePlaceCommand place) {
        ResponseEntity<Integer> result = restTemplate.postForEntity(url("/v1/place"), place, Integer.class);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        return result.getBody();
    }

    private Place getPlace(int id) {
        return restTemplate.getForEntity(url("/v1/place/" + id), Place.class).getBody();
    }

    @Test
    public void should_list_all_places() {
        CreatePlaceCommand place = new CreatePlaceCommand();
        place.setName("Paikka 2");
        place.setLatitude(875.99856);
        place.setLongitude(984.98449);

        int id = addPlace(place);
        Optional<Place> savedPlaceOptional = getAllPlaces().stream().filter(p -> p.getId() == id).findFirst();

        assertTrue(savedPlaceOptional.isPresent());
        assertEquals(place.getName(), savedPlaceOptional.get().getName());
        assertEquals(place.getLongitude(), savedPlaceOptional.get().getLongitude(), 0.001);
        assertEquals(place.getLatitude(), savedPlaceOptional.get().getLatitude(), 0.001);
    }

    @Test
    public void should_add_new_place() {
        CreatePlaceCommand place = new CreatePlaceCommand();
        place.setName("Paikka 1");
        place.setLatitude(123.456);
        place.setLongitude(456.789);

        int id = addPlace(place);
        Place savedPlace = getPlace(id);

        assertEquals(place.getName(), savedPlace.getName());
        assertEquals(place.getLongitude(), savedPlace.getLongitude(), 0.001);
        assertEquals(place.getLatitude(), savedPlace.getLatitude(), 0.001);

    }
}