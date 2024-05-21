package se.inera.intyg.webcert.web.csintegration.message.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.web.csintegration.message.dto.IncomingComplementDTO.IncomingComplementDTOBuilder;

@JsonDeserialize(builder = IncomingComplementDTOBuilder.class)
@Value
@Builder
public class IncomingComplementDTO {

    String questionId;
    Integer instance;
    String content;

    @JsonPOJOBuilder(withPrefix = "")
    public static class IncomingComplementDTOBuilder {

    }
}
