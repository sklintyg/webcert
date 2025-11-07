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

  HsaIdDTO enhetsId;
  String enhetsnamn;
  ArbetsplatsKodDTO arbetsplatskod;
  String agarform;
  String postadress;
  String postnummer;
  String postort;
  String telefonnummer;
  String epost;
  LocalDateTime startdatum;
  LocalDateTime slutdatum;
  GeografiskIndelningDTO geografiskIndelning;
  VerksamhetDTO verksamhetstyp;
  CareProviderDTO vardgivare;

  @JsonPOJOBuilder(withPrefix = "")
  public static class EnhetsTypBuilder {

  }
}
