package se.inera.intyg.webcert.web.privatepractitioner.converter;

import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.PrivatePractitionerDetailsRequest;

@Component
public class PrivatePractitionerUpdateRequestConverter {

    public PrivatePractitionerDetailsRequest convert(PrivatePractitionerDetailsRequest privatePractitionerUpdateRequest) {
        return UpdatePrivatePractitionerRequest.builder()
            .personId(privatePractitionerUpdateRequest.getPersonId())
            .position(privatePractitionerUpdateRequest.getPosition())
            .workplaceCode(privatePractitionerUpdateRequest.getWorkplaceCode())
            .careUnitName(privatePractitionerUpdateRequest.getCareUnitName())
            .typeOfCare(privatePractitionerUpdateRequest.getTypeOfCare())
            .healthcareServiceType(privatePractitionerUpdateRequest.getHealthcareServiceType())
            .phoneNumber(privatePractitionerUpdateRequest.getPhoneNumber())
            .email(privatePractitionerUpdateRequest.getEmail())
            .address(privatePractitionerUpdateRequest.getAddress())
            .zipCode(privatePractitionerUpdateRequest.getZipCode())
            .city(privatePractitionerUpdateRequest.getCity())
            .municipality(privatePractitionerUpdateRequest.getMunicipality())
            .county(privatePractitionerUpdateRequest.getCounty())
            .build();
    }
}
