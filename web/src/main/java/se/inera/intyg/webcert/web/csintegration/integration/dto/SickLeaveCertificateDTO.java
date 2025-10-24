package se.inera.intyg.webcert.web.csintegration.integration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SickLeaveCertificateDTO.SickLeaveCertificateDTOBuilder;

@Value
@Builder
@JsonDeserialize(builder = SickLeaveCertificateDTOBuilder.class)
public class SickLeaveCertificateDTO {

  String id;
  String diagnoseCode;
  String extendsCertificateId;
  LocalDateTime signedDateTime;

  @JsonPOJOBuilder(withPrefix = "")
  public static class SickLeaveCertificateDTOBuilder {

  }

}
