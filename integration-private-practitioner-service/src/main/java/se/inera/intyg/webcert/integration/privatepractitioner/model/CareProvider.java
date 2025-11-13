package se.inera.intyg.webcert.integration.privatepractitioner.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = CareProvider.CareProviderDTOBuilder.class)
public class CareProvider {
  HsaId vardgivareId;
  String vardgivarenamn;
  LocalDateTime startdatum;
  LocalDateTime slutdatum;

  @JsonPOJOBuilder(withPrefix = "")
  public static class CareProviderDTOBuilder {

  }

}
