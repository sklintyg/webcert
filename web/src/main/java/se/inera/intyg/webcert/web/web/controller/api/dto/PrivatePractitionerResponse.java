package se.inera.intyg.webcert.web.web.controller.api.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PrivatePractitionerResponse {

  PrivatePractitionerDTO privatePractitioner;

}
