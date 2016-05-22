package fi.solita.adele.event;

import java.time.LocalDateTime;
import java.util.Optional;

public class CreateEventCommand {
    private Integer device_id;
    private Optional<Integer> place_id;
    private Optional<LocalDateTime> time;
    private EventType type;
    private Double value;

    public Integer getDevice_id() {
        return device_id;
    }

    public void setDevice_id(Integer device_id) {
        this.device_id = device_id;
    }

    public Optional<Integer> getPlace_id() {
        return place_id;
    }

    public void setPlace_id(Optional<Integer> place_id) {
        this.place_id = place_id;
    }

    public Optional<LocalDateTime> getTime() {
        return time;
    }

    public void setTime(Optional<LocalDateTime> time) {
        this.time = time;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
