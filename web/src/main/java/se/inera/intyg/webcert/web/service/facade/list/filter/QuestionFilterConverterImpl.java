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
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;

@Service
public class QuestionFilterConverterImpl implements QuestionFilterConverter {

    @Override
    public QueryFragaSvarParameter convert(ListFilter filter) {
        return convertFilter(filter);
    }

    private QueryFragaSvarParameter convertFilter(ListFilter filter) {
        final var convertedFilter = new QueryFragaSvarParameter();
        final var patientId = ListFilterHelper.getPatientIdWithoutDash(filter);

        convertedFilter.setOrderBy(ListFilterHelper.convertOrderBy(filter, ListType.QUESTIONS));
        convertedFilter.setOrderAscending(ListFilterHelper.getAscending(filter));
        convertedFilter.setPatientPersonId(patientId.length() == 0 ? null : patientId);
        convertedFilter.setPageSize(ListFilterHelper.getPageSize(filter));
        convertedFilter.setStartFrom(ListFilterHelper.getStartFrom(filter));
        convertedFilter.setVidarebefordrad(ListFilterHelper.getForwarded(filter));
        convertedFilter.setHsaId(ListFilterHelper.getSignedBy(filter));
        convertedFilter.setChangedFrom(ListFilterHelper.getSentFrom(filter));
        convertedFilter.setChangedTo(ListFilterHelper.getSentTo(filter));
        convertedFilter.setVantarPa(ListFilterHelper.getQuestionStatus(filter));
        convertedFilter.setQuestionFromFK(ListFilterHelper.includeQuestionFromFK(filter));
        convertedFilter.setQuestionFromWC(ListFilterHelper.includeQuestionFromUnit(filter));
        convertedFilter.setEnhetId(ListFilterHelper.getUnitId(filter));

        return convertedFilter;
    }
}
