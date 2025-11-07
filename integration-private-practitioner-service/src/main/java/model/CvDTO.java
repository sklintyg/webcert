package model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import model.CvDTO.CvDTOBuilder;

@Value
@Builder
@JsonDeserialize(builder = CvDTOBuilder.class)
public class CvDTO {

  String code;
  String codeSystem;
  String codeSystemName;
  String codeSystemVersion;
  String displayName;
  String originalText;

  @JsonPOJOBuilder(withPrefix = "")
  public static class CvDTOBuilder {

  }

}
