package se.inera.intyg.webcert.integration.privatepractitioner.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.integration.privatepractitioner.model.GeografiskIndelningDTO.GeografiskIndelningDTOBuilder;

@Value
@Builder
@JsonDeserialize(builder = GeografiskIndelningDTOBuilder.class)
public class GeografiskIndelningDTO {
  CvDTO lan;
  CvDTO kommun;

  @JsonPOJOBuilder(withPrefix = "")
  public static class GeografiskIndelningDTOBuilder {

  }
}
