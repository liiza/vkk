package fi.solita.adele.place;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository
public class PlaceRepository {

    private static final String PLACE = "PLACE";
    private static final RowMapper<Place> placeRowMapper = (rs, rowNum) -> {
        Place place = new Place();
        place.setId(rs.getInt("ID"));
        place.setName(rs.getString("NAME"));
        place.setLatitude(rs.getDouble("LATITUDE"));
        place.setLongitude(rs.getDouble("LONGITUDE"));
        return place;
    };

    @Resource
    private JdbcTemplate jdbcTemplate;

    public List<Place> all() {
        String sql = "select * from " + PLACE;
        return jdbcTemplate.query(sql, placeRowMapper);
    }

    public int addPlace(final Place place) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final String sql = "insert into " + PLACE + " (name, latitude, longitude) values (?, ?, ?)";
        PreparedStatementCreator statementCreator = new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, place.getName());
                ps.setDouble(2, place.getLatitude());
                ps.setDouble(3, place.getLongitude());
                return ps;
            }
        };

        jdbcTemplate.update(statementCreator, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public Place getPlace(int id) {
        Object[] args = {id};
        return jdbcTemplate.queryForObject("select * from " + PLACE + " where id = ? ", args, placeRowMapper);
    }
}
