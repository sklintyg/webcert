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

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.riv.clinicalprocess.healthcond.certificate.v33.Forifyllnad;

@Getter
@Setter
public class CreateNewDraftRequest {

    private String intygId;

    private String intygType;

    private String intygTypeVersion;

    private String referens;

    private UtkastStatus status;

    private Patient patient;

    private HoSPersonal hosPerson;

    private Optional<Forifyllnad> forifyllnad = Optional.empty();

    private boolean testability = false;

    public CreateNewDraftRequest() {
        // Needed for deserialization
    }

    public CreateNewDraftRequest(String intygId, String intygType, String intygTypeVersion, UtkastStatus status, HoSPersonal hosPerson,
        Patient patient) {
        this(intygId, intygType, intygTypeVersion, status, hosPerson, patient, null, Optional.empty());
    }

    // CHECKSTYLE:OFF ParameterNumber
    public CreateNewDraftRequest(String intygId, String intygType, String intygTypeVersion, UtkastStatus status, HoSPersonal hosPerson,
        Patient patient, String referens, Optional<Forifyllnad> forifyllnad) {
        this.intygId = intygId;
        this.intygType = intygType;
        this.intygTypeVersion = intygTypeVersion;
        this.referens = referens;
        this.status = status;
        this.hosPerson = hosPerson;
        this.patient = patient;
        this.forifyllnad = forifyllnad;
    }
    // CHECKSTYLE:ON ParameterNumber
}
