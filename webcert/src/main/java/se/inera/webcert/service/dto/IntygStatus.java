package se.inera.webcert.service.dto;

import org.joda.time.LocalDateTime;

public class IntygStatus {
    
    private String type;

    private String target;

    private LocalDateTime timestamp;
    
    public IntygStatus() {
        
    }

    public IntygStatus(String type, String target, LocalDateTime timestamp) {
        this.type = type;
        this.target = target;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
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
