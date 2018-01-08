/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.utkast.model;

import org.hibernate.annotations.Type;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * Defines an ongoing signature operation. Started, but not yet ready to be commited to the INTYG table.
 */
@Entity
@Table(name = "PAGAENDE_SIGNERING")
public class PagaendeSignering {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "INTERN_REFERENS")
    private Long internReferens;

    @Column(name = "INTYG_ID")
    private String intygsId;

    @Column(name = "SIGNERINGS_DATUM")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime signeringsDatum;

    /**
     * HSA id of the person performing the signing.
     */
    @Column(name = "SIGNERAD_AV_HSA_ID")
    private String signeradAvHsaId;

    /**
     * HSA id of the person performing the signing.
     */
    @Column(name = "SIGNERAD_AV_NAMN")
    private String signeradAvNamn;

    /**
     * The certificate data being signed.
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "INTYG_DATA")
    private String intygData;

    public Long getInternReferens() {
        return internReferens;
    }

    public void setInternReferens(Long internReferens) {
        this.internReferens = internReferens;
    }

    public String getIntygsId() {
        return intygsId;
    }

    public void setIntygsId(String intygsId) {
        this.intygsId = intygsId;
    }

    public LocalDateTime getSigneringsDatum() {
        return signeringsDatum;
    }

    public void setSigneringsDatum(LocalDateTime signeringsDatum) {
        this.signeringsDatum = signeringsDatum;
    }

    public String getSigneradAvHsaId() {
        return signeradAvHsaId;
    }

    public void setSigneradAvHsaId(String signeradAvHsaId) {
        this.signeradAvHsaId = signeradAvHsaId;
    }

    public String getSigneradAvNamn() {
        return signeradAvNamn;
    }

    public void setSigneradAvNamn(String signeradAvNamn) {
        this.signeradAvNamn = signeradAvNamn;
    }

    public String getIntygData() {
        return intygData;
    }

    public void setIntygData(String intygData) {
        this.intygData = intygData;
    }
}
