package se.inera.intyg.webcert.web.web.controller.api.dto;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import se.inera.intyg.webcert.web.ppsintegration.dto.PrivatePractitioner;

@Builder
@Value
public class PrivatePractitionerDTO {

    @NonNull
    String position;
    @NonNull
    String organisationName;
    @NonNull
    String formOfCare;
    @NonNull
    String organisationType;
    String workplaceCode;
    @NonNull
    String phoneNumber;
    @NonNull
    String email;
    @NonNull
    String postalAddress;
    @NonNull
    String postalCode;
    String postalCity;
    String municipalityCode;
    String countyCode;

    public static PrivatePractitionerDTO create(
        PrivatePractitioner privatePractitioner) {
        return PrivatePractitionerDTO.builder()
            .position(privatePractitioner.getPosition())
            .organisationName(privatePractitioner.getOrganisationName())
            .formOfCare(privatePractitioner.getFormOfCare())
            .organisationType(privatePractitioner.getOrganisationType())
            .workplaceCode(privatePractitioner.getWorkplaceCode())
            .phoneNumber(privatePractitioner.getPhoneNumber())
            .email(privatePractitioner.getEmail())
            .postalAddress(privatePractitioner.getPostalAddress())
            .postalCode(privatePractitioner.getPostalCode())
            .postalCity(privatePractitioner.getPostalCity())
            .municipalityCode(privatePractitioner.getMunicipalityCode())
            .countyCode(privatePractitioner.getCountyCode())
            .build();
    }
}
