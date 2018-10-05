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

public class IcfRequest {

    private String icd10Code1;
    private String icd10Code2;
    private String icd10Code3;

    public IcfRequest() {
    }

    private IcfRequest(final String icd10Code1, final String icd10Code2, final String icd10Code3) {
        this.icd10Code1 = icd10Code1;
        this.icd10Code2 = icd10Code2;
        this.icd10Code3 = icd10Code3;
    }

    public String getIcd10Code1() {
        return icd10Code1;
    }

    public void setIcd10Code1(final String icd10Code1) {
        this.icd10Code1 = icd10Code1;
    }

    public String getIcd10Code2() {
        return icd10Code2;
    }

    public void setIcd10Code2(final String icd10Code2) {
        this.icd10Code2 = icd10Code2;
    }

    public String getIcd10Code3() {
        return icd10Code3;
    }

    public void setIcd10Code3(final String icd10Code3) {
        this.icd10Code3 = icd10Code3;
    }

    public static IcfRequest of(final String icd10Code1, final String icd10Code2, final String icd10Code3) {
        return new IcfRequest(icd10Code1, icd10Code2, icd10Code3);
    }
}
