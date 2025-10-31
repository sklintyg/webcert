package se.inera.intyg.webcert.web.csintegration.integration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetSickLeaveCertificateInternalIgnoreModelRulesDTO.GetSickLeaveCertificateInternalIgnoreModelRulesDTOBuilder;

@Value
@Builder
@JsonDeserialize(builder = GetSickLeaveCertificateInternalIgnoreModelRulesDTOBuilder.class)
public class GetSickLeaveCertificateInternalIgnoreModelRulesDTO {

  boolean ignoreModelRules;

  @JsonPOJOBuilder(withPrefix = "")
  public static class GetSickLeaveCertificateInternalIgnoreModelRulesDTOBuilder {

  }

}
