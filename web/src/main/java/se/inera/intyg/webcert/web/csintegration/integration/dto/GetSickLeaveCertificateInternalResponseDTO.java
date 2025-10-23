package se.inera.intyg.webcert.web.csintegration.integration.dto;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GetSickLeaveCertificateInternalResponseDTO {
  boolean available;
  SickLeaveCertificateDTO sickLeaveCertificate;

  @JsonPOJOBuilder(withPrefix = "")
  public static class GetSickLeaveCertificateInternalResponseDTOBuilder {

  }
}
