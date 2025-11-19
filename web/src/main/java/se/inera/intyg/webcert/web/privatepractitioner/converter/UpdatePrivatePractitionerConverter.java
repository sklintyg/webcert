package se.inera.intyg.webcert.web.privatepractitioner.converter;

import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.PrivatePractitionerUpdateRequest;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerDetails;

@Component
public class UpdatePrivatePractitionerConverter {

    public PrivatePractitionerUpdateRequest convert(PrivatePractitionerDetails privatePractitionerRegisterRequest,
        WebCertUserService webCertUserService) {

        final var user = webCertUserService.getUser();
        if (user == null) {
            throw new IllegalStateException("User not found in WebCertUserService");
        }

        return PrivatePractitionerUpdateRequest.builder()
            .personId(user.getPersonId())
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
