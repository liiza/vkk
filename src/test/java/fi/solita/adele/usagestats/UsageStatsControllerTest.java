package fi.solita.adele.usagestats;

import fi.solita.adele.App;
import fi.solita.adele.EventTestUtil;
import fi.solita.adele.PlaceTestUtil;
import fi.solita.adele.event.EventType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static fi.solita.adele.EventTestUtil.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebAppConfiguration
@IntegrationTest({"server.port:0"})
public class UsageStatsControllerTest {
    private static final RestTemplate restTemplate = new RestTemplate();

    @Value("${local.server.port}")
    int port;

    private PlaceTestUtil placeTestUtil;
    private EventTestUtil eventTestUtil;

    @Before
    public void setup() {
        placeTestUtil = new PlaceTestUtil(port);
        eventTestUtil = new EventTestUtil(port);
    }

    private String url(String suffix) {
        return "http://localhost:" + port + suffix;
    }

    private UsageStats getUsageStats(Optional<LocalDateTime> starting,
                                     Optional<LocalDateTime> ending,
                                     Optional<Integer[]> device_id,
                                     Optional<Integer[]> place_id,
                                     Optional<EventType> type) {

        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(url("/v1/query/usagestats"));

        starting.ifPresent(v -> uri.queryParam("starting", v));
        ending.ifPresent(v -> uri.queryParam("ending", v));
        device_id.map(Arrays::asList).orElse(new ArrayList<>()).forEach(v -> uri.queryParam("device_id", v));
        place_id.map(Arrays::asList).orElse(new ArrayList<>()).forEach(v -> uri.queryParam("place_id", v));
        type.ifPresent(v -> uri.queryParam("type", v));

        return restTemplate.getForObject(uri.build().toUri(), UsageStats.class);
    }

    @Test
    public void should_calculate_usage_stats_for_a_place() {
        int placeId = placeTestUtil.addPlace();
        int deviceId = 700;
        eventTestUtil.addEvent(deviceId, placeId, LocalDateTime.now().minusDays(2), FREE);
        eventTestUtil.addEvent(deviceId, placeId, LocalDateTime.now().minusHours(3), OCCUPIED);
        eventTestUtil.addEvent(deviceId, placeId, LocalDateTime.now().minusHours(2), OCCUPIED);
        eventTestUtil.addEvent(deviceId, placeId, LocalDateTime.now().minusHours(1), OCCUPIED);
        eventTestUtil.addEvent(deviceId, placeId, LocalDateTime.now(), FREE);
        eventTestUtil.addEvent(deviceId, placeId, LocalDateTime.now().plusDays(2), FREE);

        UsageStats usageStats = getUsageStats(
                Optional.of(LocalDateTime.now().minusDays(1)),
                Optional.of(LocalDateTime.now().plusDays(1)),
                Optional.empty(),
                Optional.of(new Integer[] {placeId}),
                Optional.empty());

        assertEquals(((double)3/4), usageStats.getAverage(), EVENT_VALUE_COMPARISON_DELTA);
        assertEquals(EventType.occupied, usageStats.getType());
    }

    @Test
    public void should_handle_period_with_no_event() {
        int placeId = placeTestUtil.addPlace();

        UsageStats usageStats = getUsageStats(
                Optional.of(LocalDateTime.now().minusDays(1)),
                Optional.of(LocalDateTime.now().plusDays(1)),
                Optional.empty(),
                Optional.of(new Integer[] {placeId}),
                Optional.empty());

        assertEquals(0.0, usageStats.getAverage(), EVENT_VALUE_COMPARISON_DELTA);
        assertEquals(EventType.occupied, usageStats.getType());
    }
}