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
import se.inera.intyg.webcert.web.service.facade.list.ListSortHelperImpl;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListColumnType;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterNumberValue;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItem;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ListSortHelperImplTest {

    @InjectMocks
    private ListSortHelperImpl listSortHelper;

    @Test
    public void shouldHandleEmptyList() {
        final var result = listSortHelper.sort(new ArrayList<CertificateListItem>(), "SAVED", true);
        assertEquals(0, result.size());
    }

    @Test
    public void shouldHandleListWithOneItem() {
        final var list = new ArrayList<CertificateListItem>(List.of(ListTestHelper.createCertificateListItemWithSavedBy("EXAMPLE")));
        final var result = listSortHelper.sort(list, "SAVED_BY", true);
        assertEquals(1, result.size());
    }

    @Test
    public void shouldSortOnSavedAsByDefault() {
        final var item1 = ListTestHelper.createCertificateListItemWithPersonId("191212121212");
        item1.addValue(ListColumnType.SAVED, LocalDateTime.now());
        final var item2 = ListTestHelper.createCertificateListItemWithPersonId("201212121212");
        item2.addValue(ListColumnType.SAVED, LocalDateTime.now().plusDays(1));

        final var result = listSortHelper.sort(
            new ArrayList<CertificateListItem>(List.of(item1, item2)), "", false);
        assertEquals(2, result.size());
        assertEquals(item1, result.get(1));
        assertEquals(item2, result.get(0));
    }

    @Test
    public void shouldSortPersonIdAscending() {
        final var item1 = ListTestHelper.createCertificateListItemWithPersonId("191212121212");
        final var item2 = ListTestHelper.createCertificateListItemWithPersonId("201212121212");
        final var result = listSortHelper.sort(
            new ArrayList<CertificateListItem>(List.of(item1, item2)), "PATIENT_ID", true);
        assertEquals(2, result.size());
        assertEquals(item1, result.get(0));
        assertEquals(item2, result.get(1));
    }

    @Test
    public void shouldSortPersonIdDescending() {
        final var item1 = ListTestHelper.createCertificateListItemWithPersonId("191212121212");
        final var item2 = ListTestHelper.createCertificateListItemWithPersonId("201212121212");
        final var result = listSortHelper.sort(
            new ArrayList<CertificateListItem>(List.of(item1, item2)), "PATIENT_ID", false);
        assertEquals(2, result.size());
        assertEquals(item1, result.get(1));
        assertEquals(item2, result.get(0));
    }

    @Test
    public void shouldSortSavedByAscending() {
        final var item1 = ListTestHelper.createCertificateListItemWithSavedBy("AAAA");
        final var item2 = ListTestHelper.createCertificateListItemWithSavedBy("BBBB");
        final var result = listSortHelper.sort(
            new ArrayList<CertificateListItem>(List.of(item1, item2)), "SAVED_BY", true);
        assertEquals(2, result.size());
        assertEquals(item1, result.get(0));
        assertEquals(item2, result.get(1));
    }

    @Test
    public void shouldSortSavedByDescending() {
        final var item1 = ListTestHelper.createCertificateListItemWithSavedBy("AAAA");
        final var item2 = ListTestHelper.createCertificateListItemWithSavedBy("BBBB");
        final var result = listSortHelper.sort(
            new ArrayList<CertificateListItem>(List.of(item1, item2)), "SAVED_BY", false);
        assertEquals(2, result.size());
        assertEquals(item1, result.get(1));
        assertEquals(item2, result.get(0));
    }

    @Test
    public void shouldSortSavedAscending() {
        final var item1 = ListTestHelper.createCertificateListItemWithSaved(LocalDateTime.now());
        final var item2 = ListTestHelper.createCertificateListItemWithSaved(LocalDateTime.now().plusDays(5));
        final var result = listSortHelper.sort(
            new ArrayList<CertificateListItem>(List.of(item1, item2)), "SAVED", true);
        assertEquals(2, result.size());
        assertEquals(item1, result.get(0));
        assertEquals(item2, result.get(1));
    }

    @Test
    public void shouldSortSavedDescending() {
        final var item1 = ListTestHelper.createCertificateListItemWithSaved(LocalDateTime.now());
        final var item2 = ListTestHelper.createCertificateListItemWithSaved(LocalDateTime.now().plusDays(5));
        final var result = listSortHelper.sort(
            new ArrayList<CertificateListItem>(List.of(item1, item2)), "SAVED", false);
        assertEquals(2, result.size());
        assertEquals(item1, result.get(1));
        assertEquals(item2, result.get(0));
    }

    @Test
    public void shouldSortForwardedAscending() {
        final var item1 = ListTestHelper.createCertificateListItemWithForwarded(true);
        final var item2 = ListTestHelper.createCertificateListItemWithForwarded(false);
        final var item3 = ListTestHelper.createCertificateListItemWithForwarded(true);
        final var result = listSortHelper.sort(
            new ArrayList<CertificateListItem>(List.of(item1, item2, item3)), "FORWARDED", true);
        assertEquals(3, result.size());
        assertEquals(item1, result.get(1));
        assertEquals(item2, result.get(0));
        assertEquals(item3, result.get(2));
    }

    @Test
    public void shouldSortForwardedDescending() {
        final var item1 = ListTestHelper.createCertificateListItemWithForwarded(true);
        final var item2 = ListTestHelper.createCertificateListItemWithForwarded(false);
        final var item3 = ListTestHelper.createCertificateListItemWithForwarded(true);
        final var result = listSortHelper.sort(
            new ArrayList<CertificateListItem>(List.of(item1, item2, item3)), "FORWARDED", false);
        assertEquals(3, result.size());
        assertEquals(item1, result.get(0));
        assertEquals(item2, result.get(2));
        assertEquals(item3, result.get(1));
    }

    @Test
    public void shouldSortTypeNameAscending() {
        final var item1 = ListTestHelper.createCertificateListItemWithCertificateTypeName("LISJP");
        final var item2 = ListTestHelper.createCertificateListItemWithCertificateTypeName("AG7804");
        final var result = listSortHelper.sort(
            new ArrayList<CertificateListItem>(List.of(item1, item2)), "CERTIFICATE_TYPE_NAME", true);
        assertEquals(2, result.size());
        assertEquals(item1, result.get(1));
        assertEquals(item2, result.get(0));
    }

    @Test
    public void shouldSortTypeNameDescending() {
        final var item1 = ListTestHelper.createCertificateListItemWithCertificateTypeName("LISJP");
        final var item2 = ListTestHelper.createCertificateListItemWithCertificateTypeName("AG7804");
        final var result = listSortHelper.sort(
            new ArrayList<CertificateListItem>(List.of(item1, item2)), "CERTIFICATE_TYPE_NAME", false);
        assertEquals(2, result.size());
        assertEquals(item1, result.get(0));
        assertEquals(item2, result.get(1));
    }

    @Test
    public void shouldSortStatusAscending() {
        final var item1 = ListTestHelper.createCertificateListItemWithStatus("STATUS1");
        final var item2 = ListTestHelper.createCertificateListItemWithStatus("STATUS0");
        final var result = listSortHelper.sort(
            new ArrayList<CertificateListItem>(List.of(item1, item2)), "STATUS", true);
        assertEquals(2, result.size());
        assertEquals(item1, result.get(1));
        assertEquals(item2, result.get(0));
    }

    @Test
    public void shouldSortStatusDescending() {
        final var item1 = ListTestHelper.createCertificateListItemWithStatus("STATUS1");
        final var item2 = ListTestHelper.createCertificateListItemWithStatus("STATUS0");
        final var result = listSortHelper.sort(
            new ArrayList<CertificateListItem>(List.of(item1, item2)), "STATUS", false);
        assertEquals(2, result.size());
        assertEquals(item1, result.get(0));
        assertEquals(item2, result.get(1));
    }
}
