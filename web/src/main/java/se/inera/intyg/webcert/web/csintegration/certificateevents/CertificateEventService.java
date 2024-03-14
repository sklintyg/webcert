/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.csintegration.certificateevents;

import static se.inera.intyg.webcert.web.csintegration.certificateevents.CertificateEventType.CERTIFICATE_ID;
import static se.inera.intyg.webcert.web.csintegration.certificateevents.CertificateEventType.EVENT_TYPE;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CertificateEventService {

    private final JmsTemplate jmsCertificateEventTemplate;

    public CertificateEventService(
        @Qualifier("jmsCertificateEventTemplate") JmsTemplate jmsCertificateEventTemplate) {
        this.jmsCertificateEventTemplate = jmsCertificateEventTemplate;
    }

    public boolean send(CertificateEventMessage certificateEventMessage) {

        try {
            jmsCertificateEventTemplate.send(session -> {
                final var textMessage = session.createTextMessage();
                textMessage.setStringProperty(CERTIFICATE_ID, certificateEventMessage.getCertificateId());
                textMessage.setStringProperty(EVENT_TYPE, certificateEventMessage.getEventType());
                return textMessage;
            });
            return true;
        } catch (Exception e) {
            log.warn("Exception occured while trying to send NotificationCertificateEvent for certificate: '{}'",
                certificateEventMessage.getCertificateId(), e);
            return false;
        }
    }
}
