package fi.solita.adele.place;

import fi.solita.adele.App;
import fi.solita.adele.PlaceTestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

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

    private PlaceTestUtil placeTestUtil;

    @Before
    public void setup() {
        placeTestUtil = new PlaceTestUtil(port);
    }

    @Test
    public void should_list_all_places() {
        CreatePlaceCommand place = new CreatePlaceCommand();
        place.setName("Paikka 2");
        place.setLatitude(875.99856);
        place.setLongitude(984.98449);

        int id = placeTestUtil.addPlace(place);
        Optional<Place> savedPlaceOptional = placeTestUtil.getAllPlaces().stream().filter(p -> p.getId() == id).findFirst();

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

        int id = placeTestUtil.addPlace(place);
        Place savedPlace = placeTestUtil.getPlace(id);

        assertEquals(place.getName(), savedPlace.getName());
        assertEquals(place.getLongitude(), savedPlace.getLongitude(), 0.001);
        assertEquals(place.getLatitude(), savedPlace.getLatitude(), 0.001);

    }
}