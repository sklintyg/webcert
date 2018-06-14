package se.inera.intyg.webcert.web.service.underskrift.model;

import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.infra.xmldsig.model.IntygSignature;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SignaturBiljett implements Serializable {
    private String ticketId;
    private String intygsId;
    private long version;
    private SignaturStatus status;
    private IntygSignature intygSignature;
    private LocalDateTime skapad;
    private SignaturTyp signaturTyp;
    private String hash;

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

    public IntygSignature getIntygSignature() {
        return intygSignature;
    }

    public void setIntygSignature(IntygSignature intygSignature) {
        this.intygSignature = intygSignature;
    }

    public LocalDateTime getSkapad() {
        return skapad;
    }

    public void setSkapad(LocalDateTime skapad) {
        this.skapad = skapad;
    }

    public SignaturTyp getSignaturTyp() {
        return signaturTyp;
    }

    public void setSignaturTyp(SignaturTyp signaturTyp) {
        this.signaturTyp = signaturTyp;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }


    public static final class SignaturBiljettBuilder {
        private String ticketId;
        private String intygsId;
        private long version;
        private SignaturStatus status;
        private IntygSignature intygSignature;
        private LocalDateTime skapad;
        private SignaturTyp signaturTyp;
        private String hash;

        private SignaturBiljettBuilder() {
        }

        public static SignaturBiljettBuilder aSignaturBiljett() {
            return new SignaturBiljettBuilder();
        }

        public SignaturBiljettBuilder withTicketId(String ticketId) {
            this.ticketId = ticketId;
            return this;
        }

        public SignaturBiljettBuilder withIntygsId(String intygsId) {
            this.intygsId = intygsId;
            return this;
        }

        public SignaturBiljettBuilder withVersion(long version) {
            this.version = version;
            return this;
        }

        public SignaturBiljettBuilder withStatus(SignaturStatus status) {
            this.status = status;
            return this;
        }

        public SignaturBiljettBuilder withIntygSignature(IntygSignature intygSignature) {
            this.intygSignature = intygSignature;
            return this;
        }

        public SignaturBiljettBuilder withSkapad(LocalDateTime skapad) {
            this.skapad = skapad;
            return this;
        }

        public SignaturBiljettBuilder withSignaturTyp(SignaturTyp signaturTyp) {
            this.signaturTyp = signaturTyp;
            return this;
        }

        public SignaturBiljettBuilder withHash(String hash) {
            this.hash = hash;
            return this;
        }

        public SignaturBiljett build() {
            SignaturBiljett signaturBiljett = new SignaturBiljett();
            signaturBiljett.setTicketId(ticketId);
            signaturBiljett.setIntygsId(intygsId);
            signaturBiljett.setVersion(version);
            signaturBiljett.setStatus(status);
            signaturBiljett.setIntygSignature(intygSignature);
            signaturBiljett.setSkapad(skapad);
            signaturBiljett.setSignaturTyp(signaturTyp);
            signaturBiljett.setHash(hash);
            return signaturBiljett;
        }
    }
}
