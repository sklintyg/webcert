package se.inera.intyg.webcert.integration.privatepractitioner.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.integration.privatepractitioner.model.ArbetsplatsKod.ArbetsplatsKodDTOBuilder;

@Value
@Builder
@JsonDeserialize(builder = ArbetsplatsKodDTOBuilder.class)
public class ArbetsplatsKod {

  String root;
  String extension;
  String identifierName;

  @JsonPOJOBuilder(withPrefix = "")
  public static class ArbetsplatsKodDTOBuilder {

  }

}
