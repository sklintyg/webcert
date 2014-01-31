package se.inera.webcert.web.controller.api.dto;

import org.joda.time.LocalDateTime;

public class ListIntygEntry {

    private String intygId;

    private IntygSource source;

    private String intygType;

    private String status;

    private LocalDateTime lastUpdatedSigned;

    private String updatedSignedBy;
    
    private boolean discarded;

    public ListIntygEntry() {

    }

    public String getIntygId() {
        return intygId;
    }

    public void setIntygId(String intygId) {
        this.intygId = intygId;
    }

    public IntygSource getSource() {
        return source;
    }

    public void setSource(IntygSource source) {
        this.source = source;
    }

    public String getIntygType() {
        return intygType;
    }

    public void setIntygType(String intygType) {
        this.intygType = intygType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastUpdatedSigned() {
        return lastUpdatedSigned;
    }

    public void setLastUpdatedSigned(LocalDateTime lastUpdatedSigned) {
        this.lastUpdatedSigned = lastUpdatedSigned;
    }

    public String getUpdatedSignedBy() {
        return updatedSignedBy;
    }

    public void setUpdatedSignedBy(String updatedSignedBy) {
        this.updatedSignedBy = updatedSignedBy;
    }

    public boolean isDiscarded() {
        return discarded;
    }

    public void setDiscarded(boolean discarded) {
        this.discarded = discarded;
    }

    @Override
    public String toString() {
        return "ListIntygEntry [intygId=" + intygId + ", source=" + source + ", intygType=" + intygType + ", status="
                + status + ", lastUpdatedSigned=" + lastUpdatedSigned + ", updatedSignedBy=" + updatedSignedBy + "]";
    }

}
