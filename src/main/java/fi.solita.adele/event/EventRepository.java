package fi.solita.adele.event;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class EventRepository {

    private static final String EVENT = "EVENT";
    private static final RowMapper<Event> eventRowMapper = (rs, rowNum) -> {
        Event event = new Event();
        event.setID(rs.getInt("id"));
        event.setDevice_id(rs.getInt("device_id"));
        event.setPlace_id(rs.getInt("place_id"));
        event.setTime(LocalDateTime.ofInstant(rs.getTimestamp("time").toInstant(), ZoneId.systemDefault()));
        event.setType(EventType.valueOf(rs.getString("type")));
        event.setValue(rs.getDouble("value"));
        return event;
    };

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<Event> all(final Optional<LocalDateTime> starting,
                           final Optional<LocalDateTime> ending,
                           final Optional<Integer[]> device_id,
                           final Optional<Integer[]> place_id,
                           final Optional<EventType> type) {
        final List<String> where = new ArrayList<>();
        final MapSqlParameterSource params = new MapSqlParameterSource();

        starting.ifPresent(start -> {
            where.add("time >= :starting");
            params.addValue("starting", Timestamp.valueOf(start));
        });
        ending.ifPresent(end -> {
            where.add("time <= :ending");
            params.addValue("ending", Timestamp.valueOf(end));
        });
        device_id.filter(ids -> ids.length > 0).ifPresent(ids -> {
            where.add("device_id IN (:device_id)");
            params.addValue("device_id", Arrays.asList(ids));
        });
        place_id.filter(ids -> ids.length > 0).ifPresent(ids -> {
            where.add("place_id IN (:place_id)");
            params.addValue("place_id", Arrays.asList(ids));
        });
        type.ifPresent(t -> {
            where.add("type = :type");
            params.addValue("type", t.toString());
        });

        String whereSql = "";
        if( where.size() > 0) {
            whereSql = " where " + where.stream().collect(Collectors.joining(" AND "));
        }

        final String sql = "select * " +
                "from " + EVENT +
                whereSql;

        return namedParameterJdbcTemplate.query(sql, params, eventRowMapper);
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

    public Event getEvent(final int id) {
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
        if (result.isEmpty()) {
            throw new NoPreviousEventForDeviceException(deviceId);
        }
        return (int) result.get(0).get("place_id");
    }

}
