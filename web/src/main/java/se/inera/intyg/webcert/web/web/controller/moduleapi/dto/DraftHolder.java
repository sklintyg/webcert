package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;

import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;

/**
 * Container for a draft and its current status.
 *
 * @author nikpet
 */
public class DraftHolder {

    private long version;

    private boolean vidarebefordrad;

    private UtkastStatus status;

    private String enhetsNamn;

    private String vardgivareNamn;

    @JsonRawValue
    private String content;

    public DraftHolder() {

    }

    public long getVersion() {
        return version;
    }


    public void setVersion(long version) {
        this.version = version;
    }


    public boolean isVidarebefordrad() {
        return vidarebefordrad;
    }

    public void setVidarebefordrad(boolean vidarebefordrad) {
        this.vidarebefordrad = vidarebefordrad;
    }

    public UtkastStatus getStatus() {
        return status;
    }

    public void setStatus(UtkastStatus status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEnhetsNamn() {
        return enhetsNamn;
    }

    public void setEnhetsNamn(String enhetsNamn) {
        this.enhetsNamn = enhetsNamn;
    }

    public String getVardgivareNamn() {
        return vardgivareNamn;
    }

    public void setVardgivareNamn(String vardgivareNamn) {
        this.vardgivareNamn = vardgivareNamn;
    }
}
