/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import com.google.common.base.Strings;

import se.inera.intyg.common.support.validate.SamordningsnummerValidator;
import se.inera.intyg.schemas.contract.Personnummer;

public class CopyIntygRequest {

    private Personnummer patientPersonnummer;

    private Personnummer nyttPatientPersonnummer;

    private String fornamn;
    private String efternamn;
    private String mellannamn;
    private String postadress;
    private String postnummer;
    private String postort;
    private boolean coherentJournaling = false;

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

    public boolean isCoherentJournaling() {
        return coherentJournaling;
    }

    public void setCoherentJournaling(boolean coherentJournaling) {
        this.coherentJournaling = coherentJournaling;
    }

    public boolean containsNewValidPatientPersonId() {
        return (nyttPatientPersonnummer != null && (Personnummer.createValidatedPersonnummerWithDash(nyttPatientPersonnummer).isPresent()
                || SamordningsnummerValidator.isSamordningsNummer(nyttPatientPersonnummer)));
    }

    public boolean isValid() {
        return patientPersonnummer != null && !Strings.nullToEmpty(patientPersonnummer.getPersonnummer()).trim().isEmpty();
    }

    public String getFornamn() {
        return fornamn;
    }

    public void setFornamn(String fornamn) {
        this.fornamn = fornamn;
    }

    public String getEfternamn() {
        return efternamn;
    }

    public void setEfternamn(String efternamn) {
        this.efternamn = efternamn;
    }

    public String getMellannamn() {
        return mellannamn;
    }

    public void setMellannamn(String mellannamn) {
        this.mellannamn = mellannamn;
    }

    public String getPostadress() {
        return postadress;
    }

    public void setPostadress(String postadress) {
        this.postadress = postadress;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public void setPostnummer(String postnummer) {
        this.postnummer = postnummer;
    }

    public String getPostort() {
        return postort;
    }

    public void setPostort(String postort) {
        this.postort = postort;
    }
}
