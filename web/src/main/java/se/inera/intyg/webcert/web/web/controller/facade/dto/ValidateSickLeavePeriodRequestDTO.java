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
package se.inera.intyg.webcert.web.web.controller.facade.dto;

import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRangeList;

public class ValidateSickLeavePeriodRequestDTO {

    private String personId;
    private String[] icd10Codes;
    private CertificateDataValueDateRangeList dateRangeList;

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public void setIcd10Codes(String[] icd10Codes) {
        this.icd10Codes = icd10Codes;
    }

    public String[] getIcd10Codes() {
        return icd10Codes;
    }

    public String getIcd10Code(int index) {
        return icd10Codes.length - 1 < index ? "" : icd10Codes[index];
    }

    public void setDateRangeList(CertificateDataValueDateRangeList dateRangeList) {
        this.dateRangeList = dateRangeList;
    }

    public CertificateDataValueDateRangeList getDateRangeList() {
        return dateRangeList;
    }

}
