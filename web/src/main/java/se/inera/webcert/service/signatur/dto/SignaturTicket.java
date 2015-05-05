package se.inera.webcert.service.signatur.dto;

import org.joda.time.LocalDateTime;

public class SignaturTicket {

    public enum Status {
        BEARBETAR, SIGNERAD, OKAND
    }

    private final String id;
    private final Status status;
    private final String intygsId;
    private final long version;
    private final String hash;
    private final LocalDateTime timestamp;
    private final LocalDateTime signeringstid;

    public SignaturTicket(String id, Status status, String intygsId, long version, LocalDateTime signeringstid, String hash, LocalDateTime timestamp) {
        this.id = id;
        this.status = status;
        this.intygsId = intygsId;
        this.version = version;
        this.hash = hash;
        this.timestamp = timestamp;
        this.signeringstid = signeringstid;
    }

    public String getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public String getIntygsId() {
        return intygsId;
    }

    public long getVersion() {
        return version;
    }

    public LocalDateTime getSigneringstid() {
        return signeringstid;
    }

    public String getHash() {
        return hash;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public SignaturTicket withStatus(Status status) {
        return new SignaturTicket(id, status, intygsId, version, signeringstid, hash, new LocalDateTime());
    }

    @Override
    public String toString() {
        return "SignatureTicket [ id:" + id + " intyg:" + intygsId + " status: " + status + " ]";
    }

}
