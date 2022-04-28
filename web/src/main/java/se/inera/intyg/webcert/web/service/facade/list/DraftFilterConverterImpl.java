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

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastFilter;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.*;
import se.inera.intyg.webcert.web.service.facade.list.dto.*;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DraftFilterConverterImpl implements DraftFilterConverter {

    private final WebCertUserService webCertUserService;

    public DraftFilterConverterImpl(WebCertUserService webCertUserService) {
        this.webCertUserService = webCertUserService;
    }

    @Override
    public UtkastFilter convert(ListFilter filter) {
        return convertFilter(filter);
    }

    private UtkastFilter convertFilter(ListFilter filter) {
        final var convertedFilter = initializeFilter();

        convertedFilter.setSavedFrom(getSavedFrom(filter));
        convertedFilter.setSavedTo(getSavedTo(filter));
        convertedFilter.setSavedByHsaId(getSavedBy(filter));
        convertedFilter.setPatientId(getPatientId(filter));
        convertedFilter.setNotified(getForwarded(filter));
        convertedFilter.setOrderBy(getOrderBy(filter));
        convertedFilter.setOrderAscending(getAscending(filter));
        convertedFilter.setStatusList(getStatus(filter));
        return convertedFilter;
    }

    private UtkastFilter initializeFilter() {
        final var user = webCertUserService.getUser();
        final var selectedUnitHsaId = user.getValdVardenhet().getId();
        return new UtkastFilter(selectedUnitHsaId);
    }

    private LocalDateTime getSavedFrom(ListFilter filter) {
        ListFilterDateRangeValue saved = (ListFilterDateRangeValue) filter.getValue("SAVED");
        return saved != null && saved.getFrom() != null ? saved.getFrom() : null;
    }

    private LocalDateTime getSavedTo(ListFilter filter) {
        ListFilterDateRangeValue saved = (ListFilterDateRangeValue) filter.getValue("SAVED");
        return saved != null && saved.getTo() != null ? saved.getTo().plusDays(1) : null;
    }

    private String getSavedBy(ListFilter filter) {
        ListFilterSelectValue savedBy = (ListFilterSelectValue) filter.getValue("SAVED_BY");
        return savedBy != null && !savedBy.getValue().equals("SHOW_ALL") ? savedBy.getValue() : "";
    }

    private String getPatientId(ListFilter filter) {
        ListFilterPersonIdValue patientId = (ListFilterPersonIdValue) filter.getValue("PATIENT_ID");
        return patientId != null ? patientId.getValue() : "";
    }

    private Boolean getForwarded(ListFilter filter) {
        ListFilterSelectValue forwarded = (ListFilterSelectValue) filter.getValue("FORWARDED");
        return forwarded != null ? getForwardedValue(forwarded.getValue()) : null;
    }

    private String getOrderBy(ListFilter filter) {
        ListFilterTextValue orderBy = (ListFilterTextValue) filter.getValue("ORDER_BY");
        return orderBy == null ? "" : orderBy.getValue();
    }

    private boolean getAscending(ListFilter filter) {
        ListFilterBooleanValue ascending = (ListFilterBooleanValue) filter.getValue("ASCENDING");
        return ascending != null && ascending.getValue();
    }

    private List<UtkastStatus> getStatus(ListFilter filter) {
        ListFilterSelectValue status = (ListFilterSelectValue) filter.getValue("STATUS");
        return getStatusListFromFilter(status != null ? status.getValue() : "");
    }

    private List<UtkastStatus> getStatusListFromFilter(String status) {
        final var showAll = Arrays.asList(UtkastStatus.DRAFT_COMPLETE, UtkastStatus.DRAFT_LOCKED, UtkastStatus.DRAFT_INCOMPLETE);

        if (status.isBlank()) {
            return showAll;
        }

        final var convertedStatus = CertificateStatus.valueOf(status);

        if (convertedStatus == CertificateStatus.INCOMPLETE) {
            return List.of(UtkastStatus.DRAFT_INCOMPLETE);
        } else if (convertedStatus == CertificateStatus.COMPLETE) {
            return List.of(UtkastStatus.DRAFT_COMPLETE);
        } else if (convertedStatus == CertificateStatus.LOCKED) {
            return List.of(UtkastStatus.DRAFT_LOCKED);
        }
        return showAll;
    }

    private Boolean getForwardedValue(String value) {
        if (value.equals(ForwardedType.FORWARDED.toString())) {
            return true;
        } else if (value.equals(ForwardedType.NOT_FORWARDED.toString())) {
            return false;
        }
        return null;
    }
}
