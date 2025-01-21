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
package se.inera.intyg.webcert.persistence.fmb.model.dto;

public class MaximalSjukskrivningstidDagar {

    private String icd10Kod;
    private Integer maximalSjukrivningstidDagar;
    private String maximalSjukrivningstidSourceValue;
    private String maximalSjukrivningstidSourceUnit;

    public MaximalSjukskrivningstidDagar(final String icd10Kod, final int maximalSjukrivningstidDagar,
        final String maximalSjukrivningstidSourceValue, final String maximalSjukrivningstidSourceUnit) {
        this.icd10Kod = icd10Kod;
        this.maximalSjukrivningstidDagar = maximalSjukrivningstidDagar;
        this.maximalSjukrivningstidSourceValue = maximalSjukrivningstidSourceValue;
        this.maximalSjukrivningstidSourceUnit = maximalSjukrivningstidSourceUnit;
    }

    public static MaximalSjukskrivningstidDagar of(final String icd10Kod, final Integer maximalSjukrivningstidDagar,
        final String maximalSjukrivningstidSourceValue, final String maximalSjukrivningstidSourceUnit) {
        return new MaximalSjukskrivningstidDagar(icd10Kod, maximalSjukrivningstidDagar, maximalSjukrivningstidSourceValue,
            maximalSjukrivningstidSourceUnit);
    }

    public String getIcd10Kod() {
        return icd10Kod;
    }

    public int getMaximalSjukrivningstidDagar() {
        return maximalSjukrivningstidDagar;
    }

    public String getMaximalSjukrivningstidSourceValue() {
        return maximalSjukrivningstidSourceValue;
    }

    public String getMaximalSjukrivningstidSourceUnit() {
        return maximalSjukrivningstidSourceUnit;
    }
}
