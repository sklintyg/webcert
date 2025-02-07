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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.web.service.facade.list.filter.DraftFilterConverterImpl;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.*;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItemStatus;
import se.inera.intyg.webcert.web.service.facade.list.dto.ForwardedType;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class DraftFilterConverterImplTest {

    @Mock
    private WebCertUserService webCertUserService;

    @InjectMocks
    private DraftFilterConverterImpl draftFilterConverter;

    @BeforeEach
    public void setup() {
        final var user = new WebCertUser();
        final var unit = ListTestHelper.buildVardenhet();
        user.setValdVardenhet(unit);
        doReturn(user).when(webCertUserService).getUser();
    }

    @Test
    public void shouldConvertSavedFrom() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterDateRangeValue();
        final var now = LocalDateTime.now();
        filterValue.setFrom(now);
        filterValue.setTo(LocalDateTime.now().plusDays(1));
        filter.addValue(filterValue, "SAVED");

        final var convertedFilter = draftFilterConverter.convert(filter);

        assertEquals(now, convertedFilter.getSavedFrom());
    }

    @Test
    public void shouldConvertSavedTo() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterDateRangeValue();
        final var now = LocalDateTime.now();
        filterValue.setFrom(now);
        filterValue.setTo(now.plusDays(1));
        filter.addValue(filterValue, "SAVED");

        final var convertedFilter = draftFilterConverter.convert(filter);

        assertEquals(now.plusDays(2), convertedFilter.getSavedTo());
    }

    @Test
    public void shouldConvertSavedBy() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterSelectValue("SAVED_BY_HSA_ID");
        filter.addValue(filterValue, "SAVED_BY");

        final var convertedFilter = draftFilterConverter.convert(filter);

        assertEquals("SAVED_BY_HSA_ID", convertedFilter.getSavedByHsaId());
    }

    @Test
    public void shouldConvertPatientId() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterPersonIdValue("19121212-1212");
        filter.addValue(filterValue, "PATIENT_ID");

        final var convertedFilter = draftFilterConverter.convert(filter);

        assertEquals("19121212-1212", convertedFilter.getPatientId());
    }

    @Test
    public void shouldConvertForwarded() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterSelectValue(ForwardedType.FORWARDED.toString());
        filter.addValue(filterValue, "FORWARDED");

        final var convertedFilter = draftFilterConverter.convert(filter);

        assertTrue(convertedFilter.getNotified());
    }

    @Test
    public void shouldConvertNotForwarded() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterSelectValue(ForwardedType.NOT_FORWARDED.toString());
        filter.addValue(filterValue, "FORWARDED");

        final var convertedFilter = draftFilterConverter.convert(filter);

        assertFalse(convertedFilter.getNotified());
    }

    @Test
    public void shouldConvertShowAllForwardedAsNull() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterSelectValue(ForwardedType.SHOW_ALL.toString());
        filter.addValue(filterValue, "FORWARDED");

        final var convertedFilter = draftFilterConverter.convert(filter);

        assertNull(convertedFilter.getNotified());
    }

    @Test
    public void shouldConvertOrder() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterTextValue("ORDER_BY_VALUE");
        filter.addValue(filterValue, "ORDER_BY");

        final var convertedFilter = draftFilterConverter.convert(filter);

        assertEquals("ORDER_BY_VALUE", convertedFilter.getOrderBy());
    }

    @Test
    public void shouldConvertAscending() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterBooleanValue(true);
        filter.addValue(filterValue, "ASCENDING");

        final var convertedFilter = draftFilterConverter.convert(filter);

        assertTrue(convertedFilter.getOrderAscending());
    }

    @Test
    public void shouldConvertDescending() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterBooleanValue(false);
        filter.addValue(filterValue, "ASCENDING");

        final var convertedFilter = draftFilterConverter.convert(filter);

        assertFalse(convertedFilter.getOrderAscending());
    }

    @Test
    public void shouldConvertStatusToShowAllIfEmpty() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterSelectValue("");
        filter.addValue(filterValue, "STATUS");

        final var convertedFilter = draftFilterConverter.convert(filter);

        assertEquals(3, convertedFilter.getStatusList().size());
    }

    @Test
    public void shouldConvertStatusComplete() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterSelectValue(CertificateListItemStatus.COMPLETE.toString());
        filter.addValue(filterValue, "STATUS");

        final var convertedFilter = draftFilterConverter.convert(filter);

        assertEquals(1, convertedFilter.getStatusList().size());
        assertEquals(UtkastStatus.DRAFT_COMPLETE, convertedFilter.getStatusList().get(0));
    }

    @Test
    public void shouldConvertStatusIncomplete() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterSelectValue(CertificateListItemStatus.INCOMPLETE.toString());
        filter.addValue(filterValue, "STATUS");

        final var convertedFilter = draftFilterConverter.convert(filter);

        assertEquals(1, convertedFilter.getStatusList().size());
        assertEquals(UtkastStatus.DRAFT_INCOMPLETE, convertedFilter.getStatusList().get(0));
    }

    @Test
    public void shouldConvertStatusLocked() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterSelectValue(CertificateListItemStatus.LOCKED.toString());
        filter.addValue(filterValue, "STATUS");

        final var convertedFilter = draftFilterConverter.convert(filter);

        assertEquals(1, convertedFilter.getStatusList().size());
        assertEquals(UtkastStatus.DRAFT_LOCKED, convertedFilter.getStatusList().get(0));
    }
}
