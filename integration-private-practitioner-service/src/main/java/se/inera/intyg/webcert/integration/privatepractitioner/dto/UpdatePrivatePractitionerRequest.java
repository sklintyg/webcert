package se.inera.intyg.webcert.integration.privatepractitioner.dto;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@Builder
public class UpdatePrivatePractitionerRequest {

    @With
    String personId;

    String position;
    String careUnitName;
    String typeOfCare;
    String healthcareServiceType;
    String workplaceCode;

    String phoneNumber;
    String email;
    String address;
    String zipCode;
    String city;
    String municipality;
    String county;
}
