package fi.solita.adele;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class DataService {

    @Resource private JdbcTemplate jdbcTemplate;

    private static final RowMapper<Device> deviceRowMapper = (rs, rowNum) -> {
        Device device = new Device();
        device.setID(Integer.parseInt(rs.getString("ID")));
        device.setTime(rs.getString("TIME"));
        device.setType(rs.getString("TYPE"));
        device.setValue(Integer.parseInt(rs.getString("VALUE")));
        return device;
    };

    public List<Device> all() {
        String sql = "select * from DEVICE";
        return jdbcTemplate.query(sql, deviceRowMapper);
    }
}
