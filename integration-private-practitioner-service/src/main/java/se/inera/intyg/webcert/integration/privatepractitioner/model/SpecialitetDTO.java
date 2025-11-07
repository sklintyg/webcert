package se.inera.intyg.webcert.integration.privatepractitioner.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.integration.privatepractitioner.model.SpecialitetDTO.SpecialitetDTOBuilder;

@Value
@Builder
@JsonDeserialize(builder= SpecialitetDTOBuilder.class)
public class SpecialitetDTO {

  String kod;
  String namn;

  @JsonPOJOBuilder(withPrefix = "")
  public static class SpecialitetDTOBuilder {

  }

}
