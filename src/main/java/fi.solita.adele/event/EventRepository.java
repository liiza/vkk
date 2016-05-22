package fi.solita.adele.event;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class EventRepository {

    private static final String EVENT = "EVENT";
    private static final RowMapper<Event> eventRowMapper = (rs, rowNum) -> {
        Event event = new Event();
        event.setID(rs.getInt("ID"));
        event.setDevice_id(rs.getInt("DEVICE_ID"));
        event.setPlace_id(rs.getInt("PLACE_ID"));
        event.setTime(LocalDateTime.ofInstant(rs.getTimestamp("TIME").toInstant(), ZoneId.systemDefault()));
        event.setType(EventType.valueOf(rs.getString("TYPE")));
        event.setValue(rs.getDouble("VALUE"));
        return event;
    };

    @Resource
    private JdbcTemplate jdbcTemplate;

    public List<Event> all() {
        String sql = "select * from " + EVENT;
        return jdbcTemplate.query(sql, eventRowMapper);
    }

    public int addEvent(final CreateEventCommand event) {
        EventValidator.validateCreate(event);
        final Optional<Integer> placeIdOptional = event.getPlace_id() != null ? event.getPlace_id() : Optional.empty();
        final int placeId = placeIdOptional.orElseGet(() -> getLastPlaceIdForDeviceId(event.getDevice_id()));
        final Optional<LocalDateTime> timeOptional = event.getTime() != null ? event.getTime() : Optional.empty();

        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final String sql = "insert into " + EVENT + " (device_id, place_id, time, type, value) values (?, ?, ?, ?, ?)";
        final PreparedStatementCreator statementCreator = connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, event.getDevice_id());
            ps.setInt(2, placeId);
            ps.setTimestamp(3, Timestamp.valueOf(timeOptional.orElse(LocalDateTime.now())));
            ps.setString(4, event.getType().toString());
            ps.setDouble(5, event.getValue());
            return ps;
        };

        jdbcTemplate.update(statementCreator, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public Event getEvent(int id) {
        Object[] args = {id};
        return jdbcTemplate.queryForObject("select * from " + EVENT + " where id = ? ", args, eventRowMapper);
    }

    public int getLastPlaceIdForDeviceId(final int deviceId) {
        final String sql = "select place_id, max(time) " +
                "from " + EVENT + " " +
                "where device_id = ? " +
                "group by place_id " +
                "order by 2 desc";
        final List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, deviceId);
        if(result.isEmpty()) {
            throw new NoPreviousEventForDeviceException(deviceId);
        }
        return (int) result.get(0).get("place_id");
    }
}
