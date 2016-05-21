package fi.solita.adele;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataService {

    private static final String EVENT = "EVENT";
    private static final RowMapper<Event> deviceRowMapper = (rs, rowNum) -> {
        Event event = new Event();
        event.setID(Integer.parseInt(rs.getString("ID")));
        event.setTime(rs.getString("TIME"));
        event.setType(rs.getString("TYPE"));
        event.setValue(Double.parseDouble(rs.getString("VALUE")));
        return event;
    };

    @Resource private JdbcTemplate jdbcTemplate;

    public List<Event> all() {
        String sql = "select * from " + EVENT;
        return jdbcTemplate.query(sql, deviceRowMapper);
    }

    public int addEvent(Event event) {
        String sql = "insert into " + EVENT + " (time, type, value) values (?, ?, ?)";
        Object[] args = {event.getTime() != null ? event.getTime() : LocalDate.now().toString(),
                                event.getType(),
                                event.getValue()};

        return jdbcTemplate.update(sql, args);
    }

    private Event getEvent(Integer id) {
        Object[] args = {id};
        return jdbcTemplate.queryForObject("select * from " + EVENT + " where id = ? ", args, deviceRowMapper);
    }
}
