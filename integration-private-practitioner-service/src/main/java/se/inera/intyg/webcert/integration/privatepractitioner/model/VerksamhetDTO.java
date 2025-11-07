package se.inera.intyg.webcert.integration.privatepractitioner.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = VerksamhetDTO.VerksamhetDTOBuilder.class)
public class VerksamhetDTO {

  List<CvDTO> verksamhet = new ArrayList<>();
  List<CvDTO> vardform = new ArrayList<>();

  @JsonPOJOBuilder(withPrefix = "")
  public static class VerksamhetDTOBuilder {

  }
}
