package fi.solita.adele.place.status;

import fi.solita.adele.App;
import fi.solita.adele.EventTestUtil;
import fi.solita.adele.PlaceTestUtil;
import fi.solita.adele.place.Place;
import org.junit.Before;
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

import static fi.solita.adele.EventTestUtil.FREE;
import static fi.solita.adele.EventTestUtil.OCCUPIED;
import static fi.solita.adele.PlaceTestUtil.LOCATION_COMPARISON_DELTA;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebAppConfiguration
@IntegrationTest({"server.port:0"})
public class PlaceStatusControllerTest {
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

    private List<PlaceStatus> getCurrentStatusForAllPlaces() {
        ResponseEntity<PlaceStatus[]> result = restTemplate.getForEntity(url("/v1/status/current"), PlaceStatus[].class);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        return Arrays.asList(result.getBody());
    }

    @Test
    public void should_list_current_state_for_all_places() {
        int deviceId = 1;

        final int placeId1 = placeTestUtil.addPlace();
        final int placeId2 = placeTestUtil.addPlace();
        final int placeId3 = placeTestUtil.addPlace();

        final Place place1 = placeTestUtil.getPlace(placeId1);
        final Place place2 = placeTestUtil.getPlace(placeId2);
        final Place place3 = placeTestUtil.getPlace(placeId3);

        eventTestUtil.addEvent(deviceId, placeId1, LocalDateTime.now().minusDays(3), FREE);
        eventTestUtil.addEvent(deviceId, placeId1, LocalDateTime.now().minusDays(2), FREE);
        eventTestUtil.addEvent(deviceId, placeId1, LocalDateTime.now().minusDays(1), OCCUPIED);

        eventTestUtil.addEvent(deviceId, placeId2, LocalDateTime.now().minusDays(3), OCCUPIED);
        eventTestUtil.addEvent(deviceId, placeId2, LocalDateTime.now().minusDays(2), OCCUPIED);
        eventTestUtil.addEvent(deviceId, placeId2, LocalDateTime.now().minusDays(1), FREE);

        List<PlaceStatus> result = getCurrentStatusForAllPlaces();
        assertNotNull(result);

        Optional<PlaceStatus> place1Status = result.stream().filter(status -> status.getPlace_id() == placeId1).findFirst();
        assertTrue(place1Status.isPresent());
        assertTrue(place1Status.get().isOccupied());
        assertEquals(place1.getLongitude(), place1Status.get().getLongitude(), LOCATION_COMPARISON_DELTA);
        assertEquals(place1.getLatitude(), place1Status.get().getLatitude(), LOCATION_COMPARISON_DELTA);

        Optional<PlaceStatus> place2Status = result.stream().filter(status -> status.getPlace_id() == placeId2).findFirst();
        assertTrue(place2Status.isPresent());
        assertFalse(place2Status.get().isOccupied());
        assertEquals(place2.getLongitude(), place2Status.get().getLongitude(), LOCATION_COMPARISON_DELTA);
        assertEquals(place2.getLatitude(), place2Status.get().getLatitude(), LOCATION_COMPARISON_DELTA);

        assertFalse(result.stream().anyMatch(status -> status.getPlace_id() == placeId3));
    }
}