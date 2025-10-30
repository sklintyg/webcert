package se.inera.intyg.webcert.web.csintegration.integration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@JsonDeserialize(builder = GetCertificateTypeVersionsResponse.GetCertificateTypeVersionsResponseBuilder.class)
@Value
@Builder
public class GetCertificateTypeVersionsResponse {

    List<CertificateModelIdDTO> certificateModelIds;

    @JsonPOJOBuilder(withPrefix = "")
    public static class GetCertificateTypeVersionsResponseBuilder {

    }
}
