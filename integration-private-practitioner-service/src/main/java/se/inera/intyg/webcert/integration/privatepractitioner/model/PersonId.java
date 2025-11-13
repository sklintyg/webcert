package se.inera.intyg.webcert.integration.privatepractitioner.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.integration.privatepractitioner.model.PersonId.PersonIdDTOBuilder;

@Value
@Builder
@JsonDeserialize(builder = PersonIdDTOBuilder.class)
public class PersonId {

    String root;
    String extension;
    String identifierName;

    @JsonPOJOBuilder(withPrefix = "")
    public static class PersonIdDTOBuilder {

    }

}
