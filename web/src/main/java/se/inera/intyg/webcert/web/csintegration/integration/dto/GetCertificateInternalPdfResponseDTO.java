package se.inera.intyg.webcert.web.csintegration.integration.dto;

import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(
    builder =
        GetCertificateInternalPdfResponseDTO.GetCertificateInternalPdfResponseDTOBuilder.class)
@Value
@Builder
public class GetCertificateInternalPdfResponseDTO {

  byte[] pdfData;

  @JsonPOJOBuilder(withPrefix = "")
  public static class GetCertificateInternalPdfResponseDTOBuilder {}
}
