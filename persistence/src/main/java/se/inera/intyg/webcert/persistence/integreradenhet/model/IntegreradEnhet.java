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
package se.inera.intyg.webcert.persistence.integreradenhet.model;

import java.time.LocalDateTime;

import javax.persistence.*;

import org.hibernate.annotations.Type;

/**
 * Entity for a Vardenhet that uses to integrate with Webcert using journaling systems.
 *
 * @author nikpet
 */
@Entity
@Table(name = "INTEGRERADE_VARDENHETER")
public class IntegreradEnhet {

    @Id
    @Column(name = "ENHETS_ID")
    private String enhetsId;

    @Column(name = "ENHETS_NAMN")
    private String enhetsNamn;

    @Column(name = "VARDGIVAR_ID")
    private String vardgivarId;

    @Column(name = "VARDGIVAR_NAMN")
    private String vardgivarNamn;

    @Column(name = "SKAPAD_DATUM")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime skapadDatum;

    @Column(name = "SENASTE_KONTROLL_DATUM")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime senasteKontrollDatum;

    @Column(name = "SCHEMA_VERSION_1")
    private boolean schemaVersion1;

    @Column(name = "SCHEMA_VERSION_3")
    private boolean schemaVersion3;
    @PrePersist
    void onPrePersist() {
        if (skapadDatum == null) {
            skapadDatum = LocalDateTime.now();
        }
    }

    @PreUpdate
    void onPreUpdate() {
        senasteKontrollDatum = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "IntegreradEnhet [enhetsId=" + enhetsId + ", enhetsNamn=" + enhetsNamn + ", vardgivarId=" + vardgivarId + ", vardgivarNamn="
                + vardgivarNamn + ", skapadDatum=" + skapadDatum + ", senasteKontrollDatum=" + senasteKontrollDatum + ", version1="
                + isSchemaVersion1() + ", version2=" + isSchemaVersion3() + "]";
    }

    public String getEnhetsId() {
        return enhetsId;
    }

    public void setEnhetsId(String enhetsId) {
        this.enhetsId = enhetsId;
    }

    public String getEnhetsNamn() {
        return enhetsNamn;
    }

    public void setEnhetsNamn(String enhetsNamn) {
        this.enhetsNamn = enhetsNamn;
    }

    public String getVardgivarId() {
        return vardgivarId;
    }

    public void setVardgivarId(String vardgivarId) {
        this.vardgivarId = vardgivarId;
    }

    public String getVardgivarNamn() {
        return vardgivarNamn;
    }

    public void setVardgivarNamn(String vardgivarNamn) {
        this.vardgivarNamn = vardgivarNamn;
    }

    public LocalDateTime getSkapadDatum() {
        return skapadDatum;
    }

    public void setSkapadDatum(LocalDateTime skapadDatum) {
        this.skapadDatum = skapadDatum;
    }

    public LocalDateTime getSenasteKontrollDatum() {
        return senasteKontrollDatum;
    }

    public void setSenasteKontrollDatum(LocalDateTime senasteKontrollDatum) {
        this.senasteKontrollDatum = senasteKontrollDatum;
    }

    public boolean isSchemaVersion1() {
        return schemaVersion1;
    }

    public void setSchemaVersion1(boolean schemaVersion1) {
        this.schemaVersion1 = schemaVersion1;
    }

    public boolean isSchemaVersion3() {
        return schemaVersion3;
    }

    public void setSchemaVersion3(boolean schemaVersion3) {
        this.schemaVersion3 = schemaVersion3;
    }
}
