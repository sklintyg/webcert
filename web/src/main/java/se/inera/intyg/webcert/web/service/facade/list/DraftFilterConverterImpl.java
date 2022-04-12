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
        final var user = webCertUserService.getUser();
        final var selectedUnitHsaId = user.getValdVardenhet().getId();
        final var convertedFilter = new UtkastFilter(selectedUnitHsaId);

        ListFilterDateRangeValue saved = (ListFilterDateRangeValue) filter.getValue("SAVED");
        ListFilterSelectValue savedBy = (ListFilterSelectValue) filter.getValue("SAVED_BY");
        ListFilterPersonIdValue patientId = (ListFilterPersonIdValue) filter.getValue("PATIENT_ID");
        ListFilterSelectValue forwarded = (ListFilterSelectValue) filter.getValue("FORWARDED");
        ListFilterTextValue orderBy = (ListFilterTextValue) filter.getValue("ORDER_BY");
        ListFilterBooleanValue ascending = (ListFilterBooleanValue) filter.getValue("ASCENDING");
        ListFilterSelectValue status = (ListFilterSelectValue) filter.getValue("STATUS");


        convertedFilter.setSavedFrom(saved != null ? saved.getFrom() : null);
        convertedFilter.setSavedTo(saved != null ? saved.getTo() : null);
        convertedFilter.setSavedByHsaId(savedBy != null && !savedBy.getValue().equals("SHOW_ALL") ? savedBy.getValue() : "");
        convertedFilter.setPatientId(patientId != null ? patientId.getValue() : "");
        convertedFilter.setNotified(forwarded != null ? getForwardedValue(forwarded.getValue()) : null);
        convertedFilter.setOrderBy(orderBy == null ? "" : orderBy.getValue());
        convertedFilter.setOrderAscending(ascending != null && ascending.getValue());
        convertedFilter.setStatusList(getStatusListFromFilter(status != null ? status.getValue() : ""));
        return convertedFilter;
    }

    private List<UtkastStatus> getStatusListFromFilter(String status) {
        final var showAll = Arrays.asList(UtkastStatus.DRAFT_COMPLETE, UtkastStatus.DRAFT_LOCKED, UtkastStatus.DRAFT_INCOMPLETE);

        if(status.equals("")) {
            return showAll;
        }

        final var convertedStatus = DraftStatus.valueOf(status);

        if (convertedStatus == DraftStatus.INCOMPLETE) {
            return List.of(UtkastStatus.DRAFT_INCOMPLETE);
        } else if (convertedStatus == DraftStatus.COMPLETE) {
            return List.of(UtkastStatus.DRAFT_COMPLETE);
        } else if (convertedStatus == DraftStatus.LOCKED) {
            return List.of(UtkastStatus.DRAFT_LOCKED);
        }
        return showAll;
    }

    private Boolean getForwardedValue(String value) {
        if(value.equals(ForwardedType.FORWARDED.toString())) {
            return true;
        } else if(value.equals(ForwardedType.NOT_FORWARDED.toString())) {
            return false;
        }
        return null;
    }
}
