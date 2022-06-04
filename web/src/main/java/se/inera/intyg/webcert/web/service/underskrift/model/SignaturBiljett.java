/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.service.underskrift.model;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.infra.xmldsig.model.IntygSignature;

public class SignaturBiljett implements Serializable {

    private String ticketId;
    private String intygsId;
    private long version;
    private SignaturStatus status;
    private IntygSignature intygSignature;
    private LocalDateTime skapad;
    private SignaturTyp signaturTyp;
    private String hash;
    private SignMethod signMethod;
    private boolean wc2ClientRequest;

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

    public SignMethod getSignMethod() {
        return signMethod;
    }

    public void setSignMethod(SignMethod signMethod) {
        this.signMethod = signMethod;
    }

    public boolean isWc2ClientRequest() {
        return wc2ClientRequest;
    }

    public void setWc2ClientRequest(boolean wc2ClientRequest) {
        this.wc2ClientRequest = wc2ClientRequest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SignaturBiljett)) {
            return false;
        }
        SignaturBiljett that = (SignaturBiljett) o;
        return version == that.version
            && Objects.equals(ticketId, that.ticketId)
            && Objects.equals(intygsId, that.intygsId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticketId, intygsId, version);
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
        private SignMethod signMethod;
        private boolean wc2ClientRequest;

        private SignaturBiljettBuilder(String ticketId, SignaturTyp signaturTyp, SignMethod signMethod) {
            this.ticketId = ticketId;
            this.signaturTyp = signaturTyp;
            this.signMethod = signMethod;
        }

        public static SignaturBiljettBuilder aSignaturBiljett(String ticketId, SignaturTyp signaturTyp, SignMethod signMethod) {
            return new SignaturBiljettBuilder(ticketId, signaturTyp, signMethod);
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

        public SignaturBiljettBuilder withHash(String hash) {
            this.hash = hash;
            return this;
        }

        public SignaturBiljettBuilder withSignMethod(SignMethod signMethod) {
            this.signMethod = signMethod;
            return this;
        }

        public SignaturBiljettBuilder withWc2ClientRequest(boolean wc2Client) {
            this.wc2ClientRequest = wc2Client;
            return this;
        }

        public SignaturBiljett build() {
            checkArgument(nonNull(ticketId));
            checkArgument(nonNull(signaturTyp));
            checkArgument(nonNull(signMethod));

            SignaturBiljett signaturBiljett = new SignaturBiljett();
            signaturBiljett.setTicketId(ticketId);
            signaturBiljett.setIntygsId(intygsId);
            signaturBiljett.setVersion(version);
            signaturBiljett.setStatus(status);
            signaturBiljett.setIntygSignature(intygSignature);
            signaturBiljett.setSkapad(skapad);
            signaturBiljett.setSignaturTyp(signaturTyp);
            signaturBiljett.setHash(hash);
            signaturBiljett.setSignMethod(signMethod);
            signaturBiljett.setWc2ClientRequest(wc2ClientRequest);
            return signaturBiljett;
        }
    }
}
