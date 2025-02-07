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
import se.inera.intyg.webcert.web.service.facade.list.config.dto.*;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.facade.list.filter.QuestionFilterConverterImpl;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class QuestionFilterConverterImplTest {

    private final static String HSA_ID = "HSA_ID";

    @InjectMocks
    private QuestionFilterConverterImpl questionFilterConverter;

    @Test
    public void shouldConvertSentFrom() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterDateRangeValue();
        final var now = LocalDateTime.now();
        filterValue.setFrom(now);
        filterValue.setTo(LocalDateTime.now().plusDays(1));
        filter.addValue(filterValue, "SENT");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals(now, convertedFilter.getChangedFrom());
    }

    @Test
    public void shouldConvertSentToAndAddOneDay() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterDateRangeValue();
        final var now = LocalDateTime.now();
        filterValue.setFrom(now);
        filterValue.setTo(now.plusDays(1));
        filter.addValue(filterValue, "SENT");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals(now.plusDays(1), convertedFilter.getChangedTo());
    }

    @Test
    public void shouldSetHsaId() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterSelectValue();
        filterValue.setValue(HSA_ID);
        filter.addValue(filterValue, "SIGNED_BY");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals(HSA_ID, convertedFilter.getHsaId());
    }

    @Test
    public void shouldConvertPatientIdWithDash() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterPersonIdValue("19121212-1212");
        filter.addValue(filterValue, "PATIENT_ID");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals("191212121212", convertedFilter.getPatientPersonId());
    }

    @Test
    public void shouldConvertPatientIdWithoutDash() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterPersonIdValue("191212121212");
        filter.addValue(filterValue, "PATIENT_ID");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals("191212121212", convertedFilter.getPatientPersonId());
    }

    @Test
    public void shouldConvertEmptyPatientIdToNull() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterPersonIdValue("");
        filter.addValue(filterValue, "PATIENT_ID");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertNull(convertedFilter.getPatientPersonId());
    }

    @Test
    public void shouldConvertOrderReceivedDate() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterTextValue("SENT_RECEIVED");
        filter.addValue(filterValue, "ORDER_BY");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals("receivedDate", convertedFilter.getOrderBy());
    }

    @Test
    public void shouldConvertOrderPatientId() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterTextValue("PATIENT_ID");
        filter.addValue(filterValue, "ORDER_BY");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals("patientId", convertedFilter.getOrderBy());
    }

    @Test
    public void shouldConvertOrderSignedBy() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterTextValue("SIGNED_BY");
        filter.addValue(filterValue, "ORDER_BY");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals("signeratAvNamn", convertedFilter.getOrderBy());
    }

    @Test
    public void shouldConvertOrderQuestionAction() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterTextValue("QUESTION_ACTION");
        filter.addValue(filterValue, "ORDER_BY");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals("amne", convertedFilter.getOrderBy());
    }

    @Test
    public void shouldConvertOrderForwarded() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterTextValue("FORWARDED");
        filter.addValue(filterValue, "ORDER_BY");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals("vidarebefordrad", convertedFilter.getOrderBy());
    }

    @Test
    public void shouldConvertOrderSender() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterTextValue("SENDER");
        filter.addValue(filterValue, "ORDER_BY");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals("fragestallare", convertedFilter.getOrderBy());
    }

    @Test
    public void shouldConvertAscending() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterBooleanValue(true);
        filter.addValue(filterValue, "ASCENDING");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertTrue(convertedFilter.getOrderAscending());
    }

    @Test
    public void shouldConvertDescending() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterBooleanValue(false);
        filter.addValue(filterValue, "ASCENDING");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertFalse(convertedFilter.getOrderAscending());
    }

    @Test
    public void shouldConvertUnit() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterSelectValue("UNIT_ID");
        filter.addValue(filterValue, "UNIT");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals("UNIT_ID", convertedFilter.getEnhetId());
    }

    @Test
    public void shouldConvertPageSize() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterNumberValue(10);
        filter.addValue(filterValue, "PAGESIZE");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals(10, convertedFilter.getPageSize());
    }

    @Test
    public void shouldConvertStartFrom() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterNumberValue(10);
        filter.addValue(filterValue, "START_FROM");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals(10, convertedFilter.getStartFrom());
    }

    @Test
    public void shouldConvertStatusHandled() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterSelectValue("HANDLED");
        filter.addValue(filterValue, "STATUS");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals("HANTERAD", convertedFilter.getVantarPa());
    }

    @Test
    public void shouldConvertStatusNotHandled() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterSelectValue("NOT_HANDLED");
        filter.addValue(filterValue, "STATUS");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals("ALLA_OHANTERADE", convertedFilter.getVantarPa());
    }

    @Test
    public void shouldConvertStatusComplement() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterSelectValue("COMPLEMENT");
        filter.addValue(filterValue, "STATUS");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals("KOMPLETTERING_FRAN_VARDEN", convertedFilter.getVantarPa());
    }

    @Test
    public void shouldConvertStatusAnswer() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterSelectValue("ANSWER");
        filter.addValue(filterValue, "STATUS");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals("SVAR_FRAN_VARDEN", convertedFilter.getVantarPa());
    }

    @Test
    public void shouldConvertStatusReadAnswer() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterSelectValue("READ_ANSWER");
        filter.addValue(filterValue, "STATUS");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals("MARKERA_SOM_HANTERAD", convertedFilter.getVantarPa());
    }

    @Test
    public void shouldConvertStatusWait() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterSelectValue("WAIT");
        filter.addValue(filterValue, "STATUS");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals("SVAR_FRAN_FK", convertedFilter.getVantarPa());
    }

    @Test
    public void shouldConvertStatusAll() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterSelectValue("SHOW_ALL");
        filter.addValue(filterValue, "STATUS");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertEquals("ALLA", convertedFilter.getVantarPa());
    }

    @Test
    public void shouldConvertSenderFK() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterSelectValue("FK");
        filter.addValue(filterValue, "SENDER");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertTrue(convertedFilter.getQuestionFromFK());
    }

    @Test
    public void shouldConvertSenderWC() {
        final var filter = new ListFilter();
        final var filterValue = new ListFilterSelectValue("WC");
        filter.addValue(filterValue, "SENDER");

        final var convertedFilter = questionFilterConverter.convert(filter);

        assertTrue(convertedFilter.getQuestionFromWC());
    }
}
