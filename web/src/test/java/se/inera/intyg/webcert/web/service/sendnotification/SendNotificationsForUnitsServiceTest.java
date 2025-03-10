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
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CountNotificationsForUnitsRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForUnitsRequestDTO;

@ExtendWith(MockitoExtension.class)
class SendNotificationsForUnitsServiceTest {

    private static final Integer COUNT = 10;
    private static final Integer LIMIT = 5;
    private static final Integer LIMIT_INTERVAL = 10;
    private static final List<String> IDS = List.of("ID ID");
    private static final List<String> SANITIZED_IDS = List.of("IDID");
    private static final List<NotificationDeliveryStatusEnum> STATUSES = List.of(NotificationDeliveryStatusEnum.FAILURE);
    private static final List<String> STATUS_LIST = List.of("FAILURE");
    private static final LocalDateTime START = LocalDateTime.now().minusDays(1);
    private static final LocalDateTime END = LocalDateTime.now();
    private static final LocalDateTime ACTIVATION_TIME = LocalDateTime.now();
    private static final SendNotificationsForUnitsRequestDTO REQUEST = SendNotificationsForUnitsRequestDTO.builder()
        .unitIds(IDS)
        .statuses(STATUSES)
        .start(START)
        .end(END)
        .activationTime(ACTIVATION_TIME)
        .build();
    private static final SendNotificationsForUnitsRequestDTO SANITIZED_REQUEST = SendNotificationsForUnitsRequestDTO.builder()
        .unitIds(SANITIZED_IDS)
        .statuses(STATUSES)
        .start(START)
        .end(END)
        .activationTime(ACTIVATION_TIME)
        .build();
    private static final CountNotificationsForUnitsRequestDTO COUNT_REQUEST = CountNotificationsForUnitsRequestDTO.builder()
        .start(START)
        .end(END)
        .unitIds(IDS)
        .activationTime(ACTIVATION_TIME)
        .statuses(STATUSES)
        .build();

    @Mock
    SendNotificationCountValidator sendNotificationCountValidator;

    @Mock
    NotificationRedeliveryRepository notificationRedeliveryRepository;

    @Mock
    SendNotificationRequestValidator sendNotificationRequestValidator;

    @InjectMocks
    SendNotificationsForUnitsService sendNotificationsForUnitsService;

    @Mock
    HandelseRepository handelseRepository;

    @Test
    void shouldThrowIfCountExceedLimit() {
        doThrow(IllegalArgumentException.class).when(sendNotificationCountValidator)
            .units(SANITIZED_REQUEST);

        assertThrows(IllegalArgumentException.class, () -> sendNotificationsForUnitsService.send(REQUEST));
    }

    @Nested
    class SendTests {

        @BeforeEach
        void setup() {
            when(notificationRedeliveryRepository.sendNotificationsForUnits(SANITIZED_IDS, STATUS_LIST, START, END, ACTIVATION_TIME))
                .thenReturn(COUNT);

            ReflectionTestUtils.setField(sendNotificationsForUnitsService, "maxDaysBackStartDate", LIMIT);
            ReflectionTestUtils.setField(sendNotificationsForUnitsService, "maxTimeInterval", LIMIT_INTERVAL);
        }

        @Test
        void shouldReturnResponseFromRepository() {
            final var response = sendNotificationsForUnitsService.send(REQUEST);

            assertEquals(COUNT, response.getCount());
        }

        @Test
        void shouldValidateIds() {
            final var captor = ArgumentCaptor.forClass(List.class);
            sendNotificationsForUnitsService.send(REQUEST);

            verify(sendNotificationRequestValidator).validateIds(captor.capture());

            assertEquals(SANITIZED_IDS, captor.getValue());
        }

        @Test
        void shouldValidateDateUsingStart() {
            final var captor = ArgumentCaptor.forClass(LocalDateTime.class);
            sendNotificationsForUnitsService.send(REQUEST);

            verify(sendNotificationRequestValidator).validateDate(captor.capture(), any(LocalDateTime.class), anyInt(), anyInt());

            assertEquals(REQUEST.getStart(), captor.getValue());
        }

        @Test
        void shouldValidateDateUsingEnd() {
            final var captor = ArgumentCaptor.forClass(LocalDateTime.class);
            sendNotificationsForUnitsService.send(REQUEST);

            verify(sendNotificationRequestValidator).validateDate(any(LocalDateTime.class), captor.capture(), anyInt(), anyInt());

            assertEquals(REQUEST.getEnd(), captor.getValue());
        }

        @Test
        void shouldValidateDateUsingDaysBackLimit() {
            final var captor = ArgumentCaptor.forClass(int.class);
            sendNotificationsForUnitsService.send(REQUEST);

            verify(sendNotificationRequestValidator).validateDate(any(LocalDateTime.class), any(LocalDateTime.class), anyInt(),
                captor.capture());

            assertEquals(LIMIT, captor.getValue());
        }

        @Test
        void shouldValidateDateUsingIntervalLimit() {
            final var captor = ArgumentCaptor.forClass(int.class);
            sendNotificationsForUnitsService.send(REQUEST);

            verify(sendNotificationRequestValidator).validateDate(any(LocalDateTime.class), any(LocalDateTime.class), captor.capture(),
                anyInt());

            assertEquals(LIMIT_INTERVAL, captor.getValue());
        }
    }

    @Nested
    class CountNotifications {

        @BeforeEach
        void setup() {
            when(
                handelseRepository.countNotificationsForUnits(SANITIZED_IDS, List.of("FAILURE"), START, END))
                .thenReturn(COUNT);
        }

        @Test
        void shouldReturnResponseFromRepository() {
            final var response = sendNotificationsForUnitsService.count(COUNT_REQUEST);

            assertEquals(COUNT, response.getCount());
        }

        @Test
        void shouldValidateIds() {
            final var captor = ArgumentCaptor.forClass(List.class);
            sendNotificationsForUnitsService.count(COUNT_REQUEST);

            verify(sendNotificationRequestValidator).validateIds(captor.capture());

            assertEquals(SANITIZED_IDS, captor.getValue());
        }

        @Test
        void shouldValidateDateUsingStart() {
            final var captor = ArgumentCaptor.forClass(LocalDateTime.class);
            sendNotificationsForUnitsService.count(COUNT_REQUEST);

            verify(sendNotificationRequestValidator).validateDate(captor.capture(),
                any(LocalDateTime.class), anyInt(), anyInt());

            assertEquals(REQUEST.getStart(), captor.getValue());
        }

        @Test
        void shouldValidateDateUsingEnd() {
            final var captor = ArgumentCaptor.forClass(LocalDateTime.class);
            sendNotificationsForUnitsService.count(COUNT_REQUEST);

            verify(sendNotificationRequestValidator).validateDate(any(LocalDateTime.class),
                captor.capture(), anyInt(), anyInt());

            assertEquals(REQUEST.getEnd(), captor.getValue());
        }
    }
}
