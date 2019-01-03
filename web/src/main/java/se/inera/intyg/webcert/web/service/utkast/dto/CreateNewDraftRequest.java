/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.UtkastStatus;

public class CreateNewDraftRequest {

    private String intygId;

    private String intygType;

    private String intygTypeVersion;

    private String referens;

    private UtkastStatus status;

    private Patient patient;

    private HoSPersonal hosPerson;

    public CreateNewDraftRequest() {
        // Needed for deserialization
    }

    public CreateNewDraftRequest(String intygId, String intygType, String intygTypeVersion, UtkastStatus status, HoSPersonal hosPerson,
            Patient patient) {
        this(intygId, intygType, intygTypeVersion, status, hosPerson, patient, null);
    }

    public CreateNewDraftRequest(String intygId, String intygType, String intygTypeVersion, UtkastStatus status, HoSPersonal hosPerson,
                                 Patient patient, String referens) {
        this.intygId = intygId;
        this.intygType = intygType;
        this.intygTypeVersion = intygTypeVersion;
        this.referens = referens;
        this.status = status;
        this.hosPerson = hosPerson;
        this.patient = patient;
    }
    public String getIntygId() {
        return intygId;
    }

    public void setIntygId(String intygId) {
        this.intygId = intygId;
    }

    public String getIntygType() {
        return intygType;
    }

    public void setIntygType(String intygType) {
        this.intygType = intygType;
    }

    public UtkastStatus getStatus() {
        return status;
    }

    public void setStatus(UtkastStatus status) {
        this.status = status;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public HoSPersonal getHosPerson() {
        return hosPerson;
    }

    public void setHosPerson(HoSPersonal hosPerson) {
        this.hosPerson = hosPerson;
    }

    public String getReferens() {
        return referens;
    }

    public void setReferens(String referens) {
        this.referens = referens;
    }

    public String getIntygTypeVersion() {
        return intygTypeVersion;
    }

    public void setIntygTypeVersion(String intygTypeVersion) {
        this.intygTypeVersion = intygTypeVersion;
    }
}
