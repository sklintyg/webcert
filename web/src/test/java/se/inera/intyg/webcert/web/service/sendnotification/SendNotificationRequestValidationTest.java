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

package se.inera.intyg.webcert.web.service.sendnotification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendNotificationRequestValidationTest {

    @InjectMocks
    private SendNotificationRequestValidation sendNotificationRequestValidation;

    @Nested
    class ValidateId {

        @Test
        void shouldThrowExceptionIfIdIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> sendNotificationRequestValidation.validateId(""));
        }

        @Test
        void shouldThrowExceptionIfIdIsNull() {
            assertThrows(IllegalArgumentException.class, () -> sendNotificationRequestValidation.validateId(null));
        }

        @Test
        void shouldThrowExceptionIfIdIsBlank() {
            assertThrows(IllegalArgumentException.class, () -> sendNotificationRequestValidation.validateId(" "));
        }

        @Test
        void shouldNotThrowIfValidId() {
            assertDoesNotThrow(() -> sendNotificationRequestValidation.validateId("ID"));
        }
    }

    @Nested
    class ValidateIds {

        @Test
        void shouldThrowExceptionIfIdIsEmpty() {
            final var ids = new ArrayList<String>();
            assertThrows(IllegalArgumentException.class, () -> sendNotificationRequestValidation.validateIds(ids));
        }

        @Test
        void shouldThrowExceptionIfIdIsNull() {
            assertThrows(IllegalArgumentException.class, () -> sendNotificationRequestValidation.validateIds(null));
        }

        @Test
        void shouldNotThrowIfValidId() {
            assertDoesNotThrow(() -> sendNotificationRequestValidation.validateIds(List.of("ID")));
        }
    }

    @Nested
    class ValidateDateWithoutIntervalLimit {

        @Test
        void shouldThrowExceptionIfStartDateIsBeforeLimit() {
            final var end = LocalDateTime.now().minusDays(4);
            final var start = LocalDateTime.now().minusDays(5);
            final var limitBack = 4;
            final var limitInterval = 10;

            assertThrows(IllegalArgumentException.class,
                () -> sendNotificationRequestValidation.validateDate(start, end, limitInterval, limitBack));
        }

        @Test
        void shouldNotThrowExceptionIfStartDateIsAfterLimit() {
            final var end = LocalDateTime.now().minusDays(1);
            final var start = LocalDateTime.now().minusDays(2);
            final var limitBack = 4;
            final var limitInterval = 10;

            assertDoesNotThrow(() -> sendNotificationRequestValidation.validateDate(start, end, limitInterval, limitBack));
        }

        @Test
        void shouldThrowExceptionIfEndIsAfterStart() {
            final var end = LocalDateTime.now().minusDays(1);
            final var start = LocalDateTime.now();
            final var limitBack = 4;
            final var limitInterval = 10;

            assertThrows(IllegalArgumentException.class,
                () -> sendNotificationRequestValidation.validateDate(start, end, limitInterval, limitBack));
        }

        @Test
        void shouldThrowExceptionIfIntervalIsOverLimit() {
            final var end = LocalDateTime.now();
            final var start = LocalDateTime.now().minusDays(11);
            final var limitBack = 20;
            final var limitInterval = 10;

            assertThrows(IllegalArgumentException.class,
                () -> sendNotificationRequestValidation.validateDate(start, end, limitInterval, limitBack));
        }

        @Test
        void shouldThrowExceptionIfIntervalIsOverLimitWhenEndIsNull() {
            final var start = LocalDateTime.now().minusDays(11);
            final var limitBack = 20;
            final var limitInterval = 10;

            assertThrows(IllegalArgumentException.class,
                () -> sendNotificationRequestValidation.validateDate(start, null, limitInterval, limitBack));
        }
    }

    @Nested
    class ValidateDateWithIntervalLimit {

        @Test
        void shouldThrowExceptionIfStartDateIsBeforeLimit() {
            final var end = LocalDateTime.now().minusDays(4);
            final var start = LocalDateTime.now().minusDays(5);
            final var limit = 4;

            assertThrows(IllegalArgumentException.class, () -> sendNotificationRequestValidation.validateDate(start, end, limit));
        }

        @Test
        void shouldThrowExceptionIfEndIsAfterStart() {
            final var end = LocalDateTime.now().minusDays(1);
            final var start = LocalDateTime.now();
            final var limit = 4;

            assertThrows(IllegalArgumentException.class, () -> sendNotificationRequestValidation.validateDate(start, end, limit));
        }

        @Test
        void shouldNotThrowExceptionIfStartDateIsAfterLimit() {
            final var end = LocalDateTime.now().minusDays(1);
            final var start = LocalDateTime.now().minusDays(2);
            final var limit = 4;

            assertDoesNotThrow(() -> sendNotificationRequestValidation.validateDate(start, end, limit));
        }
    }

}