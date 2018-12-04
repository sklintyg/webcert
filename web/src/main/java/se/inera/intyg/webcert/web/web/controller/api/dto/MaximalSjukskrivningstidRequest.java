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
package se.inera.intyg.webcert.web.web.controller.api.dto;

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
}
