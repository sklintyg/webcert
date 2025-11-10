package se.inera.intyg.webcert.web.web.controller.api.dto;

import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.integration.privatepractitioner.model.GetPrivatePractitionerConfigResponse;

@Value
@Builder
public class PrivatePractitionerConfigResponse {

  GetPrivatePractitionerConfigResponse getPrivatePractitionerConfig;

}
