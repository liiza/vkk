package fi.solita.adele.event;

import fi.solita.adele.App;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebAppConfiguration
@IntegrationTest({"server.port:0"})
public class EventControllerTest {
    @Value("${local.server.port}")
    int port;

    RestTemplate restTemplate = new RestTemplate();

    private String url(String suffix) {
        return "http://localhost:" + port + suffix;
    }

    private List<Event> getAllEvents() {
        return Arrays.asList(restTemplate.getForObject(url("/v1/event"), Event[].class));
    }

    private int addPlace() {
        Place place = new Place();
        place.setName("Paikka 2");
        place.setLatitude(875.99856);
        place.setLongitude(984.98449);
        ResponseEntity<Integer> result = restTemplate.postForEntity(url("/v1/place"), place, Integer.class);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        return result.getBody();
    }

    private int addEvent(Event event) {
        ResponseEntity<Integer> result = restTemplate.postForEntity(url("/v1/event"), event, Integer.class);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        return result.getBody().intValue();
    }

    private Event getEvent(int id) {
        return restTemplate.getForObject(url("/v1/event/" + id), Event.class);
    }

    @Test
    public void should_list_all_events() {
        int placeId = addPlace();

        Event event = new Event();
        event.setDevice_id(100);
        event.setPlace_id(placeId);
        event.setTime(LocalDateTime.now());
        event.setType("liiketunnistin");
        event.setValue(1.0);

        int eventId = addEvent(event);
        Optional<Event> savedEvent = getAllEvents().stream().filter(e -> e.getID() == eventId).findFirst();

        assertTrue(savedEvent.isPresent());
        assertEquals(event.getDevice_id(), savedEvent.get().getDevice_id());
        assertEquals(event.getPlace_id(), savedEvent.get().getPlace_id());
        assertEquals(event.getTime(), savedEvent.get().getTime());
        assertEquals(event.getType(), savedEvent.get().getType());
        assertEquals(event.getValue(), savedEvent.get().getValue(), 0.001);
    }

    @Test
    public void should_add_new_event() {
        int placeId = addPlace();

        Event event = new Event();
        event.setDevice_id(100);
        event.setPlace_id(placeId);
        event.setTime(LocalDateTime.now());
        event.setType("liiketunnistin");
        event.setValue(1.0);

        int eventId = addEvent(event);
        Event savedEvent = getEvent(eventId);

        assertEquals(event.getDevice_id(), savedEvent.getDevice_id());
        assertEquals(event.getPlace_id(), savedEvent.getPlace_id());
        assertEquals(event.getTime(), savedEvent.getTime());
        assertEquals(event.getType(), savedEvent.getType());
        assertEquals(event.getValue(), savedEvent.getValue(), 0.001);
    }
}