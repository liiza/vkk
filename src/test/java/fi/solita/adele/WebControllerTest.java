package fi.solita.adele;

import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.is;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebAppConfiguration
@IntegrationTest({"server.port:0",
        "spring.datasource.url:jdbc:h2:mem:vkk;DB_CLOSE_ON_EXIT=FALSE"})
public class WebControllerTest {
    @Value("${local.server.port}")
    int port;

    @Before
    public void setUp() throws Exception {
        RestAssured.port = port;
    }

    @Test
    public void testStatus() throws Exception {
        when().get("/status").then()
                .body(is("ok"));
    }

//    @Test
//    public void testCalc() throws Exception {
//        given().param("left", 100)
//                .param("right", 200)
//                .get("/calc")
//                .then()
//                .body("left", is(100))
//                .body("right", is(200))
//                .body("answer", is(300));
//    }
}