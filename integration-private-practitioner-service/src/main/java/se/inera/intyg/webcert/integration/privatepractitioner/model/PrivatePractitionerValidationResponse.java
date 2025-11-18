package se.inera.intyg.webcert.integration.privatepractitioner.model;

public record PrivatePractitionerValidationResponse(
    PrivatePractitionerValidationResultCode resultCode,
    String resultText) {

}
