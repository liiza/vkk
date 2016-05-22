package fi.solita.adele.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.solita.adele.App;
import fi.solita.adele.place.CreatePlaceCommand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.*;

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
        CreatePlaceCommand place = new CreatePlaceCommand();
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

    private ResponseEntity<Integer> addEvent(Integer deviceId, Integer placeId, LocalDateTime time, String type, Double value) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String json = getJson(deviceId, placeId, time, type, value);
        HttpEntity<String> entity = new HttpEntity<String>(json, headers);

        return restTemplate.postForEntity(url("/v1/event"), entity, Integer.class);
    }

    private Event getEvent(int id) {
        return restTemplate.getForObject(url("/v1/event/" + id), Event.class);
    }

    private void assertEventsEqual(CreateEventCommand command, Event event) {
        assertEquals(command.getDevice_id().intValue(), event.getDevice_id());
        assertEquals(command.getPlace_id().get().intValue(), event.getPlace_id());
        assertEquals(command.getTime().get(), event.getTime());
        assertEquals(command.getType(), event.getType());
        assertEquals(command.getValue(), event.getValue(), valueDelta);
    }

    private String getJson(Integer deviceId, Integer placeId, LocalDateTime time, String type, Double value) {
        Map<String, String> json = new HashMap<>();
        if (deviceId != null) {
            json.put("device_id", deviceId.toString());
        }
        if (placeId != null) {
            json.put("place_id", placeId.toString());
        }
        if (time != null) {
            json.put("time", time.toString());
        }
        if (type != null) {
            json.put("type", type);
        }
        if (value != null) {
            json.put("value", value.toString());
        }
        try {
            return new ObjectMapper().writeValueAsString(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
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

        assertEquals(command3.getDevice_id().intValue(), event.getDevice_id());
        assertEquals(placeId2, event.getPlace_id());
        assertEquals(command3.getTime().get(), event.getTime());
        assertEquals(command3.getType(), event.getType());
        assertEquals(command3.getValue(), event.getValue(), valueDelta);
    }

    @Test
    public void should_not_add_new_event_if_there_is_no_previous_event_for_device() {
        addPlace();

        CreateEventCommand command1 = new CreateEventCommand();
        command1.setDevice_id(300);
        command1.setPlace_id(Optional.empty());
        command1.setTime(Optional.of(LocalDateTime.now().minusDays(1)));
        command1.setType(EventType.occupied);
        command1.setValue(1.0);

        try {
            addEvent(command1);
            fail();
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
            assertEquals("No previous event for device 300", ex.getResponseBodyAsString());
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

        assertEquals(command.getDevice_id().intValue(), event.getDevice_id());
        assertEquals(command.getPlace_id().get().intValue(), event.getPlace_id());
        assertTrue(event.getTime().toLocalDate().equals(LocalDate.now()));
        assertEquals(command.getType(), event.getType());
        assertEquals(command.getValue(), event.getValue(), valueDelta);
    }

    @Test
    public void should_not_add_new_event_with_missing_device_id() {
        try {
            addEvent(null, addPlace(), LocalDateTime.now(), EventType.occupied.toString(), 1.0);
            fail();
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            assertEquals("Event Device Id is missing", ex.getResponseBodyAsString());
        }
    }

    @Test
    public void should_not_add_new_event_with_missing_place_id() {
        try {
            addEvent(600, null, LocalDateTime.now(), EventType.occupied.toString(), 1.0);
            fail();
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
            assertEquals("No previous event for device 600", ex.getResponseBodyAsString());
        }
    }

    @Test
    public void should_not_add_new_event_with_missing_type() {
        try {
            addEvent(600, addPlace(), LocalDateTime.now(), null, 1.0);
            fail();
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            assertEquals("Event type is missing", ex.getResponseBodyAsString());
        }
    }

    @Test
    public void should_not_add_new_event_with_unknown_type() {
        try {
            addEvent(600, addPlace(), LocalDateTime.now(), "abc", 1.0);
            fail();
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            assertTrue(ex.getResponseBodyAsString().startsWith("Can not construct instance of fi.solita.adele.event.EventType " +
                    "from String value 'abc': value not one of declared Enum instance names"));
        }
    }

    @Test
    public void should_not_add_new_event_with_missing_value() {
        try {
            addEvent(600, addPlace(), LocalDateTime.now(), EventType.occupied.toString(), null);
            fail();
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            assertEquals("Event value is missing", ex.getResponseBodyAsString());
        }
    }
}