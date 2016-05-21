package fi.solita.adele.event;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Repository
public class EventRepository {

    private static final String EVENT = "EVENT";
    private static final RowMapper<Event> deviceRowMapper = (rs, rowNum) -> {
        Event event = new Event();
        event.setID(rs.getInt("ID"));
        event.setDevice_id(rs.getInt("DEVICE_ID"));
        event.setPlace_id(rs.getInt("PLACE_ID"));
        event.setTime(LocalDateTime.ofInstant(rs.getTimestamp("TIME").toInstant(), ZoneId.systemDefault()));
        event.setType(rs.getString("TYPE"));
        event.setValue(rs.getDouble("VALUE"));
        return event;
    };

    @Resource
    private JdbcTemplate jdbcTemplate;

    public List<Event> all() {
        String sql = "select * from " + EVENT;
        return jdbcTemplate.query(sql, deviceRowMapper);
    }

    public int addEvent(Event event) {
        String sql = "insert into " + EVENT + " (device_id, place_id, time, type, value) values (?, ?, ?, ?, ?)";
        Object[] args = {event.getDevice_id(), event.getPlace_id(), Timestamp.valueOf(event.getTime()), event.getType(), event.getValue()};

        return jdbcTemplate.update(sql, args);
    }

    private Event getEvent(Integer id) {
        Object[] args = {id};
        return jdbcTemplate.queryForObject("select * from " + EVENT + " where id = ? ", args, deviceRowMapper);
    }
}
