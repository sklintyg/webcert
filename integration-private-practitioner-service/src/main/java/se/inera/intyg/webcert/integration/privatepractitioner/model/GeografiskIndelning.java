package se.inera.intyg.webcert.integration.privatepractitioner.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.integration.privatepractitioner.model.GeografiskIndelning.GeografiskIndelningDTOBuilder;

@Value
@Builder
@JsonDeserialize(builder = GeografiskIndelningDTOBuilder.class)
public class GeografiskIndelning {
  Cv lan;
  Cv kommun;

  @JsonPOJOBuilder(withPrefix = "")
  public static class GeografiskIndelningDTOBuilder {

  }
}
