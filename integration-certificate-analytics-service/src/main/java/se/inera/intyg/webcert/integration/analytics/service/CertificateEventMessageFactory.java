package se.inera.intyg.webcert.integration.analytics.service;

import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.integration.analytics.model.CertificateEventMessage;
import se.inera.intyg.webcert.integration.analytics.model.CertificateEventMessageType;

@Component
public class CertificateEventMessageFactory {

    public CertificateEventMessage draftCreated(Certificate certificate) {
        return create(certificate, CertificateEventMessageType.DRAFT_CREATED);
    }

    public CertificateEventMessage certificateSigned(Certificate certificate) {
        return create(certificate, CertificateEventMessageType.CERTIFICATE_SIGNED);
    }

    public CertificateEventMessage certificateSent(Certificate certificate) {
        return create(certificate, CertificateEventMessageType.CERTIFICATE_SENT);
    }

    private CertificateEventMessage create(Certificate certificate, CertificateEventMessageType type) {
        return CertificateEventMessage.builder()
            .certificateId(certificate.getMetadata().getId())
            .type(type)
            .build();
    }
}
