package se.inera.intyg.webcert.integration.privatepractitioner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrivatePractitionerValidationRequest {

    private String personId;

}
