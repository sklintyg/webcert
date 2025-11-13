package se.inera.intyg.webcert.integration.privatepractitioner.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = EnhetsTyp.EnhetsTypBuilder.class)
public class EnhetsTyp {

  HsaId enhetsId;
  String enhetsnamn;
  ArbetsplatsKod arbetsplatskod;
  String agarform;
  String postadress;
  String postnummer;
  String postort;
  String telefonnummer;
  String epost;
  LocalDateTime startdatum;
  LocalDateTime slutdatum;
  GeografiskIndelning geografiskIndelning;
  Verksamhet verksamhetstyp;
  CareProvider vardgivare;

  @JsonPOJOBuilder(withPrefix = "")
  public static class EnhetsTypBuilder {

  }
}
