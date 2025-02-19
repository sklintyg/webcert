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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendNotificationRequestValidatorTest {

    @InjectMocks
    private SendNotificationRequestValidator sendNotificationRequestValidator;

    @Nested
    class ValidateId {

        @Test
        void shouldThrowExceptionIfIdIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> sendNotificationRequestValidator.validateId(""));
        }

        @Test
        void shouldThrowExceptionIfIdIsNull() {
            assertThrows(IllegalArgumentException.class, () -> sendNotificationRequestValidator.validateId(null));
        }

        @Test
        void shouldThrowExceptionIfIdIsBlank() {
            assertThrows(IllegalArgumentException.class, () -> sendNotificationRequestValidator.validateId(" "));
        }

        @Test
        void shouldNotThrowIfValidId() {
            assertDoesNotThrow(() -> sendNotificationRequestValidator.validateId("ID"));
        }
    }

    @Nested
    class ValidateIds {

        @Test
        void shouldThrowExceptionIfIdIsEmpty() {
            final var ids = new ArrayList<String>();
            assertThrows(IllegalArgumentException.class, () -> sendNotificationRequestValidator.validateIds(ids));
        }

        @Test
        void shouldThrowExceptionIfIdIsNull() {
            assertThrows(IllegalArgumentException.class, () -> sendNotificationRequestValidator.validateIds(null));
        }

        @Test
        void shouldNotThrowIfValidId() {
            assertDoesNotThrow(() -> sendNotificationRequestValidator.validateIds(List.of("ID")));
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
                () -> sendNotificationRequestValidator.validateDate(start, end, limitInterval, limitBack));
        }

        @Test
        void shouldNotThrowExceptionIfStartDateIsAfterLimit() {
            final var end = LocalDateTime.now().minusDays(1);
            final var start = LocalDateTime.now().minusDays(2);
            final var limitBack = 4;
            final var limitInterval = 10;

            assertDoesNotThrow(() -> sendNotificationRequestValidator.validateDate(start, end, limitInterval, limitBack));
        }

        @Test
        void shouldThrowExceptionIfEndIsAfterStart() {
            final var end = LocalDateTime.now().minusDays(1);
            final var start = LocalDateTime.now();
            final var limitBack = 4;
            final var limitInterval = 10;

            assertThrows(IllegalArgumentException.class,
                () -> sendNotificationRequestValidator.validateDate(start, end, limitInterval, limitBack));
        }

        @Test
        @Disabled
        void shouldThrowExceptionIfIntervalIsOverLimit() {
            final var end = LocalDateTime.now();
            final var start = LocalDateTime.now().minusDays(11);
            final var limitBack = 20;
            final var limitInterval = 10;

            assertThrows(IllegalArgumentException.class,
                () -> sendNotificationRequestValidator.validateDate(start, end, limitInterval, limitBack));
        }

        @Test
        void shouldThrowExceptionIfIntervalIsOverLimitWhenEndIsNull() {
            final var start = LocalDateTime.now().minusDays(11);
            final var limitBack = 20;
            final var limitInterval = 10;

            assertThrows(IllegalArgumentException.class,
                () -> sendNotificationRequestValidator.validateDate(start, null, limitInterval, limitBack));
        }
    }

    @Nested
    class ValidateDateWithIntervalLimit {

        @Test
        void shouldThrowExceptionIfStartDateIsBeforeLimit() {
            final var end = LocalDateTime.now().minusDays(4);
            final var start = LocalDateTime.now().minusDays(5);
            final var limit = 4;

            assertThrows(IllegalArgumentException.class, () -> sendNotificationRequestValidator.validateDate(start, end, limit));
        }

        @Test
        void shouldThrowExceptionIfEndIsAfterStart() {
            final var end = LocalDateTime.now().minusDays(1);
            final var start = LocalDateTime.now();
            final var limit = 4;

            assertThrows(IllegalArgumentException.class, () -> sendNotificationRequestValidator.validateDate(start, end, limit));
        }

        @Test
        void shouldNotThrowExceptionIfStartDateIsAfterLimit() {
            final var end = LocalDateTime.now().minusDays(1);
            final var start = LocalDateTime.now().minusDays(2);
            final var limit = 4;

            assertDoesNotThrow(() -> sendNotificationRequestValidator.validateDate(start, end, limit));
        }
    }

    @Nested
    class ValidateCertificateIds {

        @Test
        void shouldThrowExceptionIfIdsAreEmpty() {
            final var ids = new ArrayList<String>();
            assertThrows(IllegalArgumentException.class, () -> sendNotificationRequestValidator.validateCertificateIds(ids));
        }

        @Test
        void shouldThrowExceptionIfIdsAreNull() {
            assertThrows(IllegalArgumentException.class, () -> sendNotificationRequestValidator.validateCertificateIds(null));
        }

        @Test
        void shouldThrowExceptionIfIdHasIncorrectFormat() {
            final var ids = List.of(
                "wrong-a0ff-4f99-ab29-d45a1e5fc9a4",
                "941dd794-9cba-4cb8-95e6-ac7da48c07c7",
                "9565ca3c-bd09-4fb3-8fcf-e5043959dfe5"
            );
            assertThrows(IllegalArgumentException.class, () -> sendNotificationRequestValidator.validateCertificateIds(ids));
        }

        @Test
        void shouldThrowExceptionIfIdIsEmpty() {
            final var ids = List.of(
                "",
                "941dd794-9cba-4cb8-95e6-ac7da48c07c7",
                "9565ca3c-qd09-4fz3-8fyf-e5043959dfe5"
            );
            assertThrows(IllegalArgumentException.class, () -> sendNotificationRequestValidator.validateCertificateIds(ids));
        }

        @Test
        void shouldNotThrowIfIdHasCorrectFormat() {
            final var ids = List.of(
                "3c6f9296-a0ff-4f99-ab29-d45a1e5fc9a4",
                "941dd794-9cba-4cb8-95e6-ac7da48c07c7",
                "9565ca3c-qd09-4fz3-8fyf-e5043959dfe5"
            );
            assertDoesNotThrow(() -> sendNotificationRequestValidator.validateCertificateIds(ids));
        }
    }
}
