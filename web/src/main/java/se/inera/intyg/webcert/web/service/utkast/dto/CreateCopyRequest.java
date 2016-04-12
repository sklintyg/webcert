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

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.web.service.dto.HoSPerson;
import se.inera.intyg.webcert.web.service.dto.Vardenhet;

public abstract class CreateCopyRequest {
    private String originalIntygId;

    private String typ;

    private Personnummer patientPersonnummer;

    private Personnummer nyttPatientPersonnummer;

    private HoSPerson hosPerson;

    private Vardenhet vardenhet;

    private boolean djupintegrerad = false;

    public CreateCopyRequest(String orginalIntygsId, String intygsTyp, Personnummer patientPersonnummer, HoSPerson hosPerson, Vardenhet vardenhet) {
        this.originalIntygId = orginalIntygsId;
        this.typ = intygsTyp;
        this.patientPersonnummer = patientPersonnummer;
        this.hosPerson = hosPerson;
        this.vardenhet = vardenhet;
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

    public Personnummer getPatientPersonnummer() {
        return patientPersonnummer;
    }

    public void setPatientPersonnummer(Personnummer patientPersonnummer) {
        this.patientPersonnummer = patientPersonnummer;
    }

    public Personnummer getNyttPatientPersonnummer() {
        return nyttPatientPersonnummer;
    }

    public void setNyttPatientPersonnummer(Personnummer nyttPatientPersonnummer) {
        this.nyttPatientPersonnummer = nyttPatientPersonnummer;
    }

    public HoSPerson getHosPerson() {
        return hosPerson;
    }

    public void setHosPerson(HoSPerson hosPerson) {
        this.hosPerson = hosPerson;
    }

    public Vardenhet getVardenhet() {
        return vardenhet;
    }

    public void setVardenhet(Vardenhet vardenhet) {
        this.vardenhet = vardenhet;
    }

    public boolean isDjupintegrerad() {
        return djupintegrerad;
    }

    public void setDjupintegrerad(boolean djupintegrerad) {
        this.djupintegrerad = djupintegrerad;
    }
}
