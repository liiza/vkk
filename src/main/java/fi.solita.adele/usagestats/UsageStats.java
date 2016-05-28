package fi.solita.adele.usagestats;

import fi.solita.adele.event.EventType;

public class UsageStats {
    private EventType type;
    private double average;

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }
}
