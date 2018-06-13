package se.inera.intyg.webcert.web.web.controller.api.dto;

import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;

public class SignaturStateDTO {
    private String ticketId;
    private String intygsId;
    private long version;
    private SignaturStatus status;
    private String intygDigest;
    private String signableDigest;

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getIntygsId() {
        return intygsId;
    }

    public void setIntygsId(String intygsId) {
        this.intygsId = intygsId;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public SignaturStatus getStatus() {
        return status;
    }

    public void setStatus(SignaturStatus status) {
        this.status = status;
    }

    public String getIntygDigest() {
        return intygDigest;
    }

    public void setIntygDigest(String intygDigest) {
        this.intygDigest = intygDigest;
    }

    public String getSignableDigest() {
        return signableDigest;
    }

    public void setSignableDigest(String signableDigest) {
        this.signableDigest = signableDigest;
    }

    public static final class SignaturStateDTOBuilder {
        private String ticketId;
        private String intygsId;
        private long version;
        private SignaturStatus status;
        private String intygDigest;
        private String signableDigest;

        private SignaturStateDTOBuilder() {
        }

        public static SignaturStateDTOBuilder aSignaturStateDTO() {
            return new SignaturStateDTOBuilder();
        }

        public SignaturStateDTOBuilder withTicketId(String ticketId) {
            this.ticketId = ticketId;
            return this;
        }

        public SignaturStateDTOBuilder withIntygsId(String intygsId) {
            this.intygsId = intygsId;
            return this;
        }

        public SignaturStateDTOBuilder withVersion(long version) {
            this.version = version;
            return this;
        }

        public SignaturStateDTOBuilder withStatus(SignaturStatus status) {
            this.status = status;
            return this;
        }

        public SignaturStateDTOBuilder withIntygDigest(String intygDigest) {
            this.intygDigest = intygDigest;
            return this;
        }

        public SignaturStateDTOBuilder withSignableDigest(String signableDigest) {
            this.signableDigest = signableDigest;
            return this;
        }

        public SignaturStateDTO build() {
            SignaturStateDTO signaturStateDTO = new SignaturStateDTO();
            signaturStateDTO.setTicketId(ticketId);
            signaturStateDTO.setIntygsId(intygsId);
            signaturStateDTO.setVersion(version);
            signaturStateDTO.setStatus(status);
            signaturStateDTO.setIntygDigest(intygDigest);
            signaturStateDTO.setSignableDigest(signableDigest);
            return signaturStateDTO;
        }
    }
}
