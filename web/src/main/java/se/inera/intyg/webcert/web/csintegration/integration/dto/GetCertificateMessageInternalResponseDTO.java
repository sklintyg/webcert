package se.inera.intyg.webcert.web.csintegration.integration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateMessageInternalResponseDTO.GetCertificateMessageInternalResponseBuilder;
import se.inera.intyg.webcert.web.web.controller.facade.dto.QuestionDTO;

@JsonDeserialize(builder = GetCertificateMessageInternalResponseBuilder.class)
@Value
@Builder
public class GetCertificateMessageInternalResponseDTO {

    List<QuestionDTO> questions;

    @JsonPOJOBuilder(withPrefix = "")
    public static class GetCertificateMessageInternalResponseBuilder {

    }
}
