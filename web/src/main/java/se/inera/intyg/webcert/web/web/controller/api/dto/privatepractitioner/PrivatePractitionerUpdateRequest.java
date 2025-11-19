package se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerDetails.PrivatePractitionerDetailsBuilder;

@Value
@Builder
@JsonDeserialize(builder = PrivatePractitionerDetailsBuilder.class)
public class PrivatePractitionerUpdateRequest {

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

    @JsonPOJOBuilder(withPrefix = "")
    public static class PrivatePractitionerUpdateRequestBuilder {

    }
}
