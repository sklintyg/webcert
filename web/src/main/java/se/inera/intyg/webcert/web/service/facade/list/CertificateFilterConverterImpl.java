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
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;

import java.time.LocalDateTime;

@Service
public class CertificateFilterConverterImpl implements CertificateFilterConverter {

    private final WebCertUserService webCertUserService;

    public CertificateFilterConverterImpl(WebCertUserService webCertUserService) {
        this.webCertUserService = webCertUserService;
    }

    @Override
    public QueryIntygParameter convert(ListFilter filter, String hsaId, String[] units) {
        return convertFilter(filter, hsaId, units);
    }

    private QueryIntygParameter convertFilter(ListFilter filter, String hsaId, String[] units) {
        final var user = webCertUserService.getUser();
        final var convertedFilter = new QueryIntygParameter();

        ListFilterDateRangeValue signed = (ListFilterDateRangeValue) filter.getValue("SIGNED");
        ListFilterPersonIdValue patientId = (ListFilterPersonIdValue) filter.getValue("PATIENT_ID");
        ListFilterTextValue orderBy = (ListFilterTextValue) filter.getValue("ORDER_BY");
        ListFilterBooleanValue ascending = (ListFilterBooleanValue) filter.getValue("ASCENDING");
        ListFilterNumberValue startFrom = (ListFilterNumberValue) filter.getValue("START_FROM");
        ListFilterNumberValue pageSize = (ListFilterNumberValue) filter.getValue("PAGESIZE");

        convertedFilter.setHsaId(hsaId);
        convertedFilter.setUnitIds(units);
        convertedFilter.setSignedFrom(signed != null ? signed.getFrom() : LocalDateTime.now().minusMonths(3));
        convertedFilter.setSignedTo(signed != null ? signed.getTo() : null);
        convertedFilter.setPatientId(patientId != null ? patientId.getValue() : "");
        convertedFilter.setOrderBy(orderBy == null ? "signedDate" : convertOrderBy(orderBy.getValue()));
        convertedFilter.setOrderAscending(ascending != null && ascending.getValue());
        convertedFilter.setStartFrom(startFrom == null ? 0 : startFrom.getValue());
        convertedFilter.setPageSize(pageSize == null ? 10 : pageSize.getValue());
        return convertedFilter;
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
