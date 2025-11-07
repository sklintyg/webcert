package se.inera.intyg.webcert.integration.privatepractitioner.model;

import lombok.Data;

@Data
public class ValidatePrivatePractitionerResponse {

  private ValidatePrivatePractitionerResultCode resultCode;
  private String resultText;

}
