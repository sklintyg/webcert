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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepositoryCustom;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForTimePeriodRequestDTO;

@ExtendWith(MockitoExtension.class)
class SendNotificationsForTimePeriodServiceTest {

    private static final Integer COUNT = 10;
    private static final List<NotificationDeliveryStatusEnum> STATUSES = List.of(NotificationDeliveryStatusEnum.FAILURE);
    private static final LocalDateTime START = LocalDateTime.now().minusDays(1);
    private static final LocalDateTime END = LocalDateTime.now();
    private static final LocalDateTime ACTIVATION_TIME = LocalDateTime.now();
    private static final SendNotificationsForTimePeriodRequestDTO REQUEST = SendNotificationsForTimePeriodRequestDTO.create(
        STATUSES, ACTIVATION_TIME, START, END);
    private static final int LIMIT = 4;
    private static final int LIMIT_INTERVAL = 14;

    @Mock
    NotificationRedeliveryRepositoryCustom notificationRedeliveryRepositoryCustom;

    @Mock
    SendNotificationRequestValidation sendNotificationRequestValidation;

    @InjectMocks
    SendNotificationsForTimePeriodService sendNotificationsForTimePeriodService;

    @BeforeEach
    void setup() {
        when(notificationRedeliveryRepositoryCustom.sendNotificationsForTimePeriod(STATUSES, START, END, ACTIVATION_TIME))
            .thenReturn(COUNT);

        ReflectionTestUtils.setField(sendNotificationsForTimePeriodService, "maxDaysBackStartDate", LIMIT);
        ReflectionTestUtils.setField(sendNotificationsForTimePeriodService, "maxTimeInterval", LIMIT_INTERVAL);
    }

    @Test
    void shouldReturnResponseFromRepository() {
        final var response = sendNotificationsForTimePeriodService.send(REQUEST);

        assertEquals(COUNT, response.getCount());
    }

    @Test
    void shouldValidateDateUsingStart() {
        final var captor = ArgumentCaptor.forClass(LocalDateTime.class);
        sendNotificationsForTimePeriodService.send(REQUEST);

        verify(sendNotificationRequestValidation).validateDate(captor.capture(), any(LocalDateTime.class), anyInt(), anyInt());

        assertEquals(REQUEST.getStart(), captor.getValue());
    }

    @Test
    void shouldValidateDateUsingEnd() {
        final var captor = ArgumentCaptor.forClass(LocalDateTime.class);
        sendNotificationsForTimePeriodService.send(REQUEST);

        verify(sendNotificationRequestValidation).validateDate(any(LocalDateTime.class), captor.capture(), anyInt(), anyInt());

        assertEquals(REQUEST.getEnd(), captor.getValue());
    }

    @Test
    void shouldValidateDateUsingDaysBackLimit() {
        final var captor = ArgumentCaptor.forClass(int.class);
        sendNotificationsForTimePeriodService.send(REQUEST);

        verify(sendNotificationRequestValidation).validateDate(any(LocalDateTime.class), any(LocalDateTime.class), anyInt(),
            captor.capture());

        assertEquals(LIMIT, captor.getValue());
    }

    @Test
    void shouldValidateDateUsingIntervalLimit() {
        final var captor = ArgumentCaptor.forClass(int.class);
        sendNotificationsForTimePeriodService.send(REQUEST);

        verify(sendNotificationRequestValidation).validateDate(any(LocalDateTime.class), any(LocalDateTime.class), captor.capture(),
            anyInt());

        assertEquals(LIMIT_INTERVAL, captor.getValue());
    }
}