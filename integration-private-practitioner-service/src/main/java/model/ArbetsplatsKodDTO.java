package model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import model.ArbetsplatsKodDTO.ArbetsplatsKodDTOBuilder;
import model.EnhetsTyp.EnhetsTypBuilder;

@Value
@Builder
@JsonDeserialize(builder = ArbetsplatsKodDTOBuilder.class)
public class ArbetsplatsKodDTO {

  String root;
  String extension;
  String identifierName;

  @JsonPOJOBuilder(withPrefix = "")
  public static class ArbetsplatsKodDTOBuilder {

  }

}
