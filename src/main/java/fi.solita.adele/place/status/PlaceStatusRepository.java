package fi.solita.adele.place.status;

import fi.solita.adele.event.EventType;
import fi.solita.adele.event.OccupiedStatusSolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class PlaceStatusRepository {

    private static final RowMapper<PlaceStatus> placeStatusRowMapper = (rs, rowNum) -> {
        PlaceStatus status = new PlaceStatus();
        status.setPlace_id(rs.getInt("place_id"));
        status.setOccupied(OccupiedStatusSolver.isOccupied(EventType.valueOf(rs.getString("type")), rs.getDouble("value")));
        status.setLatitude(rs.getDouble("latitude"));
        status.setLongitude(rs.getDouble("longitude"));
        return status;
    };

    @Resource
    private JdbcTemplate jdbcTemplate;

    public List<PlaceStatus> getCurrentStatusForAllPlaces() {
        final String sql = "select e.place_id, p.latitude, p.longitude, e.type, e.value " +
                "from event as e " +
                "left join place as p on e.place_id = p.id " +
                "inner join (" +
                "  select place_id, max(time) as latest_time" +
                "  from event" +
                "  group by place_id" +
                ") as latest_event on latest_event.place_id = e.place_id and latest_event.latest_time = e.time ";
        return jdbcTemplate.query(sql, placeStatusRowMapper);
    }
}
