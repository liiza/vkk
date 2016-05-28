package fi.solita.adele;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.solita.adele.event.CreateEventCommand;
import fi.solita.adele.event.Event;
import fi.solita.adele.event.EventType;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class EventTestUtil {
    private static final RestTemplate restTemplate = new RestTemplate();

    public static final double EVENT_VALUE_COMPARISON_DELTA = 0.001;
    public static final double OCCUPIED = 1.0;
    public static final double FREE = 0.0;

    private final int testServerPort;

    public EventTestUtil(int testServerPort) {
        this.testServerPort = testServerPort;
    }

    private String url(String suffix) {
        return "http://localhost:" + testServerPort + suffix;
    }

    public List<Event> getAllEvents() {
        return getAllEvents(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

    public List<Event> getAllEvents(Optional<LocalDateTime> starting,
                                     Optional<LocalDateTime> ending,
                                     Optional<Integer[]> device_id,
                                     Optional<Integer[]> place_id,
                                     Optional<EventType> type) {

        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(url("/v1/event"));

        starting.ifPresent(v -> uri.queryParam("starting", v));
        ending.ifPresent(v -> uri.queryParam("ending", v));
        device_id.map(Arrays::asList).orElse(new ArrayList<>()).forEach(v -> uri.queryParam("device_id", v));
        place_id.map(Arrays::asList).orElse(new ArrayList<>()).forEach(v -> uri.queryParam("place_id", v));
        type.ifPresent(v -> uri.queryParam("type", v));

        return Arrays.asList(restTemplate.getForObject(uri.build().toUri(), Event[].class));
    }

    public int addEvent(CreateEventCommand event) {
        ResponseEntity<Integer> result = restTemplate.postForEntity(url("/v1/event"), event, Integer.class);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        return result.getBody();
    }

    public ResponseEntity<Integer> addEvent(Integer deviceId, Integer placeId, LocalDateTime time, Double value) {
        return addEvent(deviceId, placeId, time, EventType.occupied.toString(), value);
    }

    public ResponseEntity<Integer> addEvent(Integer deviceId, Integer placeId, LocalDateTime time, String type, Double value) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String json = getJson(deviceId, placeId, time, type, value);
        HttpEntity<String> entity = new HttpEntity<String>(json, headers);

        return restTemplate.postForEntity(url("/v1/event"), entity, Integer.class);
    }

    public Event getEvent(int id) {
        return restTemplate.getForObject(url("/v1/event/" + id), Event.class);
    }

    public static void assertEventsEqual(CreateEventCommand command, Event event) {
        assertEquals(command.getDevice_id().intValue(), event.getDevice_id());
        assertEquals(command.getPlace_id().get().intValue(), event.getPlace_id());
        assertEquals(command.getTime().get(), event.getTime());
        assertEquals(command.getType(), event.getType());
        assertEquals(command.getValue(), event.getValue(), EVENT_VALUE_COMPARISON_DELTA);
    }

    public static String getJson(Integer deviceId, Integer placeId, LocalDateTime time, String type, Double value) {
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

    public static CreateEventCommand eventCommand(int placeId) {
        CreateEventCommand event = new CreateEventCommand();
        event.setDevice_id(100);
        event.setPlace_id(Optional.of(placeId));
        event.setTime(Optional.of(LocalDateTime.now().minusDays(10)));
        event.setType(EventType.occupied);
        event.setValue(OCCUPIED);
        return event;
    }
}
