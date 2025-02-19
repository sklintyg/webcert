package se.inera.intyg.webcert.web.service.sendnotification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCertificatesRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForUnitsRequestDTO;

@ExtendWith(MockitoExtension.class)
class SendNotificationRequestSanitizerTest {

    private static final LocalDateTime DATE_TIME = LocalDateTime.now();

    private static final SendNotificationsForCertificatesRequestDTO.SendNotificationsForCertificatesRequestDTOBuilder
        CERTIFICATES_REQUEST_BUILDER = SendNotificationsForCertificatesRequestDTO.builder()
        .statuses(List.of(NotificationDeliveryStatusEnum.FAILURE));

    private final SendNotificationsForUnitsRequestDTO.SendNotificationsForUnitsRequestDTOBuilder UNITS_REQUEST_BUILDER =
        SendNotificationsForUnitsRequestDTO.builder()
            .start(DATE_TIME)
            .end(DATE_TIME)
            .activationTime(DATE_TIME)
            .statuses(List.of(NotificationDeliveryStatusEnum.FAILURE));

    @Test
    void shouldSanitizeCertificatesRequest() {
        final var request = CERTIFICATES_REQUEST_BUILDER
            .certificateIds(List.of("certificateId1 ", "certificateId 2", " certificateId3"))
            .build();

        final var expected = CERTIFICATES_REQUEST_BUILDER
            .certificateIds(List.of("certificateId1", "certificateId2", "certificateId3"))
            .build();

        final var result = SendNotificationRequestSanitizer.sanitize(request);

        assertEquals(expected, result);
    }

    @Test
    void shouldReturnEmptyListIfListOfCertificateIdsIsNull() {
        final var request = CERTIFICATES_REQUEST_BUILDER
            .certificateIds(null)
            .build();

        final var result = SendNotificationRequestSanitizer.sanitize(request);

        assertEquals(Collections.emptyList(), result.getCertificateIds());
    }

    @Test
    void shouldSanitizeUnitsRequest() {
        final var request = UNITS_REQUEST_BUILDER
            .unitIds(List.of("unitId1 ", "unitId 2", " unitId3"))
            .build();

        final var expected = UNITS_REQUEST_BUILDER
            .unitIds(List.of("unitId1", "unitId2", "unitId3"))
            .build();

        final var result = SendNotificationRequestSanitizer.sanitize(request);

        assertEquals(expected, result);
    }

    @Test
    void shouldReturnEmptyListIfListOfUnitIdsIsNull() {
        final var request = UNITS_REQUEST_BUILDER
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
