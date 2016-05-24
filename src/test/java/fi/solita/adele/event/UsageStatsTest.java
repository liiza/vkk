package fi.solita.adele.event;

import fi.solita.adele.App;
import fi.solita.adele.place.CreatePlaceCommand;
import fi.solita.adele.place.PlaceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebAppConfiguration
@IntegrationTest({"server.port:0"})
public class UsageStatsTest {

    static final double OCCUPIED = 1;
    static final double FREE = 0;

    @Resource
    PlaceRepository placeRepository;

    @Resource
    EventRepository eventRepository;

    @Test
    public void should_calculate_usage_stats_for_a_place() {
        int placeId = createPlace();
        int deviceId = 1;
        createOccupiedEvent(placeId, deviceId, OCCUPIED);
        createOccupiedEvent(placeId, deviceId, OCCUPIED);
        createOccupiedEvent(placeId, deviceId, OCCUPIED);
        createOccupiedEvent(placeId, deviceId, FREE);

        GetUsageStatsCommand query = new GetUsageStatsCommand();
        query.setStarting(LocalDateTime.now().minusDays(1));
        query.setEnding(LocalDateTime.now().plusDays(1));
        UsageStats usageStats = eventRepository.getUsageStats(query);

        assertTrue(Math.abs(usageStats.getAverage() - ((double)3/4)) < 0.01);
    }

    private int createPlace() {
        CreatePlaceCommand place = new CreatePlaceCommand();
        place.setLatitude(23);
        place.setLongitude(34);
        place.setName("test-place");
        return placeRepository.addPlace(place);
    }

    private void createOccupiedEvent(int placeId, int deviceId, double occupied) {
        CreateEventCommand event = new CreateEventCommand();
        event.setPlace_id(Optional.of(placeId));
        event.setType(EventType.occupied);
        event.setValue(occupied);
        event.setDevice_id(deviceId);
        eventRepository.addEvent(event);
    }
}
