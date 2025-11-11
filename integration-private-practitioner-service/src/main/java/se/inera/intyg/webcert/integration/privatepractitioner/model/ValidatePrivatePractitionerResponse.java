package se.inera.intyg.webcert.integration.privatepractitioner.model;

public record ValidatePrivatePractitionerResponse(
    ValidatePrivatePractitionerResultCode resultCode,
    String resultText) {

}
