package se.inera.intyg.webcert.web.privatepractitioner.factory;

import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.UpdatePrivatePractitionerRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerDetails;

@Component
public class UpdatePrivatePractitionerFactory {

    public UpdatePrivatePractitionerRequest create(PrivatePractitionerDetails privatePractitionerRegisterRequest) {

        return UpdatePrivatePractitionerRequest.builder()
            .position(privatePractitionerRegisterRequest.getPosition())
            .careUnitName(privatePractitionerRegisterRequest.getCareUnitName())
            .typeOfCare(privatePractitionerRegisterRequest.getTypeOfCare())
            .healthcareServiceType(privatePractitionerRegisterRequest.getHealthcareServiceType())
            .workplaceCode(privatePractitionerRegisterRequest.getWorkplaceCode())
            .phoneNumber(privatePractitionerRegisterRequest.getPhoneNumber())
            .email(privatePractitionerRegisterRequest.getEmail())
            .address(privatePractitionerRegisterRequest.getAddress())
            .zipCode(privatePractitionerRegisterRequest.getZipCode())
            .city(privatePractitionerRegisterRequest.getCity())
            .municipality(privatePractitionerRegisterRequest.getMunicipality())
            .county(privatePractitionerRegisterRequest.getCounty())
            .build();
    }
}
