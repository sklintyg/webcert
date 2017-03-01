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
package se.inera.intyg.webcert.persistence.legacy.model;

import java.time.LocalDateTime;

import javax.persistence.*;

import org.hibernate.annotations.Type;

import se.inera.intyg.schemas.contract.Personnummer;

/**
 * Entity for a Medcert certificate migrated into Webcert.
 *
 * @author nikpet
 */
@Entity
@Table(name = "MIGRERADE_INTYG_FRAN_MEDCERT")
public class MigreratMedcertIntyg {

    @Id
    @Column(name = "INTYG_ID", nullable = false)
    private String intygsId;

    @Column(name = "ENHETS_ID", nullable = false)
    private String enhetsId;

    @Column(name = "INTYGS_TYP")
    private String intygsTyp;

    @Column(name = "URSPRUNG")
    private String ursprung;

    @Column(name = "PATIENT_NAMN", nullable = false)
    private String patientNamn;

    @Column(name = "PATIENT_SSN", nullable = false)
    private String patientPersonnummer;

    @Column(name = "SKAPAD_DATUM", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime skapad;

    @Column(name = "SKICKAD_DATUM")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime skickad;

    @Column(name = "MIGRERAD_DATUM", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime migrerad;

    @Column(name = "MIGRERAD_FRAN", nullable = false)
    private String migreradFran;

    @Column(name = "INTYGS_DATA")
    private byte[] intygsData;

    public MigreratMedcertIntyg() {
        // Default constructor for hibernate
    }

    @PrePersist
    void onPrePersist() {
        if (getMigrerad() == null) {
            setMigrerad(LocalDateTime.now());
        }
    }

    public String getIntygsId() {
        return intygsId;
    }

    public void setIntygsId(String intygsId) {
        this.intygsId = intygsId;
    }

    public String getEnhetsId() {
        return enhetsId;
    }

    public void setEnhetsId(String enhetsId) {
        this.enhetsId = enhetsId;
    }

    public String getIntygsTyp() {
        return intygsTyp;
    }

    public void setIntygsTyp(String intygsTyp) {
        this.intygsTyp = intygsTyp;
    }

    public String getUrsprung() {
        return ursprung;
    }

    public void setUrsprung(String ursprung) {
        this.ursprung = ursprung;
    }

    public String getPatientNamn() {
        return patientNamn;
    }

    public void setPatientNamn(String patientNamn) {
        this.patientNamn = patientNamn;
    }

    public Personnummer getPatientPersonnummer() {
        return new Personnummer(patientPersonnummer);
    }

    public void setPatientPersonnummer(Personnummer patientPersonnummer) {
        this.patientPersonnummer = patientPersonnummer.getPersonnummer();
    }

    public LocalDateTime getSkapad() {
        return skapad;
    }

    public void setSkapad(LocalDateTime skapad) {
        this.skapad = skapad;
    }

    public LocalDateTime getSkickad() {
        return skickad;
    }

    public void setSkickad(LocalDateTime skickad) {
        this.skickad = skickad;
    }

    public LocalDateTime getMigrerad() {
        return migrerad;
    }

    public void setMigrerad(LocalDateTime migrerad) {
        this.migrerad = migrerad;
    }

    public String getMigreradFran() {
        return migreradFran;
    }

    public void setMigreradFran(String migreradFran) {
        this.migreradFran = migreradFran;
    }

    public byte[] getIntygsData() {
        return intygsData;
    }

    public void setIntygsData(byte[] intygsData) {
        this.intygsData = intygsData;
    }

}
