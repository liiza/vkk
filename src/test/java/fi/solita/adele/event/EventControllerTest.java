package fi.solita.adele.event;

import fi.solita.adele.App;
import fi.solita.adele.EventTestUtil;
import fi.solita.adele.PlaceTestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static fi.solita.adele.EventTestUtil.*;
import static fi.solita.adele.CommonTestUtil.*;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebAppConfiguration
@IntegrationTest({"server.port:0"})
public class EventControllerTest {

    @Value("${local.server.port}")
    int port;

    private PlaceTestUtil placeTestUtil;
    private EventTestUtil eventTestUtil;

    @Before
    public void setup() {
        placeTestUtil = new PlaceTestUtil(port);
        eventTestUtil = new EventTestUtil(port);
    }

    @Test
    public void should_list_all_events() {
        int placeId = placeTestUtil.addPlace();

        CreateEventCommand event = new CreateEventCommand();
        event.setDevice_id(100);
        event.setPlace_id(Optional.of(placeId));
        event.setTime(Optional.of(LocalDateTime.now()));
        event.setType(EventType.occupied);
        event.setValue(OCCUPIED);

        int eventId = eventTestUtil.addEvent(event);
        Optional<Event> savedEvent = eventTestUtil.getAllEvents().stream().filter(e -> e.getID() == eventId).findFirst();

        assertTrue(savedEvent.isPresent());
        assertEventsEqual(event, savedEvent.get());
    }

    @Test
    public void should_list_events_by_date_range() {
        int placeId = placeTestUtil.addPlace();

        CreateEventCommand event1 = eventCommand(placeId);
        event1.setTime(Optional.of(LocalDateTime.now().minusDays(10)));
        int eventId1 = eventTestUtil.addEvent(event1);

        CreateEventCommand event2 = eventCommand(placeId);
        event2.setTime(Optional.of(LocalDateTime.now().minusDays(5)));
        int eventId2 = eventTestUtil.addEvent(event2);

        CreateEventCommand event3 = eventCommand(placeId);
        event3.setTime(Optional.of(LocalDateTime.now().minusDays(1)));
        int eventId3 = eventTestUtil.addEvent(event3);

        Set<Integer> eventIds = eventTestUtil.getAllEvents(
                Optional.of(LocalDateTime.now().minusDays(7)),
                Optional.of(LocalDateTime.now().minusDays(2)),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        ).stream().map(Event::getID).collect(Collectors.toSet());

        assertContainsAll(eventIds, Arrays.asList(eventId2));
        assertNotContainsAny(eventIds, Arrays.asList(eventId1, eventId3));
    }

    @Test
    public void should_list_events_by_device_ids() {
        int placeId = placeTestUtil.addPlace();

        CreateEventCommand event1 = eventCommand(placeId);
        event1.setDevice_id(1001);
        int eventId1 = eventTestUtil.addEvent(event1);

        CreateEventCommand event2 = eventCommand(placeId);
        event2.setDevice_id(1002);
        int eventId2 = eventTestUtil.addEvent(event2);

        CreateEventCommand event3 = eventCommand(placeId);
        event3.setDevice_id(1003);
        int eventId3 = eventTestUtil.addEvent(event3);

        Set<Integer> eventIds = eventTestUtil.getAllEvents(
                Optional.empty(),
                Optional.empty(),
                Optional.of(new Integer[] {1002, 1003}),
                Optional.empty(),
                Optional.empty()
        ).stream().map(Event::getID).collect(Collectors.toSet());

        assertContainsAll(eventIds, Arrays.asList(eventId2, eventId3));
        assertNotContainsAny(eventIds, Arrays.asList(eventId1));
    }

    @Test
    public void should_list_events_by_place_ids() {
        int placeId1 = placeTestUtil.addPlace();
        int placeId2 = placeTestUtil.addPlace();
        int placeId3 = placeTestUtil.addPlace();

        CreateEventCommand event1 = eventCommand(placeId1);
        int eventId1 = eventTestUtil.addEvent(event1);

        CreateEventCommand event2 = eventCommand(placeId2);
        int eventId2 = eventTestUtil.addEvent(event2);

        CreateEventCommand event3 = eventCommand(placeId3);
        int eventId3 = eventTestUtil.addEvent(event3);

        Set<Integer> eventIds = eventTestUtil.getAllEvents(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(new Integer[] {placeId2, placeId3}),
                Optional.empty()
        ).stream().map(Event::getID).collect(Collectors.toSet());

        assertContainsAll(eventIds, Arrays.asList(eventId2, eventId3));
        assertNotContainsAny(eventIds, Arrays.asList(eventId1));
    }

    @Test
    public void should_list_events_by_type() {
        int placeId = placeTestUtil.addPlace();

        CreateEventCommand event1 = eventCommand(placeId);
        event1.setType(EventType.occupied);
        int eventId1 = eventTestUtil.addEvent(event1);

        CreateEventCommand event2 = eventCommand(placeId);
        event2.setType(EventType.closed);
        int eventId2 = eventTestUtil.addEvent(event2);

        CreateEventCommand event3 = eventCommand(placeId);
        event3.setType(EventType.movement);
        int eventId3 = eventTestUtil.addEvent(event3);

        Set<Integer> eventIds = eventTestUtil.getAllEvents(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(EventType.movement)
        ).stream().map(Event::getID).collect(Collectors.toSet());

        assertContainsAll(eventIds, Arrays.asList(eventId3));
        assertNotContainsAny(eventIds, Arrays.asList(eventId1, eventId2));
    }

    @Test
    public void should_add_new_event() {
        int placeId = placeTestUtil.addPlace();

        CreateEventCommand event = new CreateEventCommand();
        event.setDevice_id(100);
        event.setPlace_id(Optional.of(placeId));
        event.setTime(Optional.of(LocalDateTime.now()));
        event.setType(EventType.occupied);
        event.setValue(OCCUPIED);

        int eventId = eventTestUtil.addEvent(event);
        Event savedEvent = eventTestUtil.getEvent(eventId);
        assertEventsEqual(event, savedEvent);
    }

    @Test
    public void should_add_new_event_to_previous_place() {
        int placeId1 = placeTestUtil.addPlace();
        int placeId2 = placeTestUtil.addPlace();

        CreateEventCommand command1 = new CreateEventCommand();
        command1.setDevice_id(200);
        command1.setPlace_id(Optional.of(placeId1));
        command1.setTime(Optional.of(LocalDateTime.now().minusDays(2)));
        command1.setType(EventType.occupied);
        command1.setValue(OCCUPIED);
        eventTestUtil.addEvent(command1);

        CreateEventCommand command2 = new CreateEventCommand();
        command2.setDevice_id(200);
        command2.setPlace_id(Optional.of(placeId2));
        command2.setTime(Optional.of(LocalDateTime.now().minusDays(1)));
        command2.setType(EventType.occupied);
        command2.setValue(0.0);
        eventTestUtil.addEvent(command2);

        CreateEventCommand command3 = new CreateEventCommand();
        command3.setDevice_id(200);
        command3.setPlace_id(Optional.empty());
        command3.setTime(Optional.of(LocalDateTime.now().plusDays(1)));
        command3.setType(EventType.occupied);
        command3.setValue(0.0);
        int eventId3 = eventTestUtil.addEvent(command3);

        Event event = eventTestUtil.getEvent(eventId3);

        assertEquals(command3.getDevice_id().intValue(), event.getDevice_id());
        assertEquals(placeId2, event.getPlace_id());
        assertEquals(command3.getTime().get(), event.getTime());
        assertEquals(command3.getType(), event.getType());
        assertEquals(command3.getValue(), event.getValue(), EVENT_VALUE_COMPARISON_DELTA);
    }

    @Test
    public void should_not_add_new_event_if_there_is_no_previous_event_for_device() {
        placeTestUtil.addPlace();

        CreateEventCommand command1 = new CreateEventCommand();
        command1.setDevice_id(300);
        command1.setPlace_id(Optional.empty());
        command1.setTime(Optional.of(LocalDateTime.now().minusDays(1)));
        command1.setType(EventType.occupied);
        command1.setValue(OCCUPIED);

        try {
            eventTestUtil.addEvent(command1);
            fail();
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
            assertEquals("No previous event for device 300", ex.getResponseBodyAsString());
        }
    }

    @Test
    public void should_add_new_event_without_time_to_current_time() {
        int placeId = placeTestUtil.addPlace();

        CreateEventCommand command = new CreateEventCommand();
        command.setDevice_id(100);
        command.setPlace_id(Optional.of(placeId));
        command.setTime(Optional.empty());
        command.setType(EventType.occupied);
        command.setValue(OCCUPIED);

        int eventId = eventTestUtil.addEvent(command);
        Event event = eventTestUtil.getEvent(eventId);

        assertEquals(command.getDevice_id().intValue(), event.getDevice_id());
        assertEquals(command.getPlace_id().get().intValue(), event.getPlace_id());
        assertTrue(event.getTime().toLocalDate().equals(LocalDate.now()));
        assertEquals(command.getType(), event.getType());
        assertEquals(command.getValue(), event.getValue(), EVENT_VALUE_COMPARISON_DELTA);
    }

    @Test
    public void should_not_add_new_event_with_missing_device_id() {
        try {
            eventTestUtil.addEvent(null, placeTestUtil.addPlace(), LocalDateTime.now(), EventType.occupied.toString(), OCCUPIED);
            fail();
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            assertEquals("Event Device Id is missing", ex.getResponseBodyAsString());
        }
    }

    @Test
    public void should_not_add_new_event_with_missing_place_id() {
        try {
            eventTestUtil.addEvent(600, null, LocalDateTime.now(), EventType.occupied.toString(), OCCUPIED);
            fail();
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
            assertEquals("No previous event for device 600", ex.getResponseBodyAsString());
        }
    }

    @Test
    public void should_not_add_new_event_with_missing_type() {
        try {
            eventTestUtil.addEvent(600, placeTestUtil.addPlace(), LocalDateTime.now(), null, OCCUPIED);
            fail();
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            assertEquals("Event type is missing", ex.getResponseBodyAsString());
        }
    }

    @Test
    public void should_not_add_new_event_with_unknown_type() {
        try {
            eventTestUtil.addEvent(600, placeTestUtil.addPlace(), LocalDateTime.now(), "abc", OCCUPIED);
            fail();
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            assertTrue(ex.getResponseBodyAsString().startsWith(
                    "Can not construct instance of fi.solita.adele.event.EventType " + "from String value 'abc': value not one of declared Enum instance names"));
        }
    }

    @Test
    public void should_not_add_new_event_with_missing_value() {
        try {
            eventTestUtil.addEvent(600, placeTestUtil.addPlace(), LocalDateTime.now(), EventType.occupied.toString(), null);
            fail();
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            assertEquals("Event value is missing", ex.getResponseBodyAsString());
        }
    }
}