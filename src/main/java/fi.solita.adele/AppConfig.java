package fi.solita.adele;

import com.googlecode.flyway.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
public class AppConfig {

    private static final String DB_URL = "jdbc:h2:mem:export;DB_CLOSE_DELAY=-1;MODE=Oracle";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "sa";

    @Value("${spring.datasource.driverClassName:h2}")
    private String dbDriver;

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        if (isH2()) {
            return h2DataSource();
        } else{
            return DataSourceBuilder.create().build();
        }
    }

    private DataSource h2DataSource(){
        JdbcDataSource dataSource = new JdbcDataSource();

        dataSource.setURL(DB_URL);
        dataSource.setUser(DB_USER);
        dataSource.setPassword(DB_PASSWORD);

        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

    @PostConstruct
    public void migrateDb() {
        flyway().migrate();
    }

    private Flyway flyway() {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource());
        flyway.setLocations("sql.common");
        if (isH2()) {
            flyway.setInitOnMigrate(true);
        }
        return flyway;
    }

    private boolean isH2() {
        return dbDriver.contains("h2");
    }
}