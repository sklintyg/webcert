package se.inera.intyg.webcert.web.csintegration.integration.configuration;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import se.inera.intyg.webcert.logging.MdcHelper;

import static se.inera.intyg.webcert.logging.MdcLogConstants.SESSION_ID_KEY;
import static se.inera.intyg.webcert.logging.MdcLogConstants.TRACE_ID_KEY;

@Configuration
public class CertificateServiceRestClientConfiguration {
    @Bean("csRestClient")
    public RestClient csRestClient() {
        return RestClient.builder().build();
    }
}
