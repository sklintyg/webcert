package se.inera.intyg.webcert.web.web.controller.internalapi.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;

@Value
@Builder
public class CountNotificationsForCertificatesRequestDTO {

    List<String> certificateIds;
    LocalDateTime activationTime;
    List<NotificationDeliveryStatusEnum> statuses;
}
