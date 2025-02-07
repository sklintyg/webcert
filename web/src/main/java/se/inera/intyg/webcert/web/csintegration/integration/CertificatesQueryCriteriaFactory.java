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
package se.inera.intyg.webcert.web.csintegration.integration;

import java.util.List;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificatesQueryCriteriaDTO;
import se.inera.intyg.webcert.web.csintegration.patient.PersonIdDTO;
import se.inera.intyg.webcert.web.csintegration.patient.PersonIdType;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.facade.list.filter.ListFilterHelper;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;

@Component
public class CertificatesQueryCriteriaFactory {

    public CertificatesQueryCriteriaDTO create(ListFilter filter) {
        final var from = ListFilterHelper.getSavedFrom(filter);
        final var to = ListFilterHelper.getSavedTo(filter);
        final var status = ListFilterHelper.getDraftStatus(filter);
        final var patientId = ListFilterHelper.getPatientId(filter);
        final var staffId = ListFilterHelper.getSavedBy(filter);

        return CertificatesQueryCriteriaDTO.builder()
            .from(from)
            .to(to)
            .statuses(statuses(status))
            .personId(personId(patientId))
            .issuedByStaffId(issuedByStaffId(staffId))
            .validForSign(validForSign(status))
            .build();
    }

    public CertificatesQueryCriteriaDTO create(QueryIntygParameter filter) {
        return CertificatesQueryCriteriaDTO.builder()
            .from(filter.getSignedFrom())
            .to(filter.getSignedTo())
            .statuses(List.of(CertificateStatus.SIGNED))
            .personId(personId(filter.getPatientId()))
            .issuedByStaffId(issuedByStaffId(filter.getHsaId()))
            .build();
    }

    @javax.annotation.Nullable
    private Boolean validForSign(List<UtkastStatus> status) {
        if (status.contains(UtkastStatus.DRAFT_INCOMPLETE) && status.contains(UtkastStatus.DRAFT_COMPLETE)) {
            return null;
        }

        if (status.contains(UtkastStatus.DRAFT_COMPLETE)) {
            return Boolean.TRUE;
        }

        if (status.contains(UtkastStatus.DRAFT_INCOMPLETE)) {
            return Boolean.FALSE;
        }

        return null;
    }

    private static List<CertificateStatus> statuses(List<UtkastStatus> status) {
        return status.contains(UtkastStatus.DRAFT_INCOMPLETE) || status.contains(UtkastStatus.DRAFT_COMPLETE)
            ? List.of(CertificateStatus.UNSIGNED)
            : List.of(CertificateStatus.LOCKED);
    }

    private static PersonIdDTO personId(String patientId) {
        return patientId == null || patientId.isBlank() ? null
            : PersonIdDTO.builder()
                .id(patientId)
                .type(PersonIdType.PERSONAL_IDENTITY_NUMBER)
                .build();
    }

    private static String issuedByStaffId(String staffId) {
        return staffId == null || staffId.isBlank() ? null : staffId;
    }
}
