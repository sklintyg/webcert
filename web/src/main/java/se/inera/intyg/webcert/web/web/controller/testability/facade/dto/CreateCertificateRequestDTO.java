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
package se.inera.intyg.webcert.web.web.controller.testability.facade.dto;

import java.util.Map;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValue;

public class CreateCertificateRequestDTO {

    private String certificateType;
    private String certificateTypeVersion;
    private String patientId;
    private String personId;
    private String unitId;
    private boolean isSent;
    private CertificateStatus status;
    private CreateCertificateFillType fillType;
    private Map<String, CertificateDataValue> values;

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public String getCertificateTypeVersion() {
        return certificateTypeVersion;
    }

    public void setCertificateTypeVersion(String certificateTypeVersion) {
        this.certificateTypeVersion = certificateTypeVersion;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public CertificateStatus getStatus() {
        return status;
    }

    public void setStatus(CertificateStatus status) {
        this.status = status;
    }

    public CreateCertificateFillType getFillType() {
        return fillType;
    }

    public void setFillType(CreateCertificateFillType fillType) {
        this.fillType = fillType;
    }

    public Map<String, CertificateDataValue> getValues() {
        return values;
    }

    public void setValues(Map<String, CertificateDataValue> values) {
        this.values = values;
    }
}
