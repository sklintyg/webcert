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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CountNotificationsForCertificatesRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCertificatesRequestDTO;

@ExtendWith(MockitoExtension.class)
class SendNotificationsForCertificatesServiceTest {

    private static final Integer COUNT = 10;
    private static final List<String> IDS = List.of("ID ID");
    private static final List<String> SANITIZED_IDS = List.of("IDID");
    private static final List<NotificationDeliveryStatusEnum> STATUSES = List.of(NotificationDeliveryStatusEnum.FAILURE);
    private static final List<String> STATUS_LIST = List.of("FAILURE");
    private static final SendNotificationsForCertificatesRequestDTO REQUEST = SendNotificationsForCertificatesRequestDTO.builder()
        .certificateIds(IDS)
        .statuses(STATUSES)
        .build();
    private static final SendNotificationsForCertificatesRequestDTO SANITIZED_REQUEST = SendNotificationsForCertificatesRequestDTO.builder()
        .certificateIds(SANITIZED_IDS)
        .statuses(STATUSES)
        .build();
    private static final CountNotificationsForCertificatesRequestDTO COUNT_REQUEST = CountNotificationsForCertificatesRequestDTO.builder()
        .certificateIds(IDS)
        .statuses(STATUSES)
        .build();


    @Mock
    SendNotificationCountValidator sendNotificationCountValidator;
    @Mock
    NotificationRedeliveryRepository notificationRedeliveryRepository;

    @Mock
    SendNotificationRequestValidator sendNotificationRequestValidator;

    @InjectMocks
    SendNotificationsForCertificatesService sendNotificationsForCertificatesService;

    @Mock
    HandelseRepository handelseRepository;
    
    @Test
    void shouldThrowIfCountExceedLimit() {
        doThrow(IllegalArgumentException.class).when(sendNotificationCountValidator)
            .certificates(SANITIZED_REQUEST);

        assertThrows(IllegalArgumentException.class, () -> sendNotificationsForCertificatesService.send(REQUEST));
    }

    @Nested
    class SendTests {

        @BeforeEach
        void setup() {
            when(notificationRedeliveryRepository.sendNotificationsForCertificates(SANITIZED_IDS, STATUS_LIST))
                .thenReturn(COUNT);
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

            verify(sendNotificationRequestValidator).validateCertificateIds(captor.capture());

            assertEquals(SANITIZED_IDS, captor.getValue());
        }
    }

    @Nested
    class CountNotifications {

        @BeforeEach
        void setup() {
            when(
                handelseRepository.countNotificationsForCertificates(SANITIZED_IDS, STATUSES))
                .thenReturn(COUNT);
        }

        @Test
        void shouldReturnResponseFromRepository() {
            final var response = sendNotificationsForCertificatesService.count(COUNT_REQUEST);

            assertEquals(COUNT, response.getCount());
        }

        @Test
        void shouldValidateIds() {
            final var captor = ArgumentCaptor.forClass(List.class);
            sendNotificationsForCertificatesService.count(COUNT_REQUEST);

            verify(sendNotificationRequestValidator).validateCertificateIds(captor.capture());

            assertEquals(SANITIZED_IDS, captor.getValue());
        }

    }
}
