package se.inera.intyg.webcert.integration.privatepractitioner.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = BefattningDTO.BefattningDTOBuilder.class)
public class BefattningDTO {

  private String kod;
  private String namn;

  @JsonPOJOBuilder(withPrefix = "")
  public static class BefattningDTOBuilder {
  }

}
