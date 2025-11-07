package model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
@JsonDeserialize(builder = HsaIdDTO.HsaIdDTOBuilder.class)
public class HsaIdDTO {

  private String root;
  private String extension;
  private String identifierName;

  @JsonPOJOBuilder(withPrefix = "")
  public static class HsaIdDTOBuilder {

  }

}
