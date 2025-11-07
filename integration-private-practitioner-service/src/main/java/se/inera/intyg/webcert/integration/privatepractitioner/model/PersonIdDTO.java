package se.inera.intyg.webcert.integration.privatepractitioner.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.integration.privatepractitioner.model.PersonIdDTO.PersonIdDTOBuilder;

@Value
@Builder
@JsonDeserialize(builder = PersonIdDTOBuilder.class)
public class PersonIdDTO {

  private String root;
  private String extension;
  private String identifierName;

  @JsonPOJOBuilder(withPrefix = "")
  public static class PersonIdDTOBuilder {

  }

}
