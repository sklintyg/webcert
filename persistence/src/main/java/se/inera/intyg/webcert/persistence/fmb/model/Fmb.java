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

package se.inera.intyg.webcert.persistence.fmb.model;

import java.util.Locale;

import javax.persistence.*;

@Entity
@Table(name = "FMB")
public class Fmb {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ICD10")
    private String icd10;

    @Column(name = "TYP")
    @Enumerated(EnumType.STRING)
    private FmbType typ;

    @Column(name = "URSPRUNG")
    @Enumerated(EnumType.STRING)
    private FmbCallType ursprung;

    @Column(name = "TEXT")
    private String text;

    @Column(name = "SENAST_UPPDATERAD")
    private String lastUpdate;

    Fmb() {
        // default constructor for hibernate
    }

    public Fmb(String icd10, FmbType type, FmbCallType callType, String text, String lastUpdate) {
        this.icd10 = icd10 != null ? icd10.toUpperCase(Locale.ENGLISH) : null;
        this.typ = type;
        this.ursprung = callType;
        this.text = text;
        this.lastUpdate = lastUpdate != null ? lastUpdate : "unknown";
    }

    public Long getId() {
        return id;
    }

    public String getIcd10() {
        return icd10;
    }

    public FmbType getTyp() {
        return typ;
    }

    public FmbCallType getUrsprung() {
        return ursprung;
    }

    public String getText() {
        return text;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

}
