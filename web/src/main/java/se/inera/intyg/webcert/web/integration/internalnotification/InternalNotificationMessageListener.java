/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.internalnotification;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.service.notification.NotificationService;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import static com.google.common.base.Preconditions.checkArgument;

@Service
public class InternalNotificationMessageListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(InternalNotificationMessageListener.class);

    static final String CERTIFICATE_ID = "certificate-id";
    static final String CERTIFICATE_TYPE = "certificate-type";
    static final String CARE_UNIT_ID = "care-unit-id";

    private static final String INTERNAL_NOTIFICATION_QUEUE = "internal.notification.queue";

    @Autowired
    private IntygModuleRegistry intygModuleRegistry;

    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    @Autowired
    private NotificationService notificationService;

    @Value("${intygstjanst.logicaladdress}")
    private String logicalAddress;

    // How to fix hard-coding in JmsListener?
    @Override
    @JmsListener(destination = INTERNAL_NOTIFICATION_QUEUE)
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            try {
                String intygsId = textMessage.getStringProperty(CERTIFICATE_ID);
                String intygsTyp = textMessage.getStringProperty(CERTIFICATE_TYPE);
                String enhetsId = textMessage.getStringProperty(CARE_UNIT_ID);

                checkArgument(StringUtils.isNotEmpty(intygsId), "Message on queue %s does not have a %s header.",
                        INTERNAL_NOTIFICATION_QUEUE, CERTIFICATE_ID);
                checkArgument(StringUtils.isNotEmpty(intygsTyp), "Message on queue %s does not have a %s header.",
                        INTERNAL_NOTIFICATION_QUEUE, CERTIFICATE_TYPE);
                checkArgument(StringUtils.isNotEmpty(enhetsId), "Message on queue %s does not have a %s header.",
                        INTERNAL_NOTIFICATION_QUEUE, CARE_UNIT_ID);

                if (!integreradeEnheterRegistry.isEnhetIntegrerad(enhetsId, intygsTyp)) {
                    LOG.debug("Not forwarding internal notification to care system, care unit '{}' is not integrated.", enhetsId);
                    return;
                }
                ModuleApi moduleApi = intygModuleRegistry.getModuleApi(intygsTyp);
                CertificateResponse certificateResponse = moduleApi.getCertificate(intygsId, logicalAddress, "HSVARD");

                Utlatande utlatande = certificateResponse.getUtlatande();
                notificationService.forwardInternalNotification(utlatande.getId(), utlatande.getTyp(), utlatande, HandelsekodEnum.SKICKA);

            } catch (IllegalArgumentException e) {
                LOG.error("Could not process internal notification, message is missing required header: {}", e.getMessage());
            } catch (JMSException | ModuleNotFoundException | ModuleException e) {
                LOG.error("Caught {} transforming internal notification to external notification. Message: {}", e.getMessage());
            }
        }
    }
}
