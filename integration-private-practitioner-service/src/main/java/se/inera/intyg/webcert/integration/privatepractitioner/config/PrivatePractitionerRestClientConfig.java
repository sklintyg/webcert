package se.inera.intyg.webcert.integration.privatepractitioner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class PrivatePractitionerRestClientConfig {

    @Value("${privatepractitioner.api.url}")
    private String internalApiValidatePrivatePractitionerUrl;
    private static final String CONFIG_PATH = "/config";

    @Bean(name = "ppsRestClient")
    public RestClient ppsRestClient() {
        return RestClient.builder()
            .baseUrl(internalApiValidatePrivatePractitionerUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
    }

    @Bean(name = "validatePpsRestClient")
    public RestClient validatePpsRestClient() {
        return RestClient.create(internalApiValidatePrivatePractitionerUrl);
    }

    @Bean(name = "configPpsRestClient")
    public RestClient configPpsRestClient() {
        return RestClient.create(internalApiValidatePrivatePractitionerUrl);
    }

}
