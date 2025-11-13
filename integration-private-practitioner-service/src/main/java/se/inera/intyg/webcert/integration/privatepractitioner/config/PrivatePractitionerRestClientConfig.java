package se.inera.intyg.webcert.integration.privatepractitioner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

@Configuration
@Profile("private-practitioner-service-active")
@ComponentScan(basePackages = {"se.inera.intyg.webcert.integration.privatepractitioner"})
public class PrivatePractitionerRestClientConfig {

    @Value("${privatepractitionerservice.base.url}")
    private String privatePractitionerServiceBaseUrl;
    public static final String CONFIG_PATH = "/configuration";
    public static final String VALIDATE_PATH = "/validate";
    public static final String HOSP_INFO_PATH = "/hosp";

    @Bean(name = "ppsRestClient")
    public RestClient ppsRestClient() {
        return RestClient.create(privatePractitionerServiceBaseUrl);
    }

}
