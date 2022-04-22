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
import se.inera.intyg.webcert.web.service.facade.list.config.dto.*;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;

import java.time.LocalDateTime;

@Service
public class CertificateFilterConverterImpl implements CertificateFilterConverter {

    @Override
    public QueryIntygParameter convert(ListFilter filter, String hsaId, String[] units) {
        return convertFilter(filter, hsaId, units);
    }

    private QueryIntygParameter convertFilter(ListFilter filter, String hsaId, String[] units) {
        final var convertedFilter = new QueryIntygParameter();

        convertedFilter.setHsaId(hsaId);
        convertedFilter.setUnitIds(units);
        convertedFilter.setSignedFrom(getSignedFrom(filter));
        convertedFilter.setSignedTo(getSignedTo(filter));
        convertedFilter.setPatientId(getPatientId(filter));
        convertedFilter.setOrderBy(getOrderBy(filter));
        convertedFilter.setOrderAscending(getAscending(filter));
        convertedFilter.setStartFrom(getStartFrom(filter));
        convertedFilter.setPageSize(getPageSize(filter));
        return convertedFilter;
    }

    private LocalDateTime getSignedFrom(ListFilter filter) {
        ListFilterDateRangeValue signed = (ListFilterDateRangeValue) filter.getValue("SIGNED");
        return signed != null ? signed.getFrom() : LocalDateTime.now().minusMonths(3);
    }

    private LocalDateTime getSignedTo(ListFilter filter) {
        ListFilterDateRangeValue signed = (ListFilterDateRangeValue) filter.getValue("SIGNED");
        return signed != null ? signed.getTo() : null;
    }

    private String getPatientId(ListFilter filter) {
        ListFilterPersonIdValue patientId = (ListFilterPersonIdValue) filter.getValue("PATIENT_ID");
        return patientId != null ? patientId.getValue() : "";
    }

    private String getOrderBy(ListFilter filter) {
        ListFilterTextValue orderBy = (ListFilterTextValue) filter.getValue("ORDER_BY");
        return orderBy == null ? "signedDate" : convertOrderBy(orderBy.getValue());
    }

    private boolean getAscending(ListFilter filter) {
        ListFilterBooleanValue ascending = (ListFilterBooleanValue) filter.getValue("ASCENDING");
        return ascending != null && ascending.getValue();
    }

    private int getStartFrom(ListFilter filter) {
        ListFilterNumberValue startFrom = (ListFilterNumberValue) filter.getValue("START_FROM");
        return startFrom == null ? 0 : startFrom.getValue();
    }

    private int getPageSize(ListFilter filter) {
        ListFilterNumberValue pageSize = (ListFilterNumberValue) filter.getValue("PAGESIZE");
        return pageSize == null ? 10 : pageSize.getValue();
    }

    private String convertOrderBy(String orderBy) {
        final var type = ListColumnType.valueOf(orderBy);
        if (type == ListColumnType.SIGNED) {
            return "signedDate";
        } else if (type == ListColumnType.PATIENT_ID) {
            return "civicRegistrationNumber";
        } else if (type == ListColumnType.STATUS) {
            return "status";
        } else if (type == ListColumnType.CERTIFICATE_TYPE_NAME) {
            return "type";
        } else {
            return "signedDate";
        }
    }
}
