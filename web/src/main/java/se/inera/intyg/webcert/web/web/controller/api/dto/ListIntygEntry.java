package se.inera.intyg.webcert.web.web.controller.api.dto;

import org.joda.time.LocalDateTime;
import se.inera.certificate.modules.support.api.dto.Personnummer;

public class ListIntygEntry {

    private String intygId;

    private Personnummer patientId;

    private IntygSource source;

    private String intygType;

    private String status;

    private LocalDateTime lastUpdatedSigned;

    private String updatedSignedBy;

    private boolean vidarebefordrad;

    private long version;

    public ListIntygEntry() {

    }

    public String getIntygId() {
        return intygId;
    }

    public void setIntygId(String intygId) {
        this.intygId = intygId;
    }

    public Personnummer getPatientId() {
        return patientId;
    }

    public void setPatientId(Personnummer patientId) {
        this.patientId = patientId;
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

    public boolean isVidarebefordrad() {
        return vidarebefordrad;
    }

    public void setVidarebefordrad(boolean vidarebefordrad) {
        this.vidarebefordrad = vidarebefordrad;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
