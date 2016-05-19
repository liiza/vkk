package fi.solita.adele.status;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class StatusService {

    public List<PlaceStatus> getCurrentStatusForAllPlaces() {
        return Arrays.asList(
                new PlaceStatus().setPlace_id(1).setLatitude(60.17667308232766).setLongitude(24.93886023759842).setOccupied(randomBoolean()),
                new PlaceStatus().setPlace_id(2).setLatitude(60.17667274884376).setLongitude(24.93882603943349).setOccupied(randomBoolean()),
                new PlaceStatus().setPlace_id(3).setLatitude(60.17667274884376).setLongitude(24.93879653513432).setOccupied(randomBoolean()),
                new PlaceStatus().setPlace_id(4).setLatitude(60.17667108142430).setLongitude(24.93876770138741).setOccupied(randomBoolean()),
                new PlaceStatus().setPlace_id(5).setLatitude(60.17660104972962).setLongitude(24.93877507746220).setOccupied(randomBoolean()),
                new PlaceStatus().setPlace_id(6).setLatitude(60.17660238366804).setLongitude(24.93886761367322).setOccupied(randomBoolean()),
                new PlaceStatus().setPlace_id(7).setLatitude(60.17703224250752).setLongitude(24.93876770138741).setOccupied(randomBoolean()),
                new PlaceStatus().setPlace_id(8).setLatitude(60.17703057510630).setLongitude(24.93873752653599).setOccupied(randomBoolean()),
                new PlaceStatus().setPlace_id(9).setLatitude(60.17710093936467).setLongitude(24.93872813880444).setOccupied(randomBoolean())
        );
    }

    private static boolean randomBoolean() {
        return Math.random() > 0.5;
    }
}
