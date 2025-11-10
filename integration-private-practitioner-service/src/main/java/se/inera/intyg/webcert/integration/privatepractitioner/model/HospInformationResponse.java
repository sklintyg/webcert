package se.inera.intyg.webcert.integration.privatepractitioner.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HospInformationResponse {
  HospInformationDTO hospInformation;
}
