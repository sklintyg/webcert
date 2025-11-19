package se.inera.intyg.webcert.web.privatepractitioner.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.PrivatePractitionerDetailsRequest;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerDetails;

@Component
@RequiredArgsConstructor
public class PrivatePractitionerUpdateRequestConverter {

    private final WebCertUserService webCertUserService;

    public PrivatePractitionerDetailsRequest convert(PrivatePractitionerDetails privatePractitionerUpdateRequest) {
        final var webCertUser = webCertUserService.getUser();

        if (privatePractitionerUpdateRequest == null) {
            throw new IllegalArgumentException("PrivatePractitionerDetails cannot be null");
        }
        if (webCertUser == null) {
            throw new IllegalStateException("WebCertUser cannot be null");
        }

        return PrivatePractitionerDetailsRequest.builder()
            .personId(webCertUser.getPersonId())
            .name(webCertUser.getNamn())
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
