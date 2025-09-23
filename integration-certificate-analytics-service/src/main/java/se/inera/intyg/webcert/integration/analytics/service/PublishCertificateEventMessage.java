package se.inera.intyg.webcert.integration.analytics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.integration.analytics.model.CertificateEventMessage;
import se.inera.intyg.webcert.logging.MdcLogConstants;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublishCertificateEventMessage {

    private final JmsTemplate jmsTemplateForCertificateEvent;

    public void publishEvent(CertificateEventMessage message) {
        jmsTemplateForCertificateEvent.convertAndSend(message, msg -> {
                msg.setStringProperty("sessionId", MDC.get(MdcLogConstants.SESSION_ID_KEY));
                msg.setStringProperty("traceId", MDC.get(MdcLogConstants.TRACE_ID_KEY));
                msg.setStringProperty("spanId", MDC.get(MdcLogConstants.SPAN_ID_KEY));
                msg.setStringProperty("schemaVersion", "v1");
                msg.setStringProperty("contentType", "application/json");
                msg.setStringProperty("eventType", message.getType().toString());
                return msg;
            }
        );
    }
}
