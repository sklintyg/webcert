package se.inera.intyg.webcert.web.service.sendnotification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCareGiverRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCertificatesRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForUnitsRequestDTO;

@ExtendWith(MockitoExtension.class)
class SendNotificationCountValidatorTest {

    private static final int LIMIT = 10;
    @Mock
    NotificationRedeliveryRepository notificationRedeliveryRepository;
    @InjectMocks
    SendNotificationCountValidator sendNotificationCountValidator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sendNotificationCountValidator, "maxAllowedNotificationSend", LIMIT);
    }

    @Nested
    class CareGiverTests {

        private static final String ID = "id";

        @Test
        void shallThrowIfLimitExceeded() {
            final var request = SendNotificationsForCareGiverRequestDTO.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .statuses(List.of(NotificationDeliveryStatusEnum.FAILURE))
                .build();

            when(notificationRedeliveryRepository.countInsertsForCareGiver(ID, request.getStatuses(), request.getStart(),
                request.getEnd())).thenReturn(LIMIT + 1);

            assertThrows(IllegalArgumentException.class, () -> sendNotificationCountValidator.careGiver(ID, request));
        }

        @Test
        void shallNotThrowIfLimitIsNotExceeded() {
            final var request = SendNotificationsForCareGiverRequestDTO.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .statuses(List.of(NotificationDeliveryStatusEnum.FAILURE))
                .build();

            when(notificationRedeliveryRepository.countInsertsForCareGiver(ID, request.getStatuses(), request.getStart(),
                request.getEnd())).thenReturn(LIMIT);

            assertDoesNotThrow(() -> sendNotificationCountValidator.careGiver(ID, request));
        }
    }

    @Nested
    class UnitsTests {

        @Test
        void shallThrowIfLimitExceeded() {
            final var request = SendNotificationsForUnitsRequestDTO.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .statuses(List.of(NotificationDeliveryStatusEnum.FAILURE))
                .unitIds(List.of("unit1", "unit2"))
                .build();

            when(notificationRedeliveryRepository.countInsertsForCertificates(request.getUnitIds(), request.getStatuses(),
                request.getStart(),
                request.getEnd())).thenReturn(LIMIT + 1);

            assertThrows(IllegalArgumentException.class, () -> sendNotificationCountValidator.units(request));
        }

        @Test
        void shallNotThrowIfLimitIsNotExceeded() {
            final var request = SendNotificationsForUnitsRequestDTO.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .statuses(List.of(NotificationDeliveryStatusEnum.FAILURE))
                .unitIds(List.of("unit1", "unit2"))
                .build();

            when(notificationRedeliveryRepository.countInsertsForCertificates(request.getUnitIds(), request.getStatuses(),
                request.getStart(),
                request.getEnd())).thenReturn(LIMIT);

            assertDoesNotThrow(() -> sendNotificationCountValidator.units(request));
        }
    }

    @Nested
    class CertificatesTests {

        @Test
        void shallThrowIfLimitExceeded() {
            final var request = SendNotificationsForCertificatesRequestDTO.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .statuses(List.of(NotificationDeliveryStatusEnum.FAILURE))
                .certificateIds(List.of("cert1", "cert2"))
                .build();

            when(notificationRedeliveryRepository.countInsertsForCertificates(request.getCertificateIds(), request.getStatuses(),
                request.getStart(),
                request.getEnd())).thenReturn(LIMIT + 1);

            assertThrows(IllegalArgumentException.class, () -> sendNotificationCountValidator.certiticates(request));
        }

        @Test
        void shallNotThrowIfLimitIsNotExceeded() {
            final var request = SendNotificationsForCertificatesRequestDTO.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .statuses(List.of(NotificationDeliveryStatusEnum.FAILURE))
                .certificateIds(List.of("cert1", "cert2"))
                .build();

            when(notificationRedeliveryRepository.countInsertsForCertificates(request.getCertificateIds(), request.getStatuses(),
                request.getStart(),
                request.getEnd())).thenReturn(LIMIT);

            assertDoesNotThrow(() -> sendNotificationCountValidator.certiticates(request));
        }
    }

    @Nested
    class NotificationTests {

        private static final String NOTIFICATION_ID = "notificationId";

        @Test
        void shallThrowIfLimitExceeded() {
            when(notificationRedeliveryRepository.countNotification(NOTIFICATION_ID)).thenReturn(LIMIT + 1);

            assertThrows(IllegalArgumentException.class, () -> sendNotificationCountValidator.notification(NOTIFICATION_ID));
        }

        @Test
        void shallNotThrowIfLimitIsNotExceeded() {
            when(notificationRedeliveryRepository.countNotification(NOTIFICATION_ID)).thenReturn(LIMIT);

            assertDoesNotThrow(() -> sendNotificationCountValidator.notification(NOTIFICATION_ID));
        }

    }
}