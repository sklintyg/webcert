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
package se.inera.intyg.webcert.web.service.facade.list.filter;

import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListType;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;

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
        convertedFilter.setSignedFrom(ListFilterHelper.getSignedFrom(filter));
        convertedFilter.setSignedTo(ListFilterHelper.getSignedTo(filter));
        convertedFilter.setPatientId(ListFilterHelper.getPatientId(filter));
        convertedFilter.setOrderBy(ListFilterHelper.convertOrderBy(filter, ListType.CERTIFICATES));
        convertedFilter.setOrderAscending(ListFilterHelper.getAscending(filter));
        convertedFilter.setStartFrom(ListFilterHelper.getStartFrom(filter));
        convertedFilter.setPageSize(ListFilterHelper.getPageSize(filter));
        return convertedFilter;
    }
}
