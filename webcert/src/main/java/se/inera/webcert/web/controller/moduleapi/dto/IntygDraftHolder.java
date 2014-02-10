package se.inera.webcert.web.controller.moduleapi.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;

import se.inera.webcert.persistence.intyg.model.IntygsStatus;

/**
 * Container for a draft and its current status.
 * 
 * @author nikpet
 *
 */
public class IntygDraftHolder {

    private IntygsStatus status;

    @JsonRawValue
    private String content;

    public IntygDraftHolder() {

    }

    public IntygsStatus getStatus() {
        return status;
    }

    public void setStatus(IntygsStatus status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
