package se.inera.intyg.webcert.integration.privatepractitioner.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder= HoSPersonDTO.HoSPersonTypeBuilder.class)
public class HoSPersonDTO {

  HsaIdDTO hsaId;
  PersonIdDTO personId;
  String fullstandigtNamn;
  List<BefattningDTO> befattning = new ArrayList<>();
  List<SpecialitetDTO> specialitet = new ArrayList<>();
  List<LegitimeradYrkesgruppDTO> legitimeradYrkesgrupp = new ArrayList<>();
  String forskrivarkod;
  boolean godkandAnvandare;
  EnhetsTyp enhet;

  @JsonPOJOBuilder(withPrefix = "")
  public static class HoSPersonTypeBuilder {

  }

}
