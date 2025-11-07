package model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import model.GetPrivatePractitionerResponseDTO.GetPrivatePractitionerResponseDTOBuilder;

@Value
@Builder
@JsonDeserialize(builder = GetPrivatePractitionerResponseDTOBuilder.class)
public class GetPrivatePractitionerResponseDTO {

  private HoSPersonDTO hoSPerson;
  private ResultCodeEnum resultCode;
  private String resultText;

  @JsonPOJOBuilder(withPrefix = "")
  public static class GetPrivatePractitionerResponseDTOBuilder {

  }

}
