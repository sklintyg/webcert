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

package se.inera.intyg.webcert.web.web.controller.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.web.service.sendnotification.SendNotificationService;
import se.inera.intyg.webcert.web.service.sendnotification.SendNotificationsForCareGiverService;
import se.inera.intyg.webcert.web.service.sendnotification.SendNotificationsForCertificatesService;
import se.inera.intyg.webcert.web.service.sendnotification.SendNotificationsForUnitsService;
import se.inera.intyg.webcert.web.web.controller.internalapi.NotificationController;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CountNotificationResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CountNotificationsForCareGiverRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CountNotificationsForCertificatesRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CountNotificationsForUnitsRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCareGiverRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCertificatesRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForUnitsRequestDTO;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private static final String ID = "ID";
    private static final Integer COUNT = 2;
    private static final SendNotificationsForCertificatesRequestDTO CERTIFICATE_REQUEST =
        SendNotificationsForCertificatesRequestDTO.builder()
            .certificateIds(List.of(ID))
            .statuses(List.of(NotificationDeliveryStatusEnum.FAILURE))
            .build();
    private static final CountNotificationsForCertificatesRequestDTO CERTIFICATE_COUNT_REQUEST =
        CountNotificationsForCertificatesRequestDTO.builder()
            .certificateIds(List.of(ID))
            .statuses(List.of(NotificationDeliveryStatusEnum.FAILURE))
            .build();
    private static final SendNotificationsForUnitsRequestDTO UNITS_REQUEST = SendNotificationsForUnitsRequestDTO.builder()
        .unitIds(List.of(ID))
        .statuses(List.of(NotificationDeliveryStatusEnum.FAILURE))
        .activationTime(LocalDateTime.now())
        .start(LocalDateTime.now())
        .end(LocalDateTime.now())
        .build();
    private static final CountNotificationsForUnitsRequestDTO UNITS_COUNT_REQUEST = CountNotificationsForUnitsRequestDTO.builder()
        .unitIds(List.of(ID))
        .statuses(List.of(NotificationDeliveryStatusEnum.FAILURE))
        .activationTime(LocalDateTime.now())
        .start(LocalDateTime.now())
        .end(LocalDateTime.now())
        .build();
    private static final SendNotificationsForCareGiverRequestDTO CARE_GIVER_REQUEST = SendNotificationsForCareGiverRequestDTO.builder()
        .statuses(List.of(NotificationDeliveryStatusEnum.FAILURE))
        .activationTime(LocalDateTime.now())
        .start(LocalDateTime.now())
        .end(LocalDateTime.now())
        .build();
    private static final CountNotificationsForCareGiverRequestDTO CARE_GIVER_COUNT_REQUEST =
        CountNotificationsForCareGiverRequestDTO.builder()
            .statuses(List.of(NotificationDeliveryStatusEnum.FAILURE))
            .build();


    @Mock
    private SendNotificationService sendNotificationService;

    @Mock
    private SendNotificationsForCertificatesService sendNotificationsForCertificatesService;

    @Mock
    private SendNotificationsForUnitsService sendNotificationsForUnitsService;

    @Mock
    private SendNotificationsForCareGiverService sendNotificationsForCareGiverService;

    @InjectMocks
    private NotificationController notificationController;

    @Nested
    class SendNotificationsForCertificates {


        @Test
        void shouldReturnResponseFromService() {
            when(sendNotificationsForCertificatesService.send(CERTIFICATE_REQUEST))
                .thenReturn(
                    SendNotificationResponseDTO.builder()
                        .count(COUNT)
                        .build()
                );
            final var response = notificationController.sendNotificationsForCertificates(CERTIFICATE_REQUEST);

            assertEquals(COUNT, response.getCount());
        }

        @Test
        void shouldReturnResponseFromCount() {
            when(sendNotificationsForCertificatesService.count( CERTIFICATE_COUNT_REQUEST))
                .thenReturn(
                    CountNotificationResponseDTO.builder()
                        .count(COUNT)
                        .max(COUNT+1)
                        .build()
                );
            final var response = notificationController.countNotificationsForCertificates(CERTIFICATE_COUNT_REQUEST);

            assertEquals(COUNT, response.getCount());
            assertEquals(COUNT+1, response.getMax());
        }
    }

    @Nested
    class SendNotification {

        @BeforeEach
        void setup() {
            when(sendNotificationService.send(ID))
                .thenReturn(
                    SendNotificationResponseDTO.builder()
                        .count(COUNT)
                        .build()
                );
        }

        @Test
        void shouldReturnResponseFromService() {
            final var response = notificationController.sendNotification(ID);

            assertEquals(COUNT, response.getCount());
        }
    }

    @Nested
    class SendNotificationsForUnits {

        @Test
        void shouldReturnResponseFromService() {
            when(sendNotificationsForUnitsService.send(UNITS_REQUEST))
                .thenReturn(
                    SendNotificationResponseDTO.builder()
                        .count(COUNT)
                        .build()
                );
            final var response = notificationController.sendNotificationsForUnits(UNITS_REQUEST);

            assertEquals(COUNT, response.getCount());
        }

        @Test
        void shouldReturnResponseFromCount() {
            when(sendNotificationsForUnitsService.count( UNITS_COUNT_REQUEST))
                .thenReturn(
                    CountNotificationResponseDTO.builder()
                        .count(COUNT)
                        .max(COUNT+1)
                        .build()
                );
            final var response = notificationController.countNotificationsForUnits(UNITS_COUNT_REQUEST);

            assertEquals(COUNT, response.getCount());
            assertEquals(COUNT+1, response.getMax());
        }
    }

    @Nested
    class SendNotificationsForCareGiver {

        @Test
        void shouldReturnResponseFromService() {
            when(sendNotificationsForCareGiverService.send("ID", CARE_GIVER_REQUEST))
                .thenReturn(
                    SendNotificationResponseDTO.builder()
                        .count(COUNT)
                        .build()
                );
            final var response = notificationController.sendNotificationsForCareGiver("ID", CARE_GIVER_REQUEST);

            assertEquals(COUNT, response.getCount());
        }

        @Test
        void shouldReturnResponseFromCount() {
            when(sendNotificationsForCareGiverService.count("ID", CARE_GIVER_COUNT_REQUEST))
                .thenReturn(
                    CountNotificationResponseDTO.builder()
                        .count(COUNT)
                        .max(COUNT+1)
                        .build()
                );
            final var response = notificationController.countNotificationsForCareGiver("ID", CARE_GIVER_COUNT_REQUEST);

            assertEquals(COUNT, response.getCount());
            assertEquals(COUNT+1, response.getMax());
        }
    }
}
