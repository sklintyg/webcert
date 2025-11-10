package se.inera.intyg.webcert.web.web.controller.api.dto;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class RegisterPrivatePractitionerRequest {

    PrivatePractitionerRegistrationDTO privatePractitionerRegistration;
}
