package se.inera.intyg.webcert.integration.privatepractitioner.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrivatePractitionerValidationRequest {

    private String personalIdentityNumber;

}
