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
package se.inera.intyg.webcert.web.service.utkast.dto;

import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * @author Magnus Ekstrand on 2019-08-27.
 */
public final class UtkastCandidateMetaData {

    private String intygId;
    private String intygType;
    private String intygTypeVersion;
    private String signedByHsaId;
    private String enhetHsaId;
    private String enhetName;

    private LocalDateTime intygCreated;
    private Boolean sameVardenhet;


    @SuppressWarnings("CheckStyle")
    private UtkastCandidateMetaData(String intygId, String intygType, String intygTypeVersion,
        String signedByHsaId, String enhetHsaId, String enhetName, LocalDateTime intygCreated,
        Boolean sameVardenhet) {
        this.intygId = intygId;
        this.intygType = intygType;
        this.intygTypeVersion = intygTypeVersion;
        this.signedByHsaId = signedByHsaId;
        this.enhetHsaId = enhetHsaId;
        this.enhetName = enhetName;
        this.intygCreated = intygCreated;
        this.sameVardenhet = sameVardenhet;
    }

    public String getIntygId() {
        return intygId;
    }

    public String getIntygType() {
        return intygType;
    }

    public String getIntygTypeVersion() {
        return intygTypeVersion;
    }

    public String getSignedByHsaId() {
        return signedByHsaId;
    }

    public String getEnhetHsaId() {
        return enhetHsaId;
    }

    public String getEnhetName() {
        return enhetName;
    }

    public LocalDateTime getIntygCreated() {
        return intygCreated;
    }

    public Boolean getSameVardenhet() {
        return sameVardenhet;
    }

    public static class Builder {

        public String intygId;
        public String intygType;
        public String intygTypeVersion;
        public String signedByHsaId;
        public String enhetHsaId;
        public String enhetName;
        public LocalDateTime intygCreated;
        public Boolean sameVardenhet;

        public Builder with(
            Consumer<Builder> builderFunction) {
            builderFunction.accept(this);
            return this;
        }

        public UtkastCandidateMetaData create() {
            return new UtkastCandidateMetaData(intygId, intygType, intygTypeVersion,
                signedByHsaId, enhetHsaId, enhetName, intygCreated, sameVardenhet);
        }
    }

}
