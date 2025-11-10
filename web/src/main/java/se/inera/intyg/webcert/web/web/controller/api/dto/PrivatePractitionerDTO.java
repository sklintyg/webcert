package se.inera.intyg.webcert.web.web.controller.api.dto;

import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.web.ppsintegration.dto.PrivatePractitioner;

@Builder
@Value
public class PrivatePractitionerDTO {

    String personId;
    String name;

    String position;
    String careUnitName;
    String ownershipType;
    String typeOfCare;
    String healthcareServiceType;
    String workplaceCode;

    String phoneNumber;
    String email;
    String postalAddress;
    String zipCode;
    String city;
    String municipality;
    String county;

    Long consentFormVersion;

    public static PrivatePractitionerDTO create(
        PrivatePractitioner privatePractitioner) {
        return PrivatePractitionerDTO.builder()
            .position(privatePractitioner.getPosition())
            .careUnitName(privatePractitioner.getOrganisationName())
            .typeOfCare(privatePractitioner.getFormOfCare())
            .healthcareServiceType(privatePractitioner.getOrganisationType())
            .workplaceCode(privatePractitioner.getWorkplaceCode())
            .phoneNumber(privatePractitioner.getPhoneNumber())
            .email(privatePractitioner.getEmail())
            .postalAddress(privatePractitioner.getPostalAddress())
            .zipCode(privatePractitioner.getPostalCode())
            .city(privatePractitioner.getPostalCity())
            .municipality(privatePractitioner.getMunicipalityCode())
            .county(privatePractitioner.getCountyCode())
            .build();
    }
}
