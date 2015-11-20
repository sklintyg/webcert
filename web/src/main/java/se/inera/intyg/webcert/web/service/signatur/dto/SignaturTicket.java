package se.inera.intyg.webcert.web.service.signatur.dto;

import org.joda.time.LocalDateTime;

public class SignaturTicket {

    /**
     * NetID/Siths uses BEARBETAR, SIGNERAD and OKAND. BEARBETAR from start to the signing either succeeds (SIGNERAD)
     * or fails (OKAND)
     *
     * BankID / Mobil BankID uses BEARBETAR from start until BankID application (mobile or not) has established connection
     * to the BankID server. Then the state is changed to VANTA_SIGN. If no connection between application and bank id
     * server were established in 6 GRP collect requests (according to spec, 18 seconds), the NO_CLIENT state is set.
     * SIGNERAD / OKAND is used similar to NetID after this.
     *
     * TODO We should align these states across all signing mechanisms. The GUI layer now needs to have different code paths
     * TODO for handling these state changes.
     */
    public enum Status {
        BEARBETAR, VANTA_SIGN, SIGNERAD, NO_CLIENT, OKAND
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
