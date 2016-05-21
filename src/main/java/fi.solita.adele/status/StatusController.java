package fi.solita.adele.status;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class StatusController {

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    String hello() {
        return "ok";
    }
}
