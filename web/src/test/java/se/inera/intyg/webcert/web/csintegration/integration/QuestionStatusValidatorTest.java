/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.csintegration.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.facade.list.dto.QuestionStatusType;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;

@ExtendWith(MockitoExtension.class)
class QuestionStatusValidatorTest {

    @InjectMocks
    private QuestionStatusValidator questionStatusValidator;

    @Test
    void shouldReturnTrueIfShowAll() {
        final var arendeListItem = new ArendeListItem();
        assertTrue(questionStatusValidator.validate(arendeListItem, QuestionStatusType.SHOW_ALL));
    }

    @Test
    void shouldReturnFalseIfStatusClosedAndFilterOnNotHandled() {
        final var arendeListItem = new ArendeListItem();
        arendeListItem.setStatus(Status.CLOSED);

        assertFalse(questionStatusValidator.validate(arendeListItem, QuestionStatusType.NOT_HANDLED));
    }

    @Test
    void shouldReturnTrueIfNotClosedAndFilterOnNotHandled() {
        final var arendeListItem = new ArendeListItem();
        arendeListItem.setStatus(Status.PENDING_EXTERNAL_ACTION);

        assertTrue(questionStatusValidator.validate(arendeListItem, QuestionStatusType.NOT_HANDLED));
    }

    @Test
    void shouldReturnFalseIfStatusNotClosedAndFilterOnHandled() {
        final var arendeListItem = new ArendeListItem();
        arendeListItem.setStatus(Status.PENDING_EXTERNAL_ACTION);

        assertFalse(questionStatusValidator.validate(arendeListItem, QuestionStatusType.HANDLED));
    }

    @Test
    void shouldReturnTrueIfStatusClosedAndFilterOnHandled() {
        final var arendeListItem = new ArendeListItem();
        arendeListItem.setStatus(Status.CLOSED);

        assertTrue(questionStatusValidator.validate(arendeListItem, QuestionStatusType.HANDLED));
    }

    @Test
    void shouldReturnTrueIfPendingInternalActionAndComplementWhenFilteringOnComplement() {
        final var arendeListItem = new ArendeListItem();
        arendeListItem.setStatus(Status.PENDING_INTERNAL_ACTION);
        arendeListItem.setAmne("KOMPLT");

        assertTrue(questionStatusValidator.validate(arendeListItem, QuestionStatusType.COMPLEMENT));
    }

    @Test
    void shouldReturnTrueIfPendingInternalActionAndContactWhenFilteringOnAnswer() {
        final var arendeListItem = new ArendeListItem();
        arendeListItem.setStatus(Status.PENDING_INTERNAL_ACTION);
        arendeListItem.setAmne("KONTKT");

        assertTrue(questionStatusValidator.validate(arendeListItem, QuestionStatusType.ANSWER));
    }

    @Test
    void shouldReturnTrueIfAnsweredWhenFilteringOnReadAnswer() {
        final var arendeListItem = new ArendeListItem();
        arendeListItem.setStatus(Status.ANSWERED);

        assertTrue(questionStatusValidator.validate(arendeListItem, QuestionStatusType.READ_ANSWER));
    }

    @Test
    void shouldReturnFalseIfPendingInternalActionWhenFilteringOnWait() {
        final var arendeListItem = new ArendeListItem();
        arendeListItem.setStatus(Status.PENDING_INTERNAL_ACTION);

        assertFalse(questionStatusValidator.validate(arendeListItem, QuestionStatusType.WAIT));
    }

    @Test
    void shouldReturnFalseIfPendingExternalActionWhenFilteringOnWait() {
        final var arendeListItem = new ArendeListItem();
        arendeListItem.setStatus(Status.PENDING_EXTERNAL_ACTION);

        assertTrue(questionStatusValidator.validate(arendeListItem, QuestionStatusType.WAIT));
    }
}
