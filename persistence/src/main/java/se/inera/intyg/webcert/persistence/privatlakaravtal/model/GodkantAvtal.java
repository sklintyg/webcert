/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.privatlakaravtal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Created by eriklupander on 2015-08-05.
 */
@Table(name = "GODKANT_AVTAL_PRIVATLAKARE")
@Entity
public class GodkantAvtal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internReferens;

    @Column(name = "AVTAL_VERSION")
    private Integer avtalVersion;

    @Column(name = "HSA_ID")
    private String hsaId;

    @Column(name = "GODKAND_DATUM")
    private LocalDateTime godkandDatum;

    public Long getInternReferens() {
        return internReferens;
    }

    public void setInternReferens(Long internReferens) {
        this.internReferens = internReferens;
    }

    public Integer getAvtalVersion() {
        return avtalVersion;
    }

    public void setAvtalVersion(Integer avtalVersion) {
        this.avtalVersion = avtalVersion;
    }

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public LocalDateTime getGodkandDatum() {
        return godkandDatum;
    }

    public void setGodkandDatum(LocalDateTime goodkandDatum) {
        this.godkandDatum = goodkandDatum;
    }
}
