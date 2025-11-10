package se.inera.intyg.webcert.integration.privatepractitioner.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
@JsonDeserialize(builder = HospInformationDTO.HospInformationDTOBuilder.class)
public class HospInformationDTO {

  String personalPrescriptionCode;
  List<String> specialityNames;
  List<String> hsaTitles;

  @JsonPOJOBuilder(withPrefix = "")
  public static class HospInformationDTOBuilder {

  }

}
