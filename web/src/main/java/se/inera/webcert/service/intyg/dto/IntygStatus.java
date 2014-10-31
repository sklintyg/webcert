package se.inera.webcert.service.intyg.dto;

import org.joda.time.LocalDateTime;

public class IntygStatus {

    private StatusType type;

    private String target;

    private LocalDateTime timestamp;

    public IntygStatus() {

    }

    public IntygStatus(StatusType type, String target, LocalDateTime timestamp) {
        this.type = type;
        this.target = target;
        this.timestamp = timestamp;
    }

    public StatusType getType() {
        return type;
    }

    public void setType(StatusType type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

}
