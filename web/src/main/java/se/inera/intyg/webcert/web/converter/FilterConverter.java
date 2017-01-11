/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.converter;

import java.util.List;
import java.util.Set;

import com.google.common.base.Strings;

import se.inera.intyg.webcert.persistence.model.Filter;
import se.inera.intyg.webcert.persistence.model.VantarPa;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;

public final class FilterConverter {

    public static final Integer DEFAULT_PAGE_SIZE = 10;

    private FilterConverter() {
    }

    public static Filter convert(QueryFragaSvarParameter source, List<String> unitIds, Set<String> intygsTyper) {
        Filter filter = new Filter();

        filter.getEnhetsIds().addAll(unitIds);

        if (!Strings.isNullOrEmpty(source.getVantarPa())) {
            filter.setVantarPa(VantarPa.valueOf(source.getVantarPa()));
        }

        filter.setChangedFrom(source.getChangedFrom());
        if (source.getChangedTo() != null) {
            filter.setChangedTo(source.getChangedTo().plusDays(1));
        }
        filter.setHsaId(source.getHsaId());
        filter.setQuestionFromFK(getSafeBooleanValue(source.getQuestionFromFK()));
        filter.setQuestionFromWC(getSafeBooleanValue(source.getQuestionFromWC()));
        filter.setReplyLatest(source.getReplyLatest());
        filter.setVidarebefordrad(source.getVidarebefordrad());

        filter.setPageSize((source.getPageSize() == null) ? DEFAULT_PAGE_SIZE : source.getPageSize());
        filter.setStartFrom((source.getStartFrom() == null) ? Integer.valueOf(0) : source.getStartFrom());

        filter.setIntygsTyper(intygsTyper);

        return filter;
    }

    private static boolean getSafeBooleanValue(Boolean booleanObj) {
        return booleanObj != null && booleanObj;
    }

}
