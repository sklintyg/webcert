/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.list;

import java.time.LocalDateTime;

public class CertificateListItemDTO {
    private String certificateType;
    private String certificateTypeName;
    private String status;
    private LocalDateTime saved;
    private PatientListInfoDTO patientListInfo;
    private String savedBy;
    private boolean forwarded;
    private String certificateId;

    public CertificateListItemDTO(){}

    public CertificateListItemDTO(String certificateId, String certificateType, String status, String savedBy, LocalDateTime saved, boolean forwarded, PatientListInfoDTO patientListInfo, String certificateTypeName) {
        this.certificateId = certificateId;
        this.certificateType = certificateType;
        this.status = status;
        this.savedBy = savedBy;
        this.saved = saved;
        this.forwarded = forwarded;
        this.patientListInfo = patientListInfo;
        this.certificateTypeName = certificateTypeName;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSavedBy() {
        return savedBy;
    }

    public void setSavedBy(String savedBy) {
        this.savedBy = savedBy;
    }

    public LocalDateTime getSaved() {
        return saved;
    }

    public void setSaved(LocalDateTime saved) {
        this.saved = saved;
    }

    public boolean isForwarded() {
        return forwarded;
    }

    public void setForwarded(boolean forwarded) {
        this.forwarded = forwarded;
    }

    public PatientListInfoDTO getPatientListInfo() {
        return patientListInfo;
    }

    public void setPatientListInfo(PatientListInfoDTO patientListInfo) {
        this.patientListInfo = patientListInfo;
    }

    public String getCertificateTypeName() {
        return certificateTypeName;
    }

    public void setCertificateTypeName(String certificateTypeName) {
        this.certificateTypeName = certificateTypeName;
    }
}
