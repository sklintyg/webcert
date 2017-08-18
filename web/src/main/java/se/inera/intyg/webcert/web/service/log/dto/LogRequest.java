/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.log.dto;

import se.inera.intyg.schemas.contract.Personnummer;

public class LogRequest {

    private String intygId;

    private Personnummer patientId;

    private String patientName;

    private String intygCareUnitId;
    private String intygCareUnitName;

    private String intygCareGiverId;
    private String intygCareGiverName;

    private String additionalInfo;

    public LogRequest() {
        super();
    }

    public String getIntygId() {
        return intygId;
    }

    public void setIntygId(String intygId) {
        this.intygId = intygId;
    }

    public Personnummer getPatientId() {
        return patientId;
    }

    public void setPatientId(Personnummer patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getIntygCareUnitId() {
        return intygCareUnitId;
    }

    public void setIntygCareUnitId(String intygCareUnitId) {
        this.intygCareUnitId = intygCareUnitId;
    }

    public String getIntygCareUnitName() {
        return intygCareUnitName;
    }

    public void setIntygCareUnitName(String intygCareUnitName) {
        this.intygCareUnitName = intygCareUnitName;
    }

    public String getIntygCareGiverId() {
        return intygCareGiverId;
    }

    public void setIntygCareGiverId(String intygCareGiverId) {
        this.intygCareGiverId = intygCareGiverId;
    }

    public String getIntygCareGiverName() {
        return intygCareGiverName;
    }

    public void setIntygCareGiverName(String intygCareGiverName) {
        this.intygCareGiverName = intygCareGiverName;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
