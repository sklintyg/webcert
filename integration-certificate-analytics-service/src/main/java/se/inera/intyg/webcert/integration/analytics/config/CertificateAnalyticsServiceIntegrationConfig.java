package se.inera.intyg.webcert.integration.analytics.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@ComponentScan(basePackages = {
    "se.inera.intyg.webcert.integration.analytics"
})
public class CertificateAnalyticsServiceIntegrationConfig {

    public CertificateAnalyticsServiceIntegrationConfig() {
        log.info("Setting 'certificate-analytics-service-integration' to be used for certificate analytics.");
    }
}
