package fi.solita.adele;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class HelloController {

    @Resource JdbcTemplate jdbcTemplate;

    @RequestMapping("/")
    String hello() {
        return "Hello World!";
    }

    @RequestMapping("/testDB")
    String test(){
        return this.jdbcTemplate.queryForList("select * from DEVICE").toString();
    }
}
