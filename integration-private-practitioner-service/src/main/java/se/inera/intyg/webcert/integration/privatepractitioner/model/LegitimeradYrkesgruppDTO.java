package se.inera.intyg.webcert.integration.privatepractitioner.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.integration.privatepractitioner.model.LegitimeradYrkesgruppDTO.LegitimeradYrkesgruppDTOBuilder;

@Value
@Builder
@JsonDeserialize(builder = LegitimeradYrkesgruppDTOBuilder.class)
public class LegitimeradYrkesgruppDTO {

  String kod;
  String namn;

  @JsonPOJOBuilder(withPrefix = "")
  public static class LegitimeradYrkesgruppDTOBuilder {

  }
}
