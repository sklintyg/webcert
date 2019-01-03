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
package se.inera.intyg.webcert.web.web.controller.api.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import se.inera.intyg.schemas.contract.Personnummer;

public final class MaximalSjukskrivningstidRequest {

    private Icd10KoderRequest icd10Koder;
    private Personnummer personnummer;
    private Integer foreslagenSjukskrivningstid;

    public MaximalSjukskrivningstidRequest() {
    }

    private MaximalSjukskrivningstidRequest(
            final Icd10KoderRequest icd10Koder,
            final Personnummer personnummer,
            final Integer foreslagenSjukskrivningstid) {
        this.icd10Koder = icd10Koder;
        this.personnummer = personnummer;
        this.foreslagenSjukskrivningstid = foreslagenSjukskrivningstid;
    }

    public Icd10KoderRequest getIcd10Koder() {
        return icd10Koder;
    }

    public void setIcd10Koder(final Icd10KoderRequest icd10Koder) {
        this.icd10Koder = icd10Koder;
    }

    public Personnummer getPersonnummer() {
        return personnummer;
    }

    public void setPersonnummer(final Personnummer personnummer) {
        this.personnummer = personnummer;
    }

    public Integer getForeslagenSjukskrivningstid() {
        return foreslagenSjukskrivningstid;
    }

    public void setForeslagenSjukskrivningstid(final Integer foreslagenSjukskrivningstid) {
        this.foreslagenSjukskrivningstid = foreslagenSjukskrivningstid;
    }

    public static MaximalSjukskrivningstidRequest of(
            final Icd10KoderRequest icd10Koder,
            final Personnummer personnummer,
            final Integer foreslagenSjukskrivningstid) {
        return new MaximalSjukskrivningstidRequest(icd10Koder, personnummer, foreslagenSjukskrivningstid);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final MaximalSjukskrivningstidRequest request = (MaximalSjukskrivningstidRequest) o;

        return new EqualsBuilder()
                .append(icd10Koder, request.icd10Koder)
                .append(personnummer, request.personnummer)
                .append(foreslagenSjukskrivningstid, request.foreslagenSjukskrivningstid)
                .isEquals();
    }

    @Override
    public int hashCode() {
        // CHECKSTYLE:OFF MagicNumber
        return new HashCodeBuilder(17, 37)
                .append(icd10Koder)
                .append(personnummer)
                .append(foreslagenSjukskrivningstid)
                .toHashCode();
        // CHECKSTYLE:ON MagicNumber
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("icd10Koder", icd10Koder)
                .append("personnummer", personnummer)
                .append("foreslagenSjukskrivningstid", foreslagenSjukskrivningstid)
                .toString();
    }
}
