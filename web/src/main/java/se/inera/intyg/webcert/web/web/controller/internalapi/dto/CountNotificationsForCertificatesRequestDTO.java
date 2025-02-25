package se.inera.intyg.webcert.web.web.controller.internalapi.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CountNotificationsForCertificatesRequestDTO.CountNotificationsForCertificatesRequestDTOBuilder;

@Value
@Builder
@JsonDeserialize(builder = CountNotificationsForCertificatesRequestDTOBuilder.class)

public class CountNotificationsForCertificatesRequestDTO {

    List<String> certificateIds;
    LocalDateTime activationTime;
    List<NotificationDeliveryStatusEnum> statuses;

    @JsonPOJOBuilder(withPrefix = "")
    public static class CountNotificationsForCertificatesRequestDTOBuilder {

    }
}
