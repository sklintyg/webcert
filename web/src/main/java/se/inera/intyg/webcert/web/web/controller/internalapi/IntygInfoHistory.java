package se.inera.intyg.webcert.web.web.controller.internalapi;

import java.time.LocalDateTime;

public class IntygInfoHistory {

    private LocalDateTime date;
    private String text;
    private String hsaId;

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }
}
