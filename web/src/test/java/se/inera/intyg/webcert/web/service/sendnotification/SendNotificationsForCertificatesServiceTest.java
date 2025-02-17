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
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCertificatesRequestDTO;

@ExtendWith(MockitoExtension.class)
class SendNotificationsForCertificatesServiceTest {

    private static final Integer COUNT = 10;
    private static final Integer LIMIT = 30;
    private static final List<String> IDS = List.of("ID ID");
    private static final List<String> SANITIZED_IDS = List.of("IDID");
    private static final List<NotificationDeliveryStatusEnum> STATUSES = List.of(NotificationDeliveryStatusEnum.FAILURE);
    private static final LocalDateTime START = LocalDateTime.now().minusDays(1);
    private static final LocalDateTime END = LocalDateTime.now();
    private static final LocalDateTime ACTIVATION_TIME = LocalDateTime.now();
    private static final SendNotificationsForCertificatesRequestDTO REQUEST = SendNotificationsForCertificatesRequestDTO.builder()
        .certificateIds(IDS)
        .statuses(STATUSES)
        .start(START)
        .end(END)
        .activationTime(ACTIVATION_TIME)
        .build();
    private static final SendNotificationsForCertificatesRequestDTO SANITIZED_REQUEST = SendNotificationsForCertificatesRequestDTO.builder()
        .certificateIds(SANITIZED_IDS)
        .statuses(STATUSES)
        .start(START)
        .end(END)
        .activationTime(ACTIVATION_TIME)
        .build();

    @Mock
    SendNotificationCountValidator sendNotificationCountValidator;
    @Mock
    NotificationRedeliveryRepository notificationRedeliveryRepository;

    @Mock
    SendNotificationRequestValidator sendNotificationRequestValidator;

    @InjectMocks
    SendNotificationsForCertificatesService sendNotificationsForCertificatesService;
    
    @Test
    void shouldThrowIfCountExceedLimit() {
        doThrow(IllegalArgumentException.class).when(sendNotificationCountValidator)
            .certiticates(SANITIZED_REQUEST);

        assertThrows(IllegalArgumentException.class, () -> sendNotificationsForCertificatesService.send(REQUEST));
    }

    @Nested
    class SendTests {

        @BeforeEach
        void setup() {
            when(notificationRedeliveryRepository.sendNotificationsForCertificates(SANITIZED_IDS, STATUSES, START, END, ACTIVATION_TIME))
                .thenReturn(COUNT);

            ReflectionTestUtils.setField(sendNotificationsForCertificatesService, "maxDaysBackStartDate", LIMIT);
        }

        @Test
        void shouldReturnResponseFromRepository() {
            final var response = sendNotificationsForCertificatesService.send(REQUEST);

            assertEquals(COUNT, response.getCount());
        }

        @Test
        void shouldValidateIds() {
            final var captor = ArgumentCaptor.forClass(List.class);
            sendNotificationsForCertificatesService.send(REQUEST);

            verify(sendNotificationRequestValidator).validateIds(captor.capture());

            assertEquals(SANITIZED_IDS, captor.getValue());
        }

        @Test
        void shouldValidateDateUsingStart() {
            final var captor = ArgumentCaptor.forClass(LocalDateTime.class);
            sendNotificationsForCertificatesService.send(REQUEST);

            verify(sendNotificationRequestValidator).validateDate(captor.capture(), any(LocalDateTime.class), anyInt());

            assertEquals(REQUEST.getStart(), captor.getValue());
        }

        @Test
        void shouldValidateDateUsingEnd() {
            final var captor = ArgumentCaptor.forClass(LocalDateTime.class);
            sendNotificationsForCertificatesService.send(REQUEST);

            verify(sendNotificationRequestValidator).validateDate(any(LocalDateTime.class), captor.capture(), anyInt());

            assertEquals(REQUEST.getEnd(), captor.getValue());
        }

        @Test
        void shouldValidateDateUsingDaysBackLimit() {
            final var captor = ArgumentCaptor.forClass(int.class);
            sendNotificationsForCertificatesService.send(REQUEST);

            verify(sendNotificationRequestValidator).validateDate(any(LocalDateTime.class), any(LocalDateTime.class), captor.capture());

            assertEquals(LIMIT, captor.getValue());
        }
    }
}
