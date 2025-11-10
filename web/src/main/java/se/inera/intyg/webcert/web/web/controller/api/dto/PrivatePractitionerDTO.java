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
            .personId(privatePractitioner.getPersonId())
            .name(privatePractitioner.getName())
            .position(privatePractitioner.getPosition())
            .careUnitName(privatePractitioner.getCareUnitName())
            .ownershipType(privatePractitioner.getOwnershipType())
            .typeOfCare(privatePractitioner.getTypeOfCare())
            .healthcareServiceType(privatePractitioner.getHealthcareServiceType())
            .workplaceCode(privatePractitioner.getWorkplaceCode())
            .phoneNumber(privatePractitioner.getPhoneNumber())
            .email(privatePractitioner.getEmail())
            .postalAddress(privatePractitioner.getPostalAddress())
            .zipCode(privatePractitioner.getZipCode())
            .city(privatePractitioner.getCity())
            .municipality(privatePractitioner.getMunicipalityCode())
            .county(privatePractitioner.getCountyCode())
            .build();
    }
}
