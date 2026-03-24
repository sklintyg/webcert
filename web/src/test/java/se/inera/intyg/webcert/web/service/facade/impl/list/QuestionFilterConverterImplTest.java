/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterBooleanValue;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterDateRangeValue;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterNumberValue;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterPersonIdValue;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterSelectValue;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterTextValue;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.facade.list.filter.QuestionFilterConverterImpl;

@ExtendWith(MockitoExtension.class)
class QuestionFilterConverterImplTest {

  private static final String HSA_ID = "HSA_ID";

  @InjectMocks private QuestionFilterConverterImpl questionFilterConverter;

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

    assertEquals( convertedFilter.getPatientPersonId(),"191212121212");
  }

  @Test
  public void shouldConvertPatientIdWithoutDash() {
    final var filter = new ListFilter();
    final var filterValue = new ListFilterPersonIdValue("191212121212");
    filter.addValue(filterValue, "PATIENT_ID");

    final var convertedFilter = questionFilterConverter.convert(filter);

    assertEquals( convertedFilter.getPatientPersonId(),"191212121212");
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

    assertEquals( convertedFilter.getOrderBy(),"receivedDate");
  }

  @Test
  public void shouldConvertOrderPatientId() {
    final var filter = new ListFilter();
    final var filterValue = new ListFilterTextValue("PATIENT_ID");
    filter.addValue(filterValue, "ORDER_BY");

    final var convertedFilter = questionFilterConverter.convert(filter);

    assertEquals( convertedFilter.getOrderBy(),"patientId");
  }

  @Test
  public void shouldConvertOrderSignedBy() {
    final var filter = new ListFilter();
    final var filterValue = new ListFilterTextValue("SIGNED_BY");
    filter.addValue(filterValue, "ORDER_BY");

    final var convertedFilter = questionFilterConverter.convert(filter);

    assertEquals( convertedFilter.getOrderBy(),"signeratAvNamn");
  }

  @Test
  public void shouldConvertOrderQuestionAction() {
    final var filter = new ListFilter();
    final var filterValue = new ListFilterTextValue("QUESTION_ACTION");
    filter.addValue(filterValue, "ORDER_BY");

    final var convertedFilter = questionFilterConverter.convert(filter);

    assertEquals( convertedFilter.getOrderBy(),"amne");
  }

  @Test
  public void shouldConvertOrderForwarded() {
    final var filter = new ListFilter();
    final var filterValue = new ListFilterTextValue("FORWARDED");
    filter.addValue(filterValue, "ORDER_BY");

    final var convertedFilter = questionFilterConverter.convert(filter);

    assertEquals( convertedFilter.getOrderBy(),"vidarebefordrad");
  }

  @Test
  public void shouldConvertOrderSender() {
    final var filter = new ListFilter();
    final var filterValue = new ListFilterTextValue("SENDER");
    filter.addValue(filterValue, "ORDER_BY");

    final var convertedFilter = questionFilterConverter.convert(filter);

    assertEquals( convertedFilter.getOrderBy(),"fragestallare");
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

    assertEquals( convertedFilter.getEnhetId(),"UNIT_ID");
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

    assertEquals( convertedFilter.getVantarPa(),"HANTERAD");
  }

  @Test
  public void shouldConvertStatusNotHandled() {
    final var filter = new ListFilter();
    final var filterValue = new ListFilterSelectValue("NOT_HANDLED");
    filter.addValue(filterValue, "STATUS");

    final var convertedFilter = questionFilterConverter.convert(filter);

    assertEquals( convertedFilter.getVantarPa(),"ALLA_OHANTERADE");
  }

  @Test
  public void shouldConvertStatusComplement() {
    final var filter = new ListFilter();
    final var filterValue = new ListFilterSelectValue("COMPLEMENT");
    filter.addValue(filterValue, "STATUS");

    final var convertedFilter = questionFilterConverter.convert(filter);

    assertEquals( convertedFilter.getVantarPa(),"KOMPLETTERING_FRAN_VARDEN");
  }

  @Test
  public void shouldConvertStatusAnswer() {
    final var filter = new ListFilter();
    final var filterValue = new ListFilterSelectValue("ANSWER");
    filter.addValue(filterValue, "STATUS");

    final var convertedFilter = questionFilterConverter.convert(filter);

    assertEquals( convertedFilter.getVantarPa(),"SVAR_FRAN_VARDEN");
  }

  @Test
  public void shouldConvertStatusReadAnswer() {
    final var filter = new ListFilter();
    final var filterValue = new ListFilterSelectValue("READ_ANSWER");
    filter.addValue(filterValue, "STATUS");

    final var convertedFilter = questionFilterConverter.convert(filter);

    assertEquals( convertedFilter.getVantarPa(),"MARKERA_SOM_HANTERAD");
  }

  @Test
  public void shouldConvertStatusWait() {
    final var filter = new ListFilter();
    final var filterValue = new ListFilterSelectValue("WAIT");
    filter.addValue(filterValue, "STATUS");

    final var convertedFilter = questionFilterConverter.convert(filter);

    assertEquals( convertedFilter.getVantarPa(),"SVAR_FRAN_FK");
  }

  @Test
  public void shouldConvertStatusAll() {
    final var filter = new ListFilter();
    final var filterValue = new ListFilterSelectValue("SHOW_ALL");
    filter.addValue(filterValue, "STATUS");

    final var convertedFilter = questionFilterConverter.convert(filter);

    assertEquals( convertedFilter.getVantarPa(),"ALLA");
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
