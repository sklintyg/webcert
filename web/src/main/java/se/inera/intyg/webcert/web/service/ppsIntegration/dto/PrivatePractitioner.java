package se.inera.intyg.webcert.web.service.ppsIntegration.dto;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Builder
@Value
public class PrivatePractitioner {

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
}
