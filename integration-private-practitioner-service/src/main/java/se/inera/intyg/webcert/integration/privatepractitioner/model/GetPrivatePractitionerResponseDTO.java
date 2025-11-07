package se.inera.intyg.webcert.integration.privatepractitioner.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.integration.privatepractitioner.model.GetPrivatePractitionerResponseDTO.GetPrivatePractitionerResponseDTOBuilder;

@Value
@Builder
@JsonDeserialize(builder = GetPrivatePractitionerResponseDTOBuilder.class)
public class GetPrivatePractitionerResponseDTO {

  HoSPersonDTO hoSPerson;
  ResultCodeEnum resultCode;
  String resultText;

  @JsonPOJOBuilder(withPrefix = "")
  public static class GetPrivatePractitionerResponseDTOBuilder {

  }

}
