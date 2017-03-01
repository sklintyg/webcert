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
package se.inera.intyg.webcert.persistence.fragasvar.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;

import se.inera.intyg.schemas.contract.Personnummer;

@Embeddable
public class IntygsReferens {

    @Column(name = "INTYGS_ID")
    private String intygsId;

    @Column(name = "INTYGS_TYP")
    private String intygsTyp;

    @Column(name = "PATIENT_ID")
    private String patientId;

    @Column(name = "PATIENT_NAMN")
    private String patientNamn;

    @Column(name = "SIGNERINGS_DATUM")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime signeringsDatum;

    public IntygsReferens() {
        // default constructor for hibernate
    }

    public IntygsReferens(String intygsId, String intygsTyp, Personnummer patientId,
                          String patientName, LocalDateTime signeringsDatum) {
        this.intygsId = intygsId;
        this.intygsTyp = intygsTyp;
        this.patientId = patientId.getPersonnummer();
        this.patientNamn = patientName;
        this.signeringsDatum = signeringsDatum;
    }

    public String getIntygsId() {
        return intygsId;
    }

    public void setIntygsId(String intygsId) {
        this.intygsId = intygsId;
    }

    public String getIntygsTyp() {
        return intygsTyp;
    }

    public void setIntygsTyp(String intygsTyp) {
        this.intygsTyp = intygsTyp;
    }

    public Personnummer getPatientId() {
        return new Personnummer(patientId);
    }

    public void setPatientId(Personnummer patientId) {
        this.patientId = patientId.getPersonnummer();
    }

    public String getPatientNamn() {
        return patientNamn;
    }

    public void setPatientNamn(String patientNamn) {
        this.patientNamn = patientNamn;
    }

    public LocalDateTime getSigneringsDatum() {
        return signeringsDatum;
    }

    public void setSigneringsDatum(LocalDateTime signeringsDatum) {
        this.signeringsDatum = signeringsDatum;
    }
}
