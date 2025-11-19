package se.inera.intyg.webcert.integration.privatepractitioner.dto;

public record PrivatePractitionerValidationResponse(
    PrivatePractitionerValidationResultCode resultCode,
    String resultText) {

}
