package se.inera.intyg.webcert.web.csintegration.integration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetSickLeaveCertificateInternalResponseDTO.GetSickLeaveCertificateInternalResponseDTOBuilder;

@Value
@Builder
@JsonDeserialize(builder = GetSickLeaveCertificateInternalResponseDTOBuilder.class)
public class GetSickLeaveCertificateInternalResponseDTO {
  boolean available;
  SickLeaveCertificateDTO sickLeaveCertificate;

  @JsonPOJOBuilder(withPrefix = "")
  public static class GetSickLeaveCertificateInternalResponseDTOBuilder {

  }
}
