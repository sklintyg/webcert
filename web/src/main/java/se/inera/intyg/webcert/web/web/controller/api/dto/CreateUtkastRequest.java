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

package se.inera.intyg.webcert.web.web.controller.api.dto;

import org.apache.commons.lang.StringUtils;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

public class CreateUtkastRequest {

    private static final int NAME_MAX_LENGTH = 255;

    private String intygType;

    private Personnummer patientPersonnummer;

    private String patientFornamn;

    private String patientMellannamn;

    private String patientEfternamn;

    private String patientPostadress;

    private String patientPostnummer;

    private String patientPostort;

    public CreateUtkastRequest() {

    }

    public boolean isValid() {

        if (StringUtils.isBlank(intygType)) {
            return false;
        }

        if (patientPersonnummer == null || StringUtils.isBlank(patientPersonnummer.getPersonnummer())) {
            return false;
        }

        if (StringUtils.isBlank(patientFornamn) || StringUtils.isBlank(patientEfternamn)) {
            return false;
        }

        return patientFornamn.length() <= NAME_MAX_LENGTH && patientEfternamn.length() <= NAME_MAX_LENGTH;
    }

    public String getIntygType() {
        return intygType;
    }

    public void setIntygType(String intygType) {
        this.intygType = intygType;
    }

    public Personnummer getPatientPersonnummer() {
        return patientPersonnummer;
    }

    public void setPatientPersonnummer(Personnummer patientPersonnummer) {
        this.patientPersonnummer = patientPersonnummer;
    }

    public String getPatientFornamn() {
        return patientFornamn;
    }

    public void setPatientFornamn(String patientFornamn) {
        this.patientFornamn = patientFornamn;
    }

    public String getPatientMellannamn() {
        return patientMellannamn;
    }

    public void setPatientMellannamn(String patientMellannamn) {
        this.patientMellannamn = patientMellannamn;
    }

    public String getPatientEfternamn() {
        return patientEfternamn;
    }

    public void setPatientEfternamn(String patientEfternamn) {
        this.patientEfternamn = patientEfternamn;
    }

    public String getPatientPostadress() {
        return patientPostadress;
    }

    public void setPatientPostadress(String patientPostadress) {
        this.patientPostadress = patientPostadress;
    }

    public String getPatientPostnummer() {
        return patientPostnummer;
    }

    public void setPatientPostnummer(String patientPostnummer) {
        this.patientPostnummer = patientPostnummer;
    }

    public String getPatientPostort() {
        return patientPostort;
    }

    public void setPatientPostort(String patientPostort) {
        this.patientPostort = patientPostort;
    }

}
