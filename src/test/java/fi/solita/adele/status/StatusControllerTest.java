package fi.solita.adele.status;

import fi.solita.adele.App;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebAppConfiguration
@IntegrationTest({"server.port:0"})
public class StatusControllerTest {
    @Value("${local.server.port}")
    int port;

    RestTemplate restTemplate = new RestTemplate();

    private String url(String suffix) {
        return "http://localhost:" + port + suffix;
    }

    @Test
    public void status_should_return_ok() throws Exception {
        ResponseEntity<String> result = restTemplate.getForEntity(url("/status"), String.class);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("ok", result.getBody());
    }
}