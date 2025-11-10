package se.inera.intyg.webcert.web.web.controller.api.dto;

import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.web.ppsintegration.dto.HospInformation;

@Value
@Builder
public class HospInformationResponse {

  HospInformation hospInformation;

}
