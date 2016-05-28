package fi.solita.adele.usagestats;

import fi.solita.adele.event.EventType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UsageStatsRepository {

    private static final RowMapper<UsageStats> usageStatsRowMapper = (rs, rowNum) -> {
        UsageStats usageStats = new UsageStats();
        usageStats.setAverage(rs.getDouble("usage"));
        usageStats.setType(EventType.valueOf(rs.getString("type")));
        return usageStats;
    };

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public UsageStats getUsageStats(final Optional<LocalDateTime> starting,
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

        final EventType eventType = type.orElse(EventType.occupied);
        where.add("type = :type");
        params.addValue("type", eventType.toString());

        String whereSql = "";
        if (where.size() > 0) {
            whereSql = " where " + where.stream().collect(Collectors.joining(" AND "));
        }

        final String sql = "select type, avg(value) as usage " +
                "from event " +
                whereSql + " " +
                "group by type";

        try {
            return namedParameterJdbcTemplate.queryForObject(sql, params, usageStatsRowMapper);
        } catch (EmptyResultDataAccessException e) {
            UsageStats usageStats = new UsageStats();
            usageStats.setAverage(0.0);
            usageStats.setType(eventType);
            return usageStats;
        }
    }

}
