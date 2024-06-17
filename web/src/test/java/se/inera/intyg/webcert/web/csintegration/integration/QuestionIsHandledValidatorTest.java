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
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;

@ExtendWith(MockitoExtension.class)
class QuestionIsHandledValidatorTest {

    @InjectMocks
    QuestionIsHandledValidator questionIsHandledValidator;

    @Test
    void shouldReturnTrueIfStatusIsClosed() {
        final var arendeListItem = new ArendeListItem();
        arendeListItem.setStatus(Status.CLOSED);

        final var result = questionIsHandledValidator.validate(arendeListItem);
        assertTrue(result);
    }

    @Test
    void shouldReturnTrueIfStatusIsAnswered() {
        final var arendeListItem = new ArendeListItem();
        arendeListItem.setStatus(Status.ANSWERED);

        final var result = questionIsHandledValidator.validate(arendeListItem);
        assertTrue(result);
    }

    @Test
    void shouldReturnTrueIfSubjectIsRevoked() {
        final var arendeListItem = new ArendeListItem();
        arendeListItem.setStatus(Status.PENDING_INTERNAL_ACTION);
        arendeListItem.setAmne("MAKULERING");

        final var result = questionIsHandledValidator.validate(arendeListItem);
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseIfPendingInternalAction() {
        final var arendeListItem = new ArendeListItem();
        arendeListItem.setStatus(Status.PENDING_INTERNAL_ACTION);
        arendeListItem.setAmne("KONTAKT");

        final var result = questionIsHandledValidator.validate(arendeListItem);
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseIfPendingExternalAction() {
        final var arendeListItem = new ArendeListItem();
        arendeListItem.setStatus(Status.PENDING_EXTERNAL_ACTION);
        arendeListItem.setAmne("KONTAKT");

        final var result = questionIsHandledValidator.validate(arendeListItem);
        assertFalse(result);
    }
}
