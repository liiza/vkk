package fi.solita.adele;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
}
