package fi.solita.adele.event;

import java.time.LocalDateTime;

public class GetUsageStatsCommand {
    private LocalDateTime starting;
    private LocalDateTime ending;

    public LocalDateTime getStarting() {
        return starting;
    }

    public void setStarting(LocalDateTime starting) {
        this.starting = starting;
    }

    public LocalDateTime getEnding() {
        return ending;
    }

    public void setEnding(LocalDateTime ending) {
        this.ending = ending;
    }
}
