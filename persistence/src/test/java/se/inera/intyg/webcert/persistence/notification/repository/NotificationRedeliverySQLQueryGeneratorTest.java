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

package se.inera.intyg.webcert.persistence.notification.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;

@ExtendWith(MockitoExtension.class)
class NotificationRedeliverySQLQueryGeneratorTest {

    @InjectMocks
    private NotificationRedeliverySQLQueryGenerator notificationRedeliverySQLQueryGenerator;

    @Nested
    class SendNotificationForCertificates {

        @Test
        void shouldReturnCorrectQueryIncludingOnlyId() {
            final var response = notificationRedeliverySQLQueryGenerator.certificates(Collections.emptyList(), null, null, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H WHERE H.INTYGS_ID in :id ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingSeveralIds() {
            final var response = notificationRedeliverySQLQueryGenerator.certificates(Collections.emptyList(), null,
                null, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H WHERE H.INTYGS_ID in :id ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingIdAndStatus() {
            final var response = notificationRedeliverySQLQueryGenerator.certificates(List.of(NotificationDeliveryStatusEnum.FAILURE), null,
                null, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.INTYGS_ID in :id"
                    + " AND HM.DELIVERY_STATUS in :status ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingIdStatusAndTimePeriod() {
            final var start = LocalDateTime.now().minusDays(1);
            final var end = LocalDateTime.now();
            final var response = notificationRedeliverySQLQueryGenerator.certificates(List.of(NotificationDeliveryStatusEnum.FAILURE),
                start, end, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.INTYGS_ID in :id"
                    + " AND HM.DELIVERY_STATUS in :status"
                    + " AND H.TIMESTAMP BETWEEN :start AND :end ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnNowIfEndIsMissing() {
            final var start = LocalDateTime.now().minusDays(1);
            final var response = notificationRedeliverySQLQueryGenerator.certificates(List.of(NotificationDeliveryStatusEnum.FAILURE),
                start, null, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.INTYGS_ID in :id"
                    + " AND HM.DELIVERY_STATUS in :status"
                    + " AND H.TIMESTAMP BETWEEN :start AND now() ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldIncludeActivationTime() {
            final var start = LocalDateTime.now().minusDays(1);
            final var response = notificationRedeliverySQLQueryGenerator.certificates(List.of(NotificationDeliveryStatusEnum.FAILURE),
                start, null, LocalDateTime.now());

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', :activationTime FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.INTYGS_ID in :id"
                    + " AND HM.DELIVERY_STATUS in :status"
                    + " AND H.TIMESTAMP BETWEEN :start AND now() ORDER BY H.TIMESTAMP;",
                response);
        }
    }

    @Nested
    class SendNotificationForUnits {

        @Test
        void shouldReturnCorrectQueryIncludingOnlyId() {
            final var response = notificationRedeliverySQLQueryGenerator.units(Collections.emptyList(), null, null, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H WHERE H.ENHETS_ID in :id ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingSeveralIds() {
            final var response = notificationRedeliverySQLQueryGenerator.units(Collections.emptyList(), null, null, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H WHERE H.ENHETS_ID in :id ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingIdAndStatus() {
            final var response = notificationRedeliverySQLQueryGenerator.units(List.of(NotificationDeliveryStatusEnum.FAILURE), null, null,
                null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.ENHETS_ID in :id"
                    + " AND HM.DELIVERY_STATUS in :status ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingIdStatusAndTimePeriod() {
            final var start = LocalDateTime.now().minusDays(1);
            final var end = LocalDateTime.now();
            final var response = notificationRedeliverySQLQueryGenerator.units(List.of(NotificationDeliveryStatusEnum.FAILURE), start, end,
                null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.ENHETS_ID in :id"
                    + " AND HM.DELIVERY_STATUS in :status"
                    + " AND H.TIMESTAMP BETWEEN :start AND :end ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnNowIfEndIsMissing() {
            final var start = LocalDateTime.now().minusDays(1);
            final var response = notificationRedeliverySQLQueryGenerator.certificates(List.of(NotificationDeliveryStatusEnum.FAILURE),
                start, null, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.INTYGS_ID in :id"
                    + " AND HM.DELIVERY_STATUS in :status"
                    + " AND H.TIMESTAMP BETWEEN :start AND now() ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldIncludeActivationTime() {
            final var start = LocalDateTime.now().minusDays(1);
            final var response = notificationRedeliverySQLQueryGenerator.certificates(List.of(NotificationDeliveryStatusEnum.FAILURE),
                start, null, LocalDateTime.now());

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', :activationTime FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.INTYGS_ID in :id"
                    + " AND HM.DELIVERY_STATUS in :status"
                    + " AND H.TIMESTAMP BETWEEN :start AND now() ORDER BY H.TIMESTAMP;",
                response);
        }
    }

    @Nested
    class SendNotificationForTimePeriod {

        @Test
        void shouldReturnCorrectQueryIncludingOnlyStart() {
            final var start = LocalDateTime.now().minusDays(1);
            final var response = notificationRedeliverySQLQueryGenerator.timePeriod(Collections.emptyList(), start, null, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H WHERE H.TIMESTAMP BETWEEN :start"
                    + " AND now() ORDER BY H.TIMESTAMP;", response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingStartAndEnd() {
            final var start = LocalDateTime.now().minusDays(1);
            final var end = LocalDateTime.now();
            final var response = notificationRedeliverySQLQueryGenerator.timePeriod(Collections.emptyList(), start, end, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H WHERE "
                    + "H.TIMESTAMP BETWEEN :start AND :end ORDER BY H.TIMESTAMP;", response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingTimePeriodAndStatus() {
            final var start = LocalDateTime.now().minusDays(1);
            final var end = LocalDateTime.now();
            final var response = notificationRedeliverySQLQueryGenerator.timePeriod(List.of(NotificationDeliveryStatusEnum.FAILURE), start,
                end, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE HM.DELIVERY_STATUS in :status"
                    + " AND H.TIMESTAMP BETWEEN :start AND :end"
                    + " ORDER BY H.TIMESTAMP;", response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingIdStatusAndStartOnly() {
            final var start = LocalDateTime.now().minusDays(1);
            final var response = notificationRedeliverySQLQueryGenerator.timePeriod(List.of(NotificationDeliveryStatusEnum.FAILURE), start,
                null, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE"
                    + " HM.DELIVERY_STATUS in :status"
                    + " AND H.TIMESTAMP BETWEEN :start AND now() ORDER BY H.TIMESTAMP;", response);
        }

        @Test
        void shouldIncludeActivationTime() {
            final var start = LocalDateTime.now().minusDays(1);
            final var response = notificationRedeliverySQLQueryGenerator.timePeriod(List.of(NotificationDeliveryStatusEnum.FAILURE), start,
                null, LocalDateTime.now());

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', :activationTime FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE"
                    + " HM.DELIVERY_STATUS in :status"
                    + " AND H.TIMESTAMP BETWEEN :start AND now() ORDER BY H.TIMESTAMP;", response);
        }
    }

    @Nested
    class SendNotificationForCareGiver {

        @Test
        void shouldReturnCorrectQueryIncludingOnlyId() {
            final var response = notificationRedeliverySQLQueryGenerator.careGiver(Collections.emptyList(), null, null, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H WHERE H.ENHETS_ID LIKE :id ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingIdAndStatus() {
            final var response = notificationRedeliverySQLQueryGenerator.careGiver(List.of(NotificationDeliveryStatusEnum.FAILURE), null,
                null, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.ENHETS_ID LIKE :id"
                    + " AND HM.DELIVERY_STATUS in :status ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingIdStatusAndTimePeriod() {
            final var start = LocalDateTime.now().minusDays(1);
            final var end = LocalDateTime.now();
            final var response = notificationRedeliverySQLQueryGenerator.careGiver(List.of(NotificationDeliveryStatusEnum.FAILURE), start,
                end, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.ENHETS_ID LIKE :id"
                    + " AND HM.DELIVERY_STATUS in :status"
                    + " AND H.TIMESTAMP BETWEEN :start AND :end ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnNowIfEndIsMissing() {
            final var response = notificationRedeliverySQLQueryGenerator.careGiver(List.of(NotificationDeliveryStatusEnum.FAILURE),
                LocalDateTime.now(), null, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.ENHETS_ID LIKE :id"
                    + " AND HM.DELIVERY_STATUS in :status"
                    + " AND H.TIMESTAMP BETWEEN :start AND now() ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldIncludeActivationTime() {
            final var response = notificationRedeliverySQLQueryGenerator.careGiver(List.of(NotificationDeliveryStatusEnum.FAILURE),
                LocalDateTime.now(), null, LocalDateTime.now());

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', :activationTime FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.ENHETS_ID LIKE :id"
                    + " AND HM.DELIVERY_STATUS in :status"
                    + " AND H.TIMESTAMP BETWEEN :start AND now() ORDER BY H.TIMESTAMP;",
                response);
        }
    }

    @Nested
    class SendNotification {

        @Test
        void shouldReturnCorrectQueryIncludingNotificationId() {
            final var response = notificationRedeliverySQLQueryGenerator.notification();

            assertEquals("INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) "
                + "VALUES(:id, 'STANDARD', now());", response);
        }
    }
}