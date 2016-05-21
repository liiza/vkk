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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebAppConfiguration
@IntegrationTest({"server.port:0"})
public class EventControllerTest {
    @Value("${local.server.port}")
    int port;

    final RestTemplate restTemplate = new RestTemplate();
    final double valueDelta = 0.001;

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

    private int addEvent(CreateEventCommand event) {
        ResponseEntity<Integer> result = restTemplate.postForEntity(url("/v1/event"), event, Integer.class);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        return result.getBody().intValue();
    }

    private Event getEvent(int id) {
        return restTemplate.getForObject(url("/v1/event/" + id), Event.class);
    }

    private void assertEventsEqual(CreateEventCommand command, Event event) {
        assertEquals(command.getDevice_id(), event.getDevice_id());
        assertEquals(command.getPlace_id().get().intValue(), event.getPlace_id());
        assertEquals(command.getTime().get(), event.getTime());
        assertEquals(command.getType(), event.getType());
        assertEquals(command.getValue(), event.getValue(), valueDelta);
    }

    @Test
    public void should_list_all_events() {
        int placeId = addPlace();

        CreateEventCommand event = new CreateEventCommand();
        event.setDevice_id(100);
        event.setPlace_id(Optional.of(placeId));
        event.setTime(Optional.of(LocalDateTime.now()));
        event.setType(EventType.occupied);
        event.setValue(1.0);

        int eventId = addEvent(event);
        Optional<Event> savedEvent = getAllEvents().stream().filter(e -> e.getID() == eventId).findFirst();

        assertTrue(savedEvent.isPresent());
        assertEventsEqual(event, savedEvent.get());
    }

    @Test
    public void should_add_new_event() {
        int placeId = addPlace();

        CreateEventCommand event = new CreateEventCommand();
        event.setDevice_id(100);
        event.setPlace_id(Optional.of(placeId));
        event.setTime(Optional.of(LocalDateTime.now()));
        event.setType(EventType.occupied);
        event.setValue(1.0);

        int eventId = addEvent(event);
        Event savedEvent = getEvent(eventId);
        assertEventsEqual(event, savedEvent);
    }

    @Test
    public void should_add_new_event_to_previous_place() {
        int placeId1 = addPlace();
        int placeId2 = addPlace();

        CreateEventCommand command1 = new CreateEventCommand();
        command1.setDevice_id(200);
        command1.setPlace_id(Optional.of(placeId1));
        command1.setTime(Optional.of(LocalDateTime.now().minusDays(2)));
        command1.setType(EventType.occupied);
        command1.setValue(1.0);
        addEvent(command1);

        CreateEventCommand command2 = new CreateEventCommand();
        command2.setDevice_id(200);
        command2.setPlace_id(Optional.of(placeId2));
        command2.setTime(Optional.of(LocalDateTime.now().minusDays(1)));
        command2.setType(EventType.occupied);
        command2.setValue(0.0);
        addEvent(command2);

        CreateEventCommand command3 = new CreateEventCommand();
        command3.setDevice_id(200);
        command3.setPlace_id(Optional.empty());
        command3.setTime(Optional.of(LocalDateTime.now().plusDays(1)));
        command3.setType(EventType.occupied);
        command3.setValue(0.0);
        int eventId3 = addEvent(command3);

        Event event = getEvent(eventId3);

        assertEquals(command3.getDevice_id(), event.getDevice_id());
        assertEquals(placeId2, event.getPlace_id());
        assertEquals(command3.getTime().get(), event.getTime());
        assertEquals(command3.getType(), event.getType());
        assertEquals(command3.getValue(), event.getValue(), valueDelta);
    }

    @Test
    public void should_not_add_new_event_if_there_is_no_previous_event_for_device() {
        Integer placeId = addPlace();

        CreateEventCommand command1 = new CreateEventCommand();
        command1.setDevice_id(300);
        command1.setPlace_id(Optional.empty());
        command1.setTime(Optional.of(LocalDateTime.now().minusDays(1)));
        command1.setType(EventType.occupied);
        command1.setValue(1.0);

        try {
            addEvent(command1);
            fail();
        }
        catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        }
    }

    @Test
    public void should_add_new_event_without_time_to_current_time() {
        int placeId = addPlace();

        CreateEventCommand command = new CreateEventCommand();
        command.setDevice_id(100);
        command.setPlace_id(Optional.of(placeId));
        command.setTime(Optional.empty());
        command.setType(EventType.occupied);
        command.setValue(1.0);

        int eventId = addEvent(command);
        Event event = getEvent(eventId);

        assertEquals(command.getDevice_id(), event.getDevice_id());
        assertEquals(command.getPlace_id().get().intValue(), event.getPlace_id());
        assertTrue(event.getTime().toLocalDate().equals(LocalDate.now()));
        assertEquals(command.getType(), event.getType());
        assertEquals(command.getValue(), event.getValue(), valueDelta);
    }
}