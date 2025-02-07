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
package se.inera.intyg.webcert.web.service.facade.list;

import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.*;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItem;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;

import java.util.Collections;
import java.util.List;

@Service
public class ListPaginationHelperImpl implements ListPaginationHelper {

    private static final int DEFAULT_PAGE_SIZE = 10;

    @Override
    public List<CertificateListItem> paginate(List<CertificateListItem> list, ListFilter filter) {
        return paginateList(list, getStartFrom(filter), getPageSize(filter));
    }

    private List<CertificateListItem> paginateList(List<CertificateListItem> list, int startFrom, int pageSize) {
        if (startFrom < list.size()) {
            int toIndex = startFrom + pageSize;
            if (toIndex > list.size()) {
                toIndex = list.size();
            }
            return list.subList(startFrom, toIndex);
        } else {
            return Collections.emptyList();
        }
    }

    private int getPageSize(ListFilter filter) {
        ListFilterNumberValue pageSize = (ListFilterNumberValue) filter.getValue("PAGESIZE");
        return pageSize == null ? DEFAULT_PAGE_SIZE : pageSize.getValue();
    }

    private int getStartFrom(ListFilter filter) {
        ListFilterNumberValue startFrom = (ListFilterNumberValue) filter.getValue("START_FROM");
        return startFrom == null ? 0 : startFrom.getValue();
    }
}
