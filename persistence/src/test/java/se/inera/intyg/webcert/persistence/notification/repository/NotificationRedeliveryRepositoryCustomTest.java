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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliverySQLConstants.ACTIVATION_TIME;
import static se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliverySQLConstants.END;
import static se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliverySQLConstants.ID;
import static se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliverySQLConstants.START;
import static se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliverySQLConstants.STATUS;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;

@ExtendWith(MockitoExtension.class)
class NotificationRedeliveryRepositoryCustomTest {

    private static final String SQL = "SQL";
    private static final String PARAMETER_VALUE_ID = "P_ID";
    private static final String COUNT = "COUNT";
    private static final List<String> IDS = List.of("ID1", "ID2");
    private static final List<NotificationDeliveryStatusEnum> STATUS_VALUES = List.of(NotificationDeliveryStatusEnum.FAILURE);
    private static final LocalDateTime START_VALUE = LocalDateTime.now();
    private static final LocalDateTime END_VALUE = LocalDateTime.now();
    private static final LocalDateTime ACTIVATION = LocalDateTime.now();
    private static final BigInteger BIG_INT = BigInteger.TEN;
    private static Query query;

    @Mock
    NotificationRedeliverySQLQueryGenerator notificationRedeliverySQLQueryGenerator;

    @Mock
    EntityManager entityManager;

    @InjectMocks
    NotificationRedeliveryRepositoryCustom notificationRedeliveryRepositoryCustom;

    @BeforeEach
    void setup() {
        query = mock(Query.class);
        final var countQuery = mock(Query.class);

        when(notificationRedeliverySQLQueryGenerator.count())
            .thenReturn(COUNT);

        when(entityManager.createQuery(anyString()))
            .thenReturn(query);

        when(entityManager.createNativeQuery(anyString()))
            .thenReturn(countQuery);

        when(countQuery.getSingleResult())
            .thenReturn(BIG_INT);
    }

    @Nested
    class Notification {

        @BeforeEach
        void setup() {
            when(notificationRedeliverySQLQueryGenerator.notification())
                .thenReturn(SQL);
        }

        @Test
        void shouldUseSQLFromGenerator() {
            final var captor = ArgumentCaptor.forClass(String.class);
            notificationRedeliveryRepositoryCustom.sendNotification(PARAMETER_VALUE_ID);

            verify(entityManager).createQuery(captor.capture());
            assertEquals(SQL, captor.getValue());
        }

        @Test
        void shouldSetParameterIdValue() {
            final var captor = ArgumentCaptor.forClass(String.class);
            notificationRedeliveryRepositoryCustom.sendNotification(PARAMETER_VALUE_ID);

            verify(query).setParameter(anyString(), captor.capture());
            assertEquals(PARAMETER_VALUE_ID, captor.getValue());
        }

        @Test
        void shouldSetParameterIdName() {
            final var captor = ArgumentCaptor.forClass(String.class);
            notificationRedeliveryRepositoryCustom.sendNotification(PARAMETER_VALUE_ID);

            verify(query).setParameter(captor.capture(), anyString());
            assertEquals(ID, captor.getValue());
        }

        @Test
        void shouldReturnCountAsResult() {
            final var response = notificationRedeliveryRepositoryCustom.sendNotification(ID);

            assertEquals(10, response);
        }
    }

    @Nested
    class Certificates {

        @BeforeEach
        void setup() {
            when(notificationRedeliverySQLQueryGenerator
                .certificates(anyList(), any(LocalDateTime.class), any(LocalDateTime.class), any(LocalDateTime.class))
            ).thenReturn(SQL);
        }

        @Test
        void shouldUseSQLFromGenerator() {
            final var captor = ArgumentCaptor.forClass(String.class);

            notificationRedeliveryRepositoryCustom.sendNotificationsForCertificates(
                IDS, STATUS_VALUES, START_VALUE, END_VALUE, ACTIVATION
            );

            verify(entityManager).createQuery(captor.capture());
            assertEquals(SQL, captor.getValue());
        }

        @Test
        void shouldSetParameterValues() {
            final var captor = ArgumentCaptor.forClass(Object.class);

            notificationRedeliveryRepositoryCustom.sendNotificationsForCertificates(
                IDS, STATUS_VALUES, START_VALUE, END_VALUE, ACTIVATION
            );

            verify(query, times(5)).setParameter(anyString(), captor.capture());
            assertTrue(captor.getAllValues().contains(IDS));
            assertTrue(captor.getAllValues().contains(STATUS_VALUES));
            assertTrue(captor.getAllValues().contains(START_VALUE));
            assertTrue(captor.getAllValues().contains(END_VALUE));
            assertTrue(captor.getAllValues().contains(ACTIVATION));
        }

        @Test
        void shouldSetParameterNames() {
            final var captor = ArgumentCaptor.forClass(String.class);

            notificationRedeliveryRepositoryCustom.sendNotificationsForCertificates(
                IDS, STATUS_VALUES, START_VALUE, END_VALUE, ACTIVATION
            );

            verify(query, times(5)).setParameter(captor.capture(), any());
            assertTrue(captor.getAllValues().contains(ID));
            assertTrue(captor.getAllValues().contains(STATUS));
            assertTrue(captor.getAllValues().contains(START));
            assertTrue(captor.getAllValues().contains(END));
            assertTrue(captor.getAllValues().contains(ACTIVATION_TIME));
        }

        @Test
        void shouldReturnCountAsResult() {
            final var response = notificationRedeliveryRepositoryCustom.sendNotificationsForCertificates(
                IDS, STATUS_VALUES, START_VALUE, END_VALUE, ACTIVATION
            );

            assertEquals(10, response);
        }
    }

    @Nested
    class Units {

        @BeforeEach
        void setup() {
            when(notificationRedeliverySQLQueryGenerator
                .units(anyList(), any(LocalDateTime.class), any(LocalDateTime.class), any(LocalDateTime.class))
            ).thenReturn(SQL);
        }

        @Test
        void shouldUseSQLFromGenerator() {
            final var captor = ArgumentCaptor.forClass(String.class);

            notificationRedeliveryRepositoryCustom.sendNotificationsForUnits(
                IDS, STATUS_VALUES, START_VALUE, END_VALUE, ACTIVATION
            );

            verify(entityManager).createQuery(captor.capture());
            assertEquals(SQL, captor.getValue());
        }

        @Test
        void shouldSetParameterValues() {
            final var captor = ArgumentCaptor.forClass(Object.class);

            notificationRedeliveryRepositoryCustom.sendNotificationsForUnits(
                IDS, STATUS_VALUES, START_VALUE, END_VALUE, ACTIVATION
            );

            verify(query, times(5)).setParameter(anyString(), captor.capture());
            assertTrue(captor.getAllValues().contains(IDS));
            assertTrue(captor.getAllValues().contains(STATUS_VALUES));
            assertTrue(captor.getAllValues().contains(START_VALUE));
            assertTrue(captor.getAllValues().contains(END_VALUE));
            assertTrue(captor.getAllValues().contains(ACTIVATION));
        }

        @Test
        void shouldSetParameterNames() {
            final var captor = ArgumentCaptor.forClass(String.class);

            notificationRedeliveryRepositoryCustom.sendNotificationsForUnits(
                IDS, STATUS_VALUES, START_VALUE, END_VALUE, ACTIVATION
            );

            verify(query, times(5)).setParameter(captor.capture(), any());
            assertTrue(captor.getAllValues().contains(ID));
            assertTrue(captor.getAllValues().contains(STATUS));
            assertTrue(captor.getAllValues().contains(START));
            assertTrue(captor.getAllValues().contains(END));
            assertTrue(captor.getAllValues().contains(ACTIVATION_TIME));
        }

        @Test
        void shouldReturnCountAsResult() {
            final var response = notificationRedeliveryRepositoryCustom.sendNotificationsForUnits(
                IDS, STATUS_VALUES, START_VALUE, END_VALUE, ACTIVATION
            );

            assertEquals(10, response);
        }
    }

    @Nested
    class CareGiver {

        @BeforeEach
        void setup() {
            when(notificationRedeliverySQLQueryGenerator
                .careGiver(anyList(), any(LocalDateTime.class), any(LocalDateTime.class), any(LocalDateTime.class))
            ).thenReturn(SQL);
        }

        @Test
        void shouldUseSQLFromGenerator() {
            final var captor = ArgumentCaptor.forClass(String.class);

            notificationRedeliveryRepositoryCustom.sendNotificationsForCareGiver(
                ID, STATUS_VALUES, START_VALUE, END_VALUE, ACTIVATION
            );

            verify(entityManager).createQuery(captor.capture());
            assertEquals(SQL, captor.getValue());
        }

        @Test
        void shouldSetParameterValues() {
            final var captor = ArgumentCaptor.forClass(Object.class);

            notificationRedeliveryRepositoryCustom.sendNotificationsForCareGiver(
                ID, STATUS_VALUES, START_VALUE, END_VALUE, ACTIVATION
            );

            verify(query, times(5)).setParameter(anyString(), captor.capture());
            assertTrue(captor.getAllValues().contains(ID + "-%"));
            assertTrue(captor.getAllValues().contains(STATUS_VALUES));
            assertTrue(captor.getAllValues().contains(START_VALUE));
            assertTrue(captor.getAllValues().contains(END_VALUE));
            assertTrue(captor.getAllValues().contains(ACTIVATION));
        }

        @Test
        void shouldSetParameterNames() {
            final var captor = ArgumentCaptor.forClass(String.class);

            notificationRedeliveryRepositoryCustom.sendNotificationsForCareGiver(
                ID, STATUS_VALUES, START_VALUE, END_VALUE, ACTIVATION
            );

            verify(query, times(5)).setParameter(captor.capture(), any());
            assertTrue(captor.getAllValues().contains(ID));
            assertTrue(captor.getAllValues().contains(STATUS));
            assertTrue(captor.getAllValues().contains(START));
            assertTrue(captor.getAllValues().contains(END));
            assertTrue(captor.getAllValues().contains(ACTIVATION_TIME));
        }

        @Test
        void shouldReturnCountAsResult() {
            final var response = notificationRedeliveryRepositoryCustom.sendNotificationsForCareGiver(
                ID, STATUS_VALUES, START_VALUE, END_VALUE, ACTIVATION
            );

            assertEquals(10, response);
        }
    }

    @Nested
    class TimePeriod {

        @BeforeEach
        void setup() {
            when(notificationRedeliverySQLQueryGenerator
                .timePeriod(anyList(), any(LocalDateTime.class), any(LocalDateTime.class), any(LocalDateTime.class))
            ).thenReturn(SQL);
        }

        @Test
        void shouldUseSQLFromGenerator() {
            final var captor = ArgumentCaptor.forClass(String.class);

            notificationRedeliveryRepositoryCustom.sendNotificationsForTimePeriod(
                STATUS_VALUES, START_VALUE, END_VALUE, ACTIVATION
            );

            verify(entityManager).createQuery(captor.capture());
            assertEquals(SQL, captor.getValue());
        }

        @Test
        void shouldSetParameterValues() {
            final var captor = ArgumentCaptor.forClass(Object.class);

            notificationRedeliveryRepositoryCustom.sendNotificationsForTimePeriod(
                STATUS_VALUES, START_VALUE, END_VALUE, ACTIVATION
            );

            verify(query, times(4)).setParameter(anyString(), captor.capture());
            assertTrue(captor.getAllValues().contains(STATUS_VALUES));
            assertTrue(captor.getAllValues().contains(START_VALUE));
            assertTrue(captor.getAllValues().contains(END_VALUE));
            assertTrue(captor.getAllValues().contains(ACTIVATION));
        }

        @Test
        void shouldSetParameterNames() {
            final var captor = ArgumentCaptor.forClass(String.class);

            notificationRedeliveryRepositoryCustom.sendNotificationsForTimePeriod(
                STATUS_VALUES, START_VALUE, END_VALUE, ACTIVATION
            );

            verify(query, times(4)).setParameter(captor.capture(), any());
            assertTrue(captor.getAllValues().contains(STATUS));
            assertTrue(captor.getAllValues().contains(START));
            assertTrue(captor.getAllValues().contains(END));
            assertTrue(captor.getAllValues().contains(ACTIVATION_TIME));
        }

        @Test
        void shouldReturnCountAsResult() {
            final var response = notificationRedeliveryRepositoryCustom.sendNotificationsForTimePeriod(
                STATUS_VALUES, START_VALUE, END_VALUE, ACTIVATION
            );

            assertEquals(10, response);
        }
    }
}