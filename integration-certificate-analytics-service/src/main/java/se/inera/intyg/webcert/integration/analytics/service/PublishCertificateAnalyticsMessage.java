/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.integration.analytics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.logging.MdcLogConstants;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublishCertificateAnalyticsMessage {

    private final CertificateAnalyticsServiceProfile certificateAnalyticsServiceProfile;
    private final JmsTemplate jmsTemplateForCertificateAnalyticsMessages;

    public void publishEvent(CertificateAnalyticsMessage message) {
        if (!certificateAnalyticsServiceProfile.isEnabled()) {
            log.debug("Certificate analytics service is not enabled - not publishing message");
            return;
        }

        jmsTemplateForCertificateAnalyticsMessages.convertAndSend(message, msg -> {
                msg.setStringProperty("messageId", message.getMessageId());
                msg.setStringProperty("sessionId", MDC.get(MdcLogConstants.SESSION_ID_KEY));
                msg.setStringProperty("traceId", MDC.get(MdcLogConstants.TRACE_ID_KEY));
                msg.setStringProperty("_type", message.getType());
                msg.setStringProperty("schemaVersion", message.getSchemaVersion());
                msg.setStringProperty("contentType", "application/json");
                msg.setStringProperty("messageType", message.getMessageType().toString());
                return msg;
            }
        );
    }
}
