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
package se.inera.intyg.webcert.web.service.facade.impl.list;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.facade.list.ListPaginationHelperImpl;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterNumberValue;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItem;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ListPaginationHelperImplTest {

    @InjectMocks
    private ListPaginationHelperImpl listPaginationHelper;

    @Test
    public void shouldHandleEmptyList() {
        final var result = listPaginationHelper.paginate(new ArrayList<>(), new ListFilter());
        assertEquals(0, result.size());
    }

    @Test
    public void shouldHandleEmptyFilter() {
        final var result = listPaginationHelper.paginate(List.of(new CertificateListItem()), new ListFilter());
        assertEquals(1, result.size());
    }

    @Test
    public void shouldReturnEmptyListIfZeroPageSize() {
        final var filter = new ListFilter();
        filter.addValue(new ListFilterNumberValue(0), "PAGESIZE");
        final var result = listPaginationHelper.paginate(new ArrayList<>(), filter);
        assertEquals(0, result.size());
    }

    @Test
    public void shouldReturnEmptyListIfListIsSmallerThanStartFrom() {
        final var filter = new ListFilter();
        filter.addValue(new ListFilterNumberValue(5), "PAGESIZE");
        filter.addValue(new ListFilterNumberValue(10), "START_FROM");
        final var result = listPaginationHelper.paginate(List.of(new CertificateListItem()), filter);
        assertEquals(0, result.size());
    }

    @Test
    public void shouldPaginateWithDefaultValuesIfNotSet() {
        final var filter = new ListFilter();
        final var originalList = createList(20);
        final var result = listPaginationHelper.paginate(originalList, filter);
        assertEquals(10, result.size());
        assertEquals(originalList.get(0), result.get(0));
        assertEquals(originalList.get(9), result.get(9));
    }

    @Test
    public void shouldReturnAllHitsIfListIsSmallerThanPageSize() {
        final var filter = new ListFilter();
        final var originalList = createList(8);
        final var result = listPaginationHelper.paginate(originalList, filter);
        assertEquals(8, result.size());
        assertEquals(originalList.get(0), result.get(0));
        assertEquals(originalList.get(7), result.get(7));
    }

    @Test
    public void shouldReturnListStartingFromStartFrom() {
        final var filter = new ListFilter();
        final var originalList = createList(10);
        filter.addValue(new ListFilterNumberValue(2), "START_FROM");
        final var result = listPaginationHelper.paginate(originalList, filter);
        assertEquals(8, result.size());
        assertEquals(originalList.get(2), result.get(0));
        assertEquals(originalList.get(9), result.get(7));
    }

    private List<CertificateListItem> createList(int listItems) {
        final var list = new ArrayList<CertificateListItem>();
        for (int i = 0; i < listItems; i++) {
            list.add(new CertificateListItem());
        }
        return list;
    }
}
