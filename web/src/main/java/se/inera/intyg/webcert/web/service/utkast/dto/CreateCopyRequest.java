/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import org.apache.commons.lang3.StringUtils;

import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

public abstract class CreateCopyRequest {
    private String originalIntygId;

    private String typ;

    private Personnummer nyttPatientPersonnummer;

    private Patient patient;

    private HoSPersonal hosPerson;

    private boolean djupintegrerad = false;

    public CreateCopyRequest(String orginalIntygsId, String intygsTyp, Patient patient, HoSPersonal hosPerson) {
        this.originalIntygId = orginalIntygsId;
        this.typ = intygsTyp;
        this.patient = patient;
        this.hosPerson = hosPerson;
    }

    public CreateCopyRequest() {

    }

    public boolean containsNyttPatientPersonnummer() {
        return nyttPatientPersonnummer != null && StringUtils.isNotBlank(nyttPatientPersonnummer.getPersonnummer());
    }

    public String getOriginalIntygId() {
        return originalIntygId;
    }

    public void setOriginalIntygId(String originalIntygId) {
        this.originalIntygId = originalIntygId;
    }

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public Personnummer getNyttPatientPersonnummer() {
        return nyttPatientPersonnummer;
    }

    public void setNyttPatientPersonnummer(Personnummer nyttPatientPersonnummer) {
        this.nyttPatientPersonnummer = nyttPatientPersonnummer;
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

    public boolean isDjupintegrerad() {
        return djupintegrerad;
    }

    public void setDjupintegrerad(boolean djupintegrerad) {
        this.djupintegrerad = djupintegrerad;
    }
}
