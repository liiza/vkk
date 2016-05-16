package fi.solita.adele;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class WebController {

    @Resource private DataService dataService;

    @RequestMapping("/status") String hello() {
        return "ok";
    }

    @RequestMapping("/all") List<Device> getAllData() {
        return dataService.all();
    }
}
