package se.inera.intyg.webcert.web.csintegration.integration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredQAs;
import java.util.Map;

@JsonDeserialize(builder = GetUnansweredCommunicationInternalResponseDTO.GetUnansweredCommunicationInternalResponseDTOBuilder.class)
@Value
@Builder
public class GetUnansweredCommunicationInternalResponseDTO {

    Map<String, UnansweredQAs> messages;

    @JsonPOJOBuilder(withPrefix = "")
    public static class GetUnansweredCommunicationInternalResponseDTOBuilder {

    }
}