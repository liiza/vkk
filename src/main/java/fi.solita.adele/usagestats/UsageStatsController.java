package fi.solita.adele.usagestats;

import fi.solita.adele.event.EventType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Optional;

@CrossOrigin
@RestController
public class UsageStatsController {

    @Resource
    private UsageStatsRepository usageStatsRepository;

    @RequestMapping(value = "/v1/query/usagestats", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    UsageStats usageStats(@RequestParam(value = "starting", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> starting,
                          @RequestParam(value = "ending", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> ending,
                          @RequestParam(value = "device_id", required = false) Integer[] device_id,
                          @RequestParam(value = "place_id", required = false) Integer[] place_id,
                          @RequestParam(value = "type", required = false) Optional<EventType> type) {
        return usageStatsRepository.getUsageStats(starting, ending, Optional.ofNullable(device_id), Optional.ofNullable(place_id), type);
    }
}
