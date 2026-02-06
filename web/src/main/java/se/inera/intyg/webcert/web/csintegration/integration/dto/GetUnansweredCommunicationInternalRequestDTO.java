package se.inera.intyg.webcert.web.csintegration.integration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@JsonDeserialize(builder = GetUnansweredCommunicationInternalRequestDTO.GetUnansweredCommunicationInternalRequestDTOBuilder.class)
public class GetUnansweredCommunicationInternalRequestDTO {

    List<String> patientId;
    Integer maxDaysOfUnansweredCommunication;

    @JsonPOJOBuilder(withPrefix = "")
    public static class GetUnansweredCommunicationInternalRequestDTOBuilder {

    }

}