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
import se.inera.intyg.webcert.web.service.facade.list.filter.CertificateFilterConverterImpl;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.*;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class CertificateFilterConverterImplTest {

    private final static String HSA_ID = "HSA_ID";
    private final static String[] UNITS = {"UNIT1", "UNIT2"};

    @InjectMocks
    private CertificateFilterConverterImpl certificateFilterConverter;

    @Test
    public void shouldConvertSignedFrom() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterDateRangeValue();
        final var now = LocalDateTime.now();
        filterValue.setFrom(now);
        filterValue.setTo(LocalDateTime.now().plusDays(1));
        filter.addValue(filterValue, "SIGNED");

        final var convertedFilter = certificateFilterConverter.convert(filter, HSA_ID, UNITS);

        assertEquals(now, convertedFilter.getSignedFrom());
    }

    @Test
    public void shouldConvertSignedTo() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterDateRangeValue();
        final var now = LocalDateTime.now();
        filterValue.setFrom(now);
        filterValue.setTo(now.plusDays(1));
        filter.addValue(filterValue, "SIGNED");

        final var convertedFilter = certificateFilterConverter.convert(filter, HSA_ID, UNITS);

        assertEquals(now.plusDays(1), convertedFilter.getSignedTo());
    }

    @Test
    public void shouldSetHsaId() {
        final var filter = new ListFilter();

        final var convertedFilter = certificateFilterConverter.convert(filter, HSA_ID, UNITS);

        assertEquals(HSA_ID, convertedFilter.getHsaId());
    }

    @Test
    public void shouldConvertPatientId() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterPersonIdValue("19121212-1212");
        filter.addValue(filterValue, "PATIENT_ID");

        final var convertedFilter = certificateFilterConverter.convert(filter, HSA_ID, UNITS);

        assertEquals("19121212-1212", convertedFilter.getPatientId());
    }

    @Test
    public void shouldConvertOrderSigned() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterTextValue("SIGNED");
        filter.addValue(filterValue, "ORDER_BY");

        final var convertedFilter = certificateFilterConverter.convert(filter, HSA_ID, UNITS);

        assertEquals("signedDate", convertedFilter.getOrderBy());
    }

    @Test
    public void shouldConvertOrderSSN() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterTextValue("PATIENT_ID");
        filter.addValue(filterValue, "ORDER_BY");

        final var convertedFilter = certificateFilterConverter.convert(filter, HSA_ID, UNITS);

        assertEquals("civicRegistrationNumber", convertedFilter.getOrderBy());
    }

    @Test
    public void shouldConvertOrderStatus() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterTextValue("STATUS");
        filter.addValue(filterValue, "ORDER_BY");

        final var convertedFilter = certificateFilterConverter.convert(filter, HSA_ID, UNITS);

        assertEquals("status", convertedFilter.getOrderBy());
    }

    @Test
    public void shouldConvertOrderTypeName() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterTextValue("CERTIFICATE_TYPE_NAME");
        filter.addValue(filterValue, "ORDER_BY");

        final var convertedFilter = certificateFilterConverter.convert(filter, HSA_ID, UNITS);

        assertEquals("type", convertedFilter.getOrderBy());
    }

    @Test
    public void shouldConvertAscending() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterBooleanValue(true);
        filter.addValue(filterValue, "ASCENDING");

        final var convertedFilter = certificateFilterConverter.convert(filter, HSA_ID, UNITS);

        assertTrue(convertedFilter.getOrderAscending());
    }

    @Test
    public void shouldConvertDescending() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterBooleanValue(false);
        filter.addValue(filterValue, "ASCENDING");

        final var convertedFilter = certificateFilterConverter.convert(filter, HSA_ID, UNITS);

        assertFalse(convertedFilter.getOrderAscending());
    }

    @Test
    public void shouldConvertUnits() {
        final var filter = new ListFilter();

        final var convertedFilter = certificateFilterConverter.convert(filter, HSA_ID, UNITS);

        assertEquals(UNITS, convertedFilter.getUnitIds());
    }
}
