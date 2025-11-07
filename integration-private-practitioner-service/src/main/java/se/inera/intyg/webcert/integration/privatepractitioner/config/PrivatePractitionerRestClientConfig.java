package se.inera.intyg.webcert.integration.privatepractitioner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PrivatePractitionerRestClientConfig {

  @Value("${privatepractitioner.internalapi.validate.url}")
  private String internalApiValidatePrivatePractitionerUrl;

  @Bean(name = "ppsRestClient")
  public RestClient ppsRestClient() {
    return RestClient.create(internalApiValidatePrivatePractitionerUrl);
  }

}
