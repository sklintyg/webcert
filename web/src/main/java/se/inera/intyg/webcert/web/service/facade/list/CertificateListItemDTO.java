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

import se.inera.intyg.common.support.facade.model.PersonId;

import java.time.LocalDateTime;

public class CertificateListItemDTO {
    private String certificateId;
    private PersonId patientId;
    private String certificateType;
    private CertificateStatusDTO status;
    private String savedBy;
    private LocalDateTime saved;
    private boolean forwarded;
    private PatientStatusesDTO patientStatuses;
    private String certificateTypeName;

    public CertificateListItemDTO(){}

    public CertificateListItemDTO(String certificateId, PersonId patientId, String certificateType, CertificateStatusDTO status, String savedBy, LocalDateTime saved, boolean forwarded, PatientStatusesDTO patientStatuses, String certificateTypeName) {
        this.certificateId = certificateId;
        this.patientId = patientId;
        this.certificateType = certificateType;
        this.status = status;
        this.savedBy = savedBy;
        this.saved = saved;
        this.forwarded = forwarded;
        this.patientStatuses = patientStatuses;
        this.certificateTypeName = certificateTypeName;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public PersonId getPatientId() {
        return patientId;
    }

    public void setPatientId(PersonId patientId) {
        this.patientId = patientId;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public CertificateStatusDTO getStatus() {
        return status;
    }

    public void setStatus(CertificateStatusDTO status) {
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

    public PatientStatusesDTO getPatientStatuses() {
        return patientStatuses;
    }

    public void setPatientStatuses(PatientStatusesDTO patientStatuses) {
        this.patientStatuses = patientStatuses;
    }

    public String getCertificateTypeName() {
        return certificateTypeName;
    }

    public void setCertificateTypeName(String certificateTypeName) {
        this.certificateTypeName = certificateTypeName;
    }
}
