/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.Getter;
import lombok.Setter;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;

@Setter
@Getter
public class SignaturStateDTO {

    private String id;
    private String intygsId;
    private long version;
    private SignaturStatus status;
    private String hash;
    private SignaturTyp signaturTyp;
    private String autoStartToken;
    private String qrCode;

    // Signing Service
    private String actionUrl;
    private String signRequest;

    @JsonIgnore
    public SignaturTyp getSignaturTyp() {
        return signaturTyp;
    }

    /**
     * The hash MUST be the Base64 encoded representation of the canonicalized XML of the
     * <SignedInfo xmlns="http://www.w3.org/2000/09/xmldsig#"> element on a single row, with all elements
     * properly closed.
     */
    public String getHash() {
        if (hash == null) {
            return null;
        }
        if (signaturTyp == SignaturTyp.XMLDSIG) {
            return Base64.getEncoder().encodeToString(hash.getBytes(StandardCharsets.UTF_8));
        }
        return hash;
    }

    public static final class SignaturStateDTOBuilder {

        private String id;
        private String intygsId;
        private long version;
        private SignaturStatus status;
        private String hash;
        private SignaturTyp signaturTyp;
        private String autoStartToken;
        private String qrCode;

        // Signing Service
        private String actionUrl;
        private String signRequest;

        private SignaturStateDTOBuilder() {
        }

        public static SignaturStateDTOBuilder aSignaturStateDTO() {
            return new SignaturStateDTOBuilder();
        }

        public SignaturStateDTOBuilder withQrCode(String qrData) {
            this.qrCode = qrData;
            return this;
        }

        public SignaturStateDTOBuilder withAutoStartToken(String autoStartToken) {
            this.autoStartToken = autoStartToken;
            return this;
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

        public SignaturStateDTOBuilder withSignaturTyp(SignaturTyp signaturTyp) {
            this.signaturTyp = signaturTyp;
            return this;
        }

        public SignaturStateDTOBuilder withActionUrl(String actionUrl) {
            this.actionUrl = actionUrl;
            return this;
        }

        public SignaturStateDTOBuilder withSignRequest(String signRequest) {
            this.signRequest = signRequest;
            return this;
        }

        public SignaturStateDTO build() {
            SignaturStateDTO signaturStateDTO = new SignaturStateDTO();
            signaturStateDTO.setId(id);
            signaturStateDTO.setIntygsId(intygsId);
            signaturStateDTO.setVersion(version);
            signaturStateDTO.setStatus(status);
            signaturStateDTO.setHash(hash);
            signaturStateDTO.setSignaturTyp(signaturTyp);
            signaturStateDTO.setActionUrl(actionUrl);
            signaturStateDTO.setSignRequest(signRequest);
            signaturStateDTO.setAutoStartToken(autoStartToken);
            signaturStateDTO.setQrCode(qrCode);
            return signaturStateDTO;
        }
    }
}
