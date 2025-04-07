package se.inera.intyg.webcert.web.csintegration.integration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitDTO;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserDTO;

@JsonDeserialize(builder = CertificateAIPrefillRequest.CertificateAIPrefillRequestBuilder.class)
@Value
@Builder
public class CertificateAIPrefillRequest {

    CertificateServiceUserDTO user;
    CertificateServiceUnitDTO unit;
    CertificateServiceUnitDTO careUnit;
    CertificateServiceUnitDTO careProvider;
    String preFillData;

    @JsonPOJOBuilder(withPrefix = "")
    public static class CertificateAIPrefillRequestBuilder {

    }
}
