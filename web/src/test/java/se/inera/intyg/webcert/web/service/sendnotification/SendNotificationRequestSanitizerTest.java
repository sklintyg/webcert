package se.inera.intyg.webcert.web.service.sendnotification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CountNotificationsForCareGiverRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CountNotificationsForUnitsRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCareGiverRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCertificatesRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForUnitsRequestDTO;

@ExtendWith(MockitoExtension.class)
class SendNotificationRequestSanitizerTest {

    private static final LocalDateTime DATE_TIME = LocalDateTime.now();

    private static final SendNotificationsForCertificatesRequestDTO.SendNotificationsForCertificatesRequestDTOBuilder
        CERTIFICATES_REQUEST_BUILDER = SendNotificationsForCertificatesRequestDTO.builder()
        .statuses(
            List.of(NotificationDeliveryStatusEnum.FAILURE,
                NotificationDeliveryStatusEnum.RESEND,
                NotificationDeliveryStatusEnum.SUCCESS)
        );

    private final SendNotificationsForUnitsRequestDTO.SendNotificationsForUnitsRequestDTOBuilder UNITS_RESEND_REQUEST_BUILDER =
        SendNotificationsForUnitsRequestDTO.builder()
            .start(DATE_TIME)
            .end(DATE_TIME)
            .activationTime(DATE_TIME)
            .statuses(
                List.of(NotificationDeliveryStatusEnum.FAILURE,
                    NotificationDeliveryStatusEnum.RESEND,
                    NotificationDeliveryStatusEnum.SUCCESS)
            );

    private final CountNotificationsForUnitsRequestDTO.CountNotificationsForUnitsRequestDTOBuilder UNITS_COUNT_REQUEST_BUILDER =
        CountNotificationsForUnitsRequestDTO.builder()
            .start(DATE_TIME)
            .end(DATE_TIME)
            .activationTime(DATE_TIME)
            .statuses(
                List.of(NotificationDeliveryStatusEnum.FAILURE,
                    NotificationDeliveryStatusEnum.RESEND,
                    NotificationDeliveryStatusEnum.SUCCESS)
            );

    private final SendNotificationsForCareGiverRequestDTO.SendNotificationsForCareGiverRequestDTOBuilder CARE_GIVER_RESEND_REQUEST_BUILDER =
        SendNotificationsForCareGiverRequestDTO.builder()
            .start(DATE_TIME)
            .end(DATE_TIME)
            .activationTime(DATE_TIME)
            .statuses(
                List.of(NotificationDeliveryStatusEnum.FAILURE,
                    NotificationDeliveryStatusEnum.RESEND,
                    NotificationDeliveryStatusEnum.SUCCESS)
            );

    private final CountNotificationsForCareGiverRequestDTO.CountNotificationsForCareGiverRequestDTOBuilder CARE_GIVER_COUNT_REQUEST_BUILDER =
        CountNotificationsForCareGiverRequestDTO.builder()
            .start(DATE_TIME)
            .end(DATE_TIME)
            .activationTime(DATE_TIME)
            .statuses(
                List.of(NotificationDeliveryStatusEnum.FAILURE,
                    NotificationDeliveryStatusEnum.RESEND,
                    NotificationDeliveryStatusEnum.SUCCESS)
            );

    @Nested
    class RemoveBlankSpaces {

        @Test
        void shouldRemoveBlankSpacesFromCertificateIdsInCertificatesRequest() {
            final var request = CERTIFICATES_REQUEST_BUILDER
                .certificateIds(List.of("certificateId1 ", "certificateId 2", " certificateId3"))
                .build();

            final var expected = List.of("certificateId1", "certificateId2", "certificateId3");

            final var result = SendNotificationRequestSanitizer.sanitize(request);

            assertEquals(expected, result.getCertificateIds());
        }

        @Test
        void shouldReturnEmptyListIfListIfCertificateIdsAreNull() {
            final var request = CERTIFICATES_REQUEST_BUILDER
                .certificateIds(null)
                .build();

            final var result = SendNotificationRequestSanitizer.sanitize(request);

            assertEquals(Collections.emptyList(), result.getCertificateIds());
        }

        @Test
        void shouldRemoveBlankSpacesFromUnitIdsInUnitRequest() {
            final var request = UNITS_RESEND_REQUEST_BUILDER
                .unitIds(List.of("unitId1 ", "unitId 2", " unitId3"))
                .build();

            final var expected = List.of("unitId1", "unitId2", "unitId3");

            final var result = SendNotificationRequestSanitizer.sanitize(request);

            assertEquals(expected, result.getUnitIds());
        }

        @Test
        void shouldReturnEmptyListIfListIfUnitIdsAreNull() {
            final var request = UNITS_RESEND_REQUEST_BUILDER
                .unitIds(null)
                .build();

            final var result = SendNotificationRequestSanitizer.sanitize(request);

            assertEquals(Collections.emptyList(), result.getUnitIds());
        }

        @Test
        void shouldRemoveBlankSpaceFromString() {
            final var stringWithBlankSpace = "string ";
            final var result = SendNotificationRequestSanitizer.sanitize(stringWithBlankSpace);

            assertEquals("string", result);
        }

    }

    @Nested
    class RemoveIncorrectStatus {

        @Test
        void shouldRemoveStatusResendFromStatusesInUnitRequest() {
            final var expected = List.of(NotificationDeliveryStatusEnum.FAILURE, NotificationDeliveryStatusEnum.SUCCESS);

            final var result = SendNotificationRequestSanitizer.sanitize(UNITS_RESEND_REQUEST_BUILDER.build());

            assertEquals(expected, result.getStatuses());
        }

        @Test
        void shouldRemoveStatusResendFromStatusesInCertificatesRequest() {
            final var expected = List.of(NotificationDeliveryStatusEnum.FAILURE, NotificationDeliveryStatusEnum.SUCCESS);

            final var result = SendNotificationRequestSanitizer.sanitize(CERTIFICATES_REQUEST_BUILDER.build());

            assertEquals(expected, result.getStatuses());
        }

        @Test
        void shouldRemoveStatusResendFromStatusesInResendCareGiverRequest() {
            final var expected = List.of(NotificationDeliveryStatusEnum.FAILURE, NotificationDeliveryStatusEnum.SUCCESS);

            final var result = SendNotificationRequestSanitizer.sanitize(CARE_GIVER_RESEND_REQUEST_BUILDER.build());

            assertEquals(expected, result.getStatuses());
        }

        @Test
        void shouldRemoveStatusResendFromStatusesInCountCareGiverRequest() {
            final var expected = List.of(NotificationDeliveryStatusEnum.FAILURE, NotificationDeliveryStatusEnum.SUCCESS);

            final var result = SendNotificationRequestSanitizer.sanitize(CARE_GIVER_COUNT_REQUEST_BUILDER.build());

            assertEquals(expected, result.getStatuses());
        }
    }

    @Nested
    class
    SetActivationTime {

        @Test
        void shouldSetActivationTimeToCurrentTimeIfNullInResendUnitRequest() {
            final var request = UNITS_RESEND_REQUEST_BUILDER
                .activationTime(null)
                .build();

            final var result = SendNotificationRequestSanitizer.sanitize(request);

            assertNotNull(result.getActivationTime());
        }

        @Test
        void shouldSetActivationTimeToCurrentTimeIfNullInCountUnitRequest() {
            final var request = UNITS_COUNT_REQUEST_BUILDER
                .activationTime(null)
                .build();

            final var result = SendNotificationRequestSanitizer.sanitize(request);

            assertNotNull(result.getActivationTime());
        }


        @Test
        void shouldSetActivationTimeToCurrentTimeIfNullInResendCareGiverRequest() {
            final var request = CARE_GIVER_RESEND_REQUEST_BUILDER
                .activationTime(null)
                .build();

            final var result = SendNotificationRequestSanitizer.sanitize(request);

            assertNotNull(result.getActivationTime());
        }

        @Test
        void shouldSetActivationTimeToCurrentTimeIfNullInCountCareGiverRequest() {
            final var request = CARE_GIVER_COUNT_REQUEST_BUILDER
                .activationTime(null)
                .build();

            final var result = SendNotificationRequestSanitizer.sanitize(request);

            assertNotNull(result.getActivationTime());
        }
    }
}
