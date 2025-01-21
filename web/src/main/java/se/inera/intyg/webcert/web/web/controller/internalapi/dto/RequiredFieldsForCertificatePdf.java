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

package se.inera.intyg.webcert.web.web.controller.internalapi.dto;

import java.util.List;
import java.util.Objects;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;

public class RequiredFieldsForCertificatePdf {

    private String certificateTypeVersion;
    private String certificateType;
    private String internalJsonModel;
    private List<Status> statuses;
    private UtkastStatus status;

    public static RequiredFieldsForCertificatePdf create(String certificateTypeVersion, String certificateType, String internalJsonModel,
        List<Status> statuses, UtkastStatus status) {
        final var requiredFieldsForCertificatePdf = new RequiredFieldsForCertificatePdf();
        requiredFieldsForCertificatePdf.setCertificateTypeVersion(certificateTypeVersion);
        requiredFieldsForCertificatePdf.setCertificateType(certificateType);
        requiredFieldsForCertificatePdf.setInternalJsonModel(internalJsonModel);
        requiredFieldsForCertificatePdf.setStatuses(statuses);
        requiredFieldsForCertificatePdf.setStatus(status);
        return requiredFieldsForCertificatePdf;
    }

    public String getCertificateTypeVersion() {
        return certificateTypeVersion;
    }

    public void setCertificateTypeVersion(String certificateTypeVersion) {
        this.certificateTypeVersion = certificateTypeVersion;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public String getInternalJsonModel() {
        return internalJsonModel;
    }

    public void setInternalJsonModel(String internalJsonModel) {
        this.internalJsonModel = internalJsonModel;
    }

    public List<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<Status> statuses) {
        this.statuses = statuses;
    }

    public UtkastStatus getStatus() {
        return status;
    }

    public void setStatus(UtkastStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RequiredFieldsForCertificatePdf that = (RequiredFieldsForCertificatePdf) o;
        return Objects.equals(certificateTypeVersion, that.certificateTypeVersion) && Objects.equals(certificateType,
            that.certificateType) && Objects.equals(internalJsonModel, that.internalJsonModel) && Objects.equals(statuses,
            that.statuses) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(certificateTypeVersion, certificateType, internalJsonModel, statuses, status);
    }
}
