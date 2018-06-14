package se.inera.intyg.webcert.web.web.controller.api.dto;

import java.nio.charset.Charset;
import java.util.Base64;

import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;

public class SignaturStateDTO {
    private String id;
    private String intygsId;
    private long version;
    private SignaturStatus status;
    private String hash;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * The hash MUST be the Base64 encoded representation of the canonicalized XML of the
     * <SignedInfo xmlns="http://www.w3.org/2000/09/xmldsig#"></SignedInfo> element on a single row, with all elements
     * properly closed. I.e. <MyElement algo="1337" /> == WRONG, <MyElement algo="1337"></MyElement> == CORRECT.
     */
    public String getHash() {
        return Base64.getEncoder().encodeToString(hash.getBytes(Charset.forName("UTF-8")));
    }

    public static final class SignaturStateDTOBuilder {
        private String id;
        private String intygsId;
        private long version;
        private SignaturStatus status;
        private String hash;

        private SignaturStateDTOBuilder() {
        }

        public static SignaturStateDTOBuilder aSignaturStateDTO() {
            return new SignaturStateDTOBuilder();
        }

        public SignaturStateDTOBuilder withId(String id) {
            this.id = id;
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

        public SignaturStateDTOBuilder withHash(String hash) {
            this.hash = hash;
            return this;
        }

        public SignaturStateDTO build() {
            SignaturStateDTO signaturStateDTO = new SignaturStateDTO();
            signaturStateDTO.setId(id);
            signaturStateDTO.setIntygsId(intygsId);
            signaturStateDTO.setVersion(version);
            signaturStateDTO.setStatus(status);
            signaturStateDTO.setHash(hash);
            return signaturStateDTO;
        }
    }
}
