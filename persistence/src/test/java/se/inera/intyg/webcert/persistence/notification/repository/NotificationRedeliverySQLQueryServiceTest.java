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
class NotificationRedeliverySQLQueryServiceTest {

    @InjectMocks
    private NotificationRedeliverySQLQueryService notificationRedeliverySQLQueryService;

    @Nested
    class SendNotificationForCertificates {

        @Test
        void shouldReturnCorrectQueryIncludingOnlyId() {
            final var response = notificationRedeliverySQLQueryService.certificates(List.of("ID"), Collections.emptyList(), null, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H WHERE H.INTYGS_ID in ('ID') ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingSeveralIds() {
            final var response = notificationRedeliverySQLQueryService.certificates(List.of("ID", "123"), Collections.emptyList(), null,
                null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H WHERE H.INTYGS_ID in ('ID','123') ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingIdAndStatus() {
            final var response = notificationRedeliverySQLQueryService.certificates(List.of("ID"),
                List.of(NotificationDeliveryStatusEnum.FAILURE), null, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.INTYGS_ID in ('ID')"
                    + " AND HM.DELIVERY_STATUS in ('FAILURE') ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingIdStatusAndTimePeriod() {
            final var start = LocalDateTime.now().minusDays(1);
            final var end = LocalDateTime.now();
            final var response = notificationRedeliverySQLQueryService.certificates(List.of("ID"),
                List.of(NotificationDeliveryStatusEnum.FAILURE), start, end);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.INTYGS_ID in ('ID')"
                    + " AND HM.DELIVERY_STATUS in ('FAILURE')"
                    + " AND H.TIMESTAMP BETWEEN '" + start + "' AND '" + end + "' ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnNowIfEndIsMissing() {
            final var start = LocalDateTime.now().minusDays(1);
            final var response = notificationRedeliverySQLQueryService.certificates(List.of("ID"),
                List.of(NotificationDeliveryStatusEnum.FAILURE), start, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.INTYGS_ID in ('ID')"
                    + " AND HM.DELIVERY_STATUS in ('FAILURE')"
                    + " AND H.TIMESTAMP BETWEEN '" + start + "' AND now() ORDER BY H.TIMESTAMP;",
                response);
        }
    }

    @Nested
    class SendNotificationForUnits {

        @Test
        void shouldReturnCorrectQueryIncludingOnlyId() {
            final var response = notificationRedeliverySQLQueryService.units(List.of("ID"), Collections.emptyList(), null, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H WHERE H.ENHETS_ID in ('ID') ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingSeveralIds() {
            final var response = notificationRedeliverySQLQueryService.units(List.of("ID", "123"), Collections.emptyList(), null,
                null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H WHERE H.ENHETS_ID in ('ID','123') ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingIdAndStatus() {
            final var response = notificationRedeliverySQLQueryService.units(List.of("ID"),
                List.of(NotificationDeliveryStatusEnum.FAILURE), null, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.ENHETS_ID in ('ID')"
                    + " AND HM.DELIVERY_STATUS in ('FAILURE') ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingIdStatusAndTimePeriod() {
            final var start = LocalDateTime.now().minusDays(1);
            final var end = LocalDateTime.now();
            final var response = notificationRedeliverySQLQueryService.units(List.of("ID"),
                List.of(NotificationDeliveryStatusEnum.FAILURE), start, end);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.ENHETS_ID in ('ID')"
                    + " AND HM.DELIVERY_STATUS in ('FAILURE')"
                    + " AND H.TIMESTAMP BETWEEN '" + start + "' AND '" + end + "' ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnNowIfEndIsMissing() {
            final var start = LocalDateTime.now().minusDays(1);
            final var response = notificationRedeliverySQLQueryService.certificates(List.of("ID"),
                List.of(NotificationDeliveryStatusEnum.FAILURE), start, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.INTYGS_ID in ('ID')"
                    + " AND HM.DELIVERY_STATUS in ('FAILURE')"
                    + " AND H.TIMESTAMP BETWEEN '" + start + "' AND now() ORDER BY H.TIMESTAMP;",
                response);
        }
    }

    @Nested
    class SendNotificationForTimePeriod {

        @Test
        void shouldReturnCorrectQueryIncludingOnlyStart() {
            final var start = LocalDateTime.now().minusDays(1);
            final var response = notificationRedeliverySQLQueryService.timePeriod(Collections.emptyList(), start, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H WHERE H.TIMESTAMP BETWEEN '" + start
                    + "' AND now() ORDER BY H.TIMESTAMP;", response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingStartAndEnd() {
            final var start = LocalDateTime.now().minusDays(1);
            final var end = LocalDateTime.now();
            final var response = notificationRedeliverySQLQueryService.timePeriod(Collections.emptyList(), start, end);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H WHERE "
                    + "H.TIMESTAMP BETWEEN '" + start + "' AND '" + end + "' ORDER BY H.TIMESTAMP;", response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingTimePeriodAndStatus() {
            final var start = LocalDateTime.now().minusDays(1);
            final var end = LocalDateTime.now();
            final var response = notificationRedeliverySQLQueryService.timePeriod(List.of(NotificationDeliveryStatusEnum.FAILURE), start,
                end);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE HM.DELIVERY_STATUS in ('FAILURE')"
                    + " AND H.TIMESTAMP BETWEEN '" + start + "' AND '" + end
                    + "' ORDER BY H.TIMESTAMP;", response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingIdStatusAndStartOnly() {
            final var start = LocalDateTime.now().minusDays(1);
            final var response = notificationRedeliverySQLQueryService.timePeriod(List.of(NotificationDeliveryStatusEnum.FAILURE), start,
                null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE"
                    + " HM.DELIVERY_STATUS in ('FAILURE')"
                    + " AND H.TIMESTAMP BETWEEN '" + start + "' AND now() ORDER BY H.TIMESTAMP;", response);
        }
    }

    @Nested
    class SendNotificationForCareGiver {

        @Test
        void shouldReturnCorrectQueryIncludingOnlyId() {
            final var response = notificationRedeliverySQLQueryService.careGiver("ID", Collections.emptyList(), null, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H WHERE H.ENHETS_ID LIKE 'ID-%' ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingIdAndStatus() {
            final var response = notificationRedeliverySQLQueryService.careGiver("ID",
                List.of(NotificationDeliveryStatusEnum.FAILURE), null, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.ENHETS_ID LIKE 'ID-%'"
                    + " AND HM.DELIVERY_STATUS in ('FAILURE') ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnCorrectQueryIncludingIdStatusAndTimePeriod() {
            final var start = LocalDateTime.now().minusDays(1);
            final var end = LocalDateTime.now();
            final var response = notificationRedeliverySQLQueryService.careGiver("ID",
                List.of(NotificationDeliveryStatusEnum.FAILURE), start, end);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.ENHETS_ID LIKE 'ID-%'"
                    + " AND HM.DELIVERY_STATUS in ('FAILURE')"
                    + " AND H.TIMESTAMP BETWEEN '" + start + "' AND '" + end + "' ORDER BY H.TIMESTAMP;",
                response);
        }

        @Test
        void shouldReturnNowIfEndIsMissing() {
            final var start = LocalDateTime.now().minusDays(1);
            final var response = notificationRedeliverySQLQueryService.careGiver("ID",
                List.of(NotificationDeliveryStatusEnum.FAILURE), start, null);

            assertEquals(
                "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) SELECT H.ID,"
                    + " 'STANDARD', now() FROM HANDELSE H"
                    + " INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID"
                    + " WHERE H.ENHETS_ID LIKE 'ID-%'"
                    + " AND HM.DELIVERY_STATUS in ('FAILURE')"
                    + " AND H.TIMESTAMP BETWEEN '" + start + "' AND now() ORDER BY H.TIMESTAMP;",
                response);
        }
    }

    @Nested
    class SendNotification {

        @Test
        void shouldReturnCorrectQueryIncludingNotificationId() {
            final var response = notificationRedeliverySQLQueryService.notification("NOTIFICATION_ID");

            assertEquals("INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) "
                + "VALUES(NOTIFICATION_ID, 'STANDARD', now());", response);
        }
    }
}