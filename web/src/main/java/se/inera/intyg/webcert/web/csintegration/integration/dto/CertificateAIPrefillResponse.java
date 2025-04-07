package se.inera.intyg.webcert.web.csintegration.integration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.common.support.facade.model.Certificate;

@JsonDeserialize(builder = CertificateAIPrefillResponse.CertificateAIPrefillResponseBuilder.class)
@Value
@Builder
public class CertificateAIPrefillResponse {

    Certificate certificate;

    @JsonPOJOBuilder(withPrefix = "")
    public static class CertificateAIPrefillResponseBuilder {

    }
}
