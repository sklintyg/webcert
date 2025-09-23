package se.inera.intyg.webcert.integration.analytics.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.integration.analytics.model.CertificateEventMessage;

@Service
@Slf4j
public class PublishCertificateEventMessage {

    public void publishEvent(CertificateEventMessage message) {
        log.info("Publishing certificate event message: {}", message);
    }
}
