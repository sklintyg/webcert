/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.notification_sender.notifications.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.webcert.common.Constants;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.NotificationRedeliveryMessage;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

// @Component
public class NotificationRedeliveryJob {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryJob.class);
    private static final String JOB_NAME = "NotificationRedeliveryJob.run";
    private static final String LOCK_AT_MOST = "PT2M";
    private static final String LOCK_AT_LEAST = "PT1M";

    @Autowired
    private HandelseRepository handelseRepository;

    @Autowired
    private NotificationRedeliveryRepository notificationRedeliveryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UtkastRepository draftRepository;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    @Qualifier("jmsTemplateNotificationWSSender")
    private JmsTemplate jmsTemplate;

    @Value("${notification.redelivery.xml.local.part}")
    private String xmlLocalPart;

    @Value("${notification.redelivery.xml.namespace.url}")
    private String xmlNamespaceUrl;

    // @Scheduled(cron = "${job.notification.redelivery.cron}")
    // @SchedulerLock(name = JOB_NAME, lockAtLeastFor = LOCK_AT_LEAST, lockAtMostFor = LOCK_AT_MOST)
    public void run() {
        LOG.info("Running job for notificaition redelivery...");

        List<NotificationRedelivery> redeliveryList = notificationRedeliveryRepository.findByRedeliveryTimeLessThan(LocalDateTime.now());
        redeliveryList.sort(Comparator.comparing(NotificationRedelivery::getRedeliveryTime));

        for (NotificationRedelivery redelivery : redeliveryList) {

            try {
                Handelse event = handelseRepository.findById(redelivery.getEventId()).orElseThrow();

                NotificationRedeliveryMessage redeliveryMessage = objectMapper.readValue(redelivery.getMessage(),
                    NotificationRedeliveryMessage.class);

                CertificateStatusUpdateForCareType statusUpdate = redeliveryMessage.get();
                setCertificateOnStatusUpdateIfRequired(statusUpdate, redeliveryMessage);

                String statusUpdateXml = marshal(statusUpdate);

                LOG.info("Notification redelivery job (re)sending status update for care [notificationId: {}, event: {}, logicalAddress: {}"
                    + ", correlationId: {}]", event.getId(), event.getCode(), event.getEnhetsId(), redelivery.getCorrelationId());

                jmsTemplate.convertAndSend(statusUpdateXml.getBytes(), jmsMessage -> setJmsMessageHeaders(jmsMessage, redelivery, event));

            } catch (NoSuchElementException e) {
                LOG.error(getLogInfoString(redelivery) + ". Could not find a corresponding event in table Handelse.", e);
            } catch (IOException | ModuleException | ModuleNotFoundException e) {
                LOG.error(getLogInfoString(redelivery) + ". Error setting a certificate on status update object.", e);
            } catch (Exception e) {
                LOG.error(getLogInfoString(redelivery) + ". An exception occurred.", e);
            }
        }
    }

    private void setCertificateOnStatusUpdateIfRequired(CertificateStatusUpdateForCareType statusUpdate,
        NotificationRedeliveryMessage redeliveryMessage) throws ModuleNotFoundException, IOException, ModuleException {
        if (redeliveryMessage.isForSignedCertificate()) {

            // TODO Perhaps move redelivery service etc to web module
            Utkast draft = draftRepository.findByIntygsIdAndIntygsTyp(redeliveryMessage.getCertId(), redeliveryMessage.getCertType());
            ModuleApi moduleApi = moduleRegistry.getModuleApi(draft.getIntygsTyp(), draft.getIntygTypeVersion());
            Utlatande utlatande = moduleApi.getUtlatandeFromJson(draft.getModel());

            Intyg certificate = moduleApi.getIntygFromUtlatande(utlatande);
            certificate.setPatient(redeliveryMessage.getPatient());
            statusUpdate.setIntyg(certificate);
        }
    }

    private String marshal(CertificateStatusUpdateForCareType statusUpdate) {
        QName qName = new QName(xmlNamespaceUrl, xmlLocalPart);
        JAXBElement<CertificateStatusUpdateForCareType> jaxbElement =
            new JAXBElement<>(qName, CertificateStatusUpdateForCareType.class, JAXBElement.GlobalScope.class, statusUpdate);

        return XmlMarshallerHelper.marshal(jaxbElement);
    }

    private Message setJmsMessageHeaders(Message jmsMessage, NotificationRedelivery redelivery, Handelse event) {
        try {
            jmsMessage.setStringProperty(NotificationRouteHeaders.CORRELATION_ID, redelivery.getCorrelationId());
            jmsMessage.setStringProperty(NotificationRouteHeaders.INTYGS_ID, event.getIntygsId());
            jmsMessage.setStringProperty(NotificationRouteHeaders.LOGISK_ADRESS, event.getEnhetsId());
            jmsMessage.setStringProperty(NotificationRouteHeaders.USER_ID, event.getHanteratAv());
            jmsMessage.setLongProperty(Constants.JMS_TIMESTAMP, Instant.now().getEpochSecond());
            return jmsMessage;
        } catch (JMSException e) {
            throw new RuntimeException(String.format("Failure resending message [notificationId: %s, event: %s, "
                + "logicalAddress: %s, correlationId: %s]. Error building the JMS message for redelivery.", event.getId(), event.getCode(),
                event.getEnhetsId(), redelivery.getCorrelationId()), e);
        }
    }

    private String getLogInfoString(NotificationRedelivery redelivery) {
        return String.format("Failure resending message [notificationId: %s, correlationId: %s]", redelivery.getEventId(),
            redelivery.getCorrelationId());
    }
}
