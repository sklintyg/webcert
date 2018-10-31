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
package se.inera.intyg.webcert.web.web.controller.api;

import io.vavr.collection.HashSet;
import java.util.Objects;

public class IcfRequest {

    private String icd10Kod1;
    private String icd10Kod2;
    private String icd10Kod3;

    public IcfRequest() {
    }

    private IcfRequest(final String icd10Kod1, final String icd10Kod2, final String icd10Kod3) {
        this.icd10Kod1 = icd10Kod1;
        this.icd10Kod2 = icd10Kod2;
        this.icd10Kod3 = icd10Kod3;
    }

    public String getIcd10Kod1() {
        return icd10Kod1;
    }

    public void setIcd10Kod1(final String icd10Kod1) {
        this.icd10Kod1 = icd10Kod1;
    }

    public String getIcd10Kod2() {
        return icd10Kod2;
    }

    public void setIcd10Kod2(final String icd10Kod2) {
        this.icd10Kod2 = icd10Kod2;
    }

    public String getIcd10Kod3() {
        return icd10Kod3;
    }

    public void setIcd10Kod3(final String icd10Kod3) {
        this.icd10Kod3 = icd10Kod3;
    }

    public HashSet<String> getIcd10Codes() {
        return HashSet.of(icd10Kod1, icd10Kod2, icd10Kod3)
                .filter(Objects::nonNull);
    }

    public static IcfRequest of(final String icd10Code1, final String icd10Code2, final String icd10Code3) {
        return new IcfRequest(icd10Code1, icd10Code2, icd10Code3);
    }
}
