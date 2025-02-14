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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CountNotificationsForCareGiverRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCareGiverRequestDTO;

@ExtendWith(MockitoExtension.class)
class SendNotificationsForCareGiverServiceTest {

    private static final Integer COUNT = 10;
    private static final String ID = "ID";
    private static final int LIMIT = 5;
    private static final int LIMIT_INTERVAL = 10;
    private static final List<NotificationDeliveryStatusEnum> STATUSES = List.of(
        NotificationDeliveryStatusEnum.FAILURE);
    private static final LocalDateTime START = LocalDateTime.now().minusDays(1);
    private static final LocalDateTime END = LocalDateTime.now();
    private static final LocalDateTime ACTIVATION_TIME = LocalDateTime.now();
    private static final SendNotificationsForCareGiverRequestDTO REQUEST = SendNotificationsForCareGiverRequestDTO.builder()
        .start(START)
        .end(END)
        .statuses(STATUSES)
        .activationTime(ACTIVATION_TIME)
        .build();
    private static final CountNotificationsForCareGiverRequestDTO COUNT_REQUEST = CountNotificationsForCareGiverRequestDTO.builder()
        .start(START)
        .end(END)
        .statuses(STATUSES)
        .build();
    @Mock
    SendNotificationCountValidator sendNotificationCountValidator;

    @Mock
    NotificationRedeliveryRepository notificationRedeliveryRepository;

    @Mock
    SendNotificationRequestValidator sendNotificationRequestValidator;

    @InjectMocks
    SendNotificationsForCareGiverService sendNotificationsForCareGiverService;

    @Nested
    class SendNotifications {

        @BeforeEach
        void setup() {
            ReflectionTestUtils.setField(sendNotificationsForCareGiverService, "maxDaysBackStartDate",
                LIMIT);
            ReflectionTestUtils.setField(sendNotificationsForCareGiverService, "maxTimeInterval",
                LIMIT_INTERVAL);
        }

        @Test
        void shouldThrowIfCountExceedLimit() {
            doThrow(IllegalArgumentException.class).when(sendNotificationCountValidator)
                .careGiver(ID, REQUEST);

            assertThrows(IllegalArgumentException.class, () -> sendNotificationsForCareGiverService.send(ID, REQUEST));
        }

        @Test
        void shouldReturnResponseFromRepository() {
            when(notificationRedeliveryRepository.sendNotificationsForCareGiver(ID, STATUSES, START, END,
                ACTIVATION_TIME))
                .thenReturn(COUNT);

            final var response = sendNotificationsForCareGiverService.send(ID, REQUEST);

            assertEquals(COUNT, response.getCount());
        }


        @Test
        void shouldValidateIds() {
            when(notificationRedeliveryRepository.sendNotificationsForCareGiver(ID, STATUSES, START, END,
                ACTIVATION_TIME))
                .thenReturn(COUNT);

            final var captor = ArgumentCaptor.forClass(String.class);
            sendNotificationsForCareGiverService.send(ID, REQUEST);

            verify(sendNotificationRequestValidator).validateId(captor.capture());

            assertEquals(ID, captor.getValue());
        }

        @Test
        void shouldValidateDateUsingStart() {
            when(notificationRedeliveryRepository.sendNotificationsForCareGiver(ID, STATUSES, START, END,
                ACTIVATION_TIME))
                .thenReturn(COUNT);

            final var captor = ArgumentCaptor.forClass(LocalDateTime.class);
            sendNotificationsForCareGiverService.send(ID, REQUEST);

            verify(sendNotificationRequestValidator).validateDate(captor.capture(),
                any(LocalDateTime.class), anyInt(), anyInt());

            assertEquals(REQUEST.getStart(), captor.getValue());
        }

        @Test
        void shouldValidateDateUsingEnd() {
            when(notificationRedeliveryRepository.sendNotificationsForCareGiver(ID, STATUSES, START, END,
                ACTIVATION_TIME))
                .thenReturn(COUNT);
            final var captor = ArgumentCaptor.forClass(LocalDateTime.class);
            sendNotificationsForCareGiverService.send(ID, REQUEST);

            verify(sendNotificationRequestValidator).validateDate(any(LocalDateTime.class),
                captor.capture(), anyInt(), anyInt());

            assertEquals(REQUEST.getEnd(), captor.getValue());
        }

        @Test
        void shouldValidateDateUsingDaysBackLimit() {
            when(notificationRedeliveryRepository.sendNotificationsForCareGiver(ID, STATUSES, START, END,
                ACTIVATION_TIME))
                .thenReturn(COUNT);
            final var captor = ArgumentCaptor.forClass(int.class);
            sendNotificationsForCareGiverService.send(ID, REQUEST);

            verify(sendNotificationRequestValidator).validateDate(any(LocalDateTime.class),
                any(LocalDateTime.class), anyInt(),
                captor.capture());

            assertEquals(LIMIT, captor.getValue());
        }

        @Test
        void shouldValidateDateUsingIntervalLimit() {
            when(notificationRedeliveryRepository.sendNotificationsForCareGiver(ID, STATUSES, START, END,
                ACTIVATION_TIME))
                .thenReturn(COUNT);
            final var captor = ArgumentCaptor.forClass(int.class);
            sendNotificationsForCareGiverService.send(ID, REQUEST);

            verify(sendNotificationRequestValidator).validateDate(any(LocalDateTime.class),
                any(LocalDateTime.class), captor.capture(),
                anyInt());

            assertEquals(LIMIT_INTERVAL, captor.getValue());
        }

    }

    @Nested
    class CountNotifications {

        @BeforeEach
        void setup() {
            when(
                notificationRedeliveryRepository.countNotificationsForCareGiver(ID, STATUSES, START, END))
                .thenReturn(COUNT);
        }

        @Test
        void shouldReturnResponseFromRepository() {
            final var response = sendNotificationsForCareGiverService.count(ID, COUNT_REQUEST);

            assertEquals(COUNT, response.getCount());
        }

        @Test
        void shouldValidateIds() {
            final var captor = ArgumentCaptor.forClass(String.class);
            sendNotificationsForCareGiverService.count(ID, COUNT_REQUEST);

            verify(sendNotificationRequestValidator).validateId(captor.capture());

            assertEquals(ID, captor.getValue());
        }

        @Test
        void shouldValidateDateUsingStart() {
            final var captor = ArgumentCaptor.forClass(LocalDateTime.class);
            sendNotificationsForCareGiverService.count(ID, COUNT_REQUEST);

            verify(sendNotificationRequestValidator).validateDate(captor.capture(),
                any(LocalDateTime.class), anyInt(), anyInt());

            assertEquals(REQUEST.getStart(), captor.getValue());
        }

        @Test
        void shouldValidateDateUsingEnd() {
            final var captor = ArgumentCaptor.forClass(LocalDateTime.class);
            sendNotificationsForCareGiverService.count(ID, COUNT_REQUEST);

            verify(sendNotificationRequestValidator).validateDate(any(LocalDateTime.class),
                captor.capture(), anyInt(), anyInt());

            assertEquals(REQUEST.getEnd(), captor.getValue());
        }
    }
}