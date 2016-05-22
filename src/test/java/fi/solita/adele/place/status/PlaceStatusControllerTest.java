package fi.solita.adele.place.status;

import fi.solita.adele.App;
import fi.solita.adele.event.CreateEventCommand;
import fi.solita.adele.event.EventType;
import fi.solita.adele.place.CreatePlaceCommand;
import fi.solita.adele.place.Place;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebAppConfiguration
@IntegrationTest({"server.port:0"})
public class PlaceStatusControllerTest {
    @Value("${local.server.port}")
    int port;

    private RestTemplate restTemplate = new RestTemplate();
    private Random random = new Random();

    private String url(String suffix) {
        return "http://localhost:" + port + suffix;
    }

    private int addEvent(int placeId, LocalDateTime time, boolean occupied) {
        CreateEventCommand event = new CreateEventCommand();
        event.setDevice_id(1);
        event.setPlace_id(Optional.of(placeId));
        event.setTime(Optional.of(time));
        event.setType(EventType.occupied);
        event.setValue(occupied ? 1.0 : 0.0);

        ResponseEntity<Integer> result = restTemplate.postForEntity(url("/v1/event"), event, Integer.class);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        return result.getBody().intValue();
    }

    private int addPlace() {
        CreatePlaceCommand place = new CreatePlaceCommand();
        place.setName("Paikka " + random.nextInt());
        place.setLatitude(random.nextDouble());
        place.setLongitude(random.nextDouble());

        ResponseEntity<Integer> result = restTemplate.postForEntity(url("/v1/place"), place, Integer.class);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        return result.getBody();
    }

    private Place getPlace(int id) {
        return restTemplate.getForEntity(url("/v1/place/" + id), Place.class).getBody();
    }

    private List<PlaceStatus> getCurrentStatusForAllPlaces() {
        ResponseEntity<PlaceStatus[]> result = restTemplate.getForEntity(url("/v1/status/current"), PlaceStatus[].class);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        return Arrays.asList(result.getBody());
    }

    @Test
    public void should_list_current_state_for_all_places() {
        final int placeId1 = addPlace();
        final int placeId2 = addPlace();
        final int placeId3 = addPlace();

        final Place place1 = getPlace(placeId1);
        final Place place2 = getPlace(placeId2);
        final Place place3 = getPlace(placeId3);

        addEvent(placeId1, LocalDateTime.now().minusDays(3), false);
        addEvent(placeId1, LocalDateTime.now().minusDays(2), false);
        addEvent(placeId1, LocalDateTime.now().minusDays(1), true);

        addEvent(placeId2, LocalDateTime.now().minusDays(3), true);
        addEvent(placeId2, LocalDateTime.now().minusDays(2), true);
        addEvent(placeId2, LocalDateTime.now().minusDays(1), false);

        List<PlaceStatus> result = getCurrentStatusForAllPlaces();
        assertNotNull(result);

        Optional<PlaceStatus> place1Status = result.stream().filter(status -> status.getPlace_id() == placeId1).findFirst();
        assertTrue(place1Status.isPresent());
        assertTrue(place1Status.get().isOccupied());
        assertEquals(place1.getLongitude(), place1Status.get().getLongitude(), 0.001);
        assertEquals(place1.getLatitude(), place1Status.get().getLatitude(), 0.001);

        Optional<PlaceStatus> place2Status = result.stream().filter(status -> status.getPlace_id() == placeId2).findFirst();
        assertTrue(place2Status.isPresent());
        assertFalse(place2Status.get().isOccupied());
        assertEquals(place2.getLongitude(), place2Status.get().getLongitude(), 0.001);
        assertEquals(place2.getLatitude(), place2Status.get().getLatitude(), 0.001);

        assertFalse(result.stream().anyMatch(status -> status.getPlace_id() == placeId3));
    }
}