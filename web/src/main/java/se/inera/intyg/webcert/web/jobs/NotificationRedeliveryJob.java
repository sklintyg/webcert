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

package se.inera.intyg.webcert.web.jobs;

import static se.inera.intyg.webcert.common.Constants.JMS_TIMESTAMP;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.CORRELATION_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.INTYGS_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.LOGISK_ADRESS;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.USER_ID;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationRedeliveryService;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationRedeliveryMessage;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@Component
public class NotificationRedeliveryJob {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryJob.class);

    private final IntygModuleRegistry moduleRegistry;
    private final NotificationRedeliveryService notificationRedeliveryService;
    private final IntygService certService;
    private final ObjectMapper objectMapper;
    private final JmsTemplate jmsTemplate;
    private final UtkastService draftService;

    private static final String JOB_NAME = "NotificationRedeliveryJob.run";
    private static final String LOCK_AT_MOST = "PT59S";
    private static final String LOCK_AT_LEAST = "PT55S";

    @Value("${notification.redelivery.xml.local.part}")
    private String xmlLocalPart;

    @Value("${notification.redelivery.xml.namespace.url}")
    private String xmlNamespaceUrl;

    public NotificationRedeliveryJob(IntygModuleRegistry moduleRegistry, NotificationRedeliveryService notificationRedeliveryService,
        IntygService certService, ObjectMapper objectMapper, @Qualifier("jmsTemplateNotificationWSSender") JmsTemplate jmsTemplate,
        UtkastService draftService) {
        this.moduleRegistry = moduleRegistry;
        this.certService = certService;
        this.notificationRedeliveryService = notificationRedeliveryService;
        this.objectMapper = objectMapper;
        this.draftService = draftService;
        this.jmsTemplate = jmsTemplate;
    }

    @Scheduled(cron = "${job.notification.redelivery.cron:-}")
    @SchedulerLock(name = JOB_NAME, lockAtLeastFor = LOCK_AT_LEAST, lockAtMostFor = LOCK_AT_MOST)
    public void run() {
        LOG.info("Running notification redelivery job...");

        final List<NotificationRedelivery> redeliveryList = notificationRedeliveryService.getNotificationsForRedelivery();

        for (NotificationRedelivery redelivery : redeliveryList) {
            // TODO Handle cases where there is is no message attached to the redelivery (triggered by manual resend)
            // Can perhaps use the full flow from notification service(?)

            // TODO Investigate the perceived behaviour where ts certificates appear to never be found in
            // the webcert database. When does this happen and why?
            try {
                final Handelse event = notificationRedeliveryService.getEventById(redelivery.getEventId());

                if (notificationRedeliveryService.isRedundantRedelivery(event)) {
                    notificationRedeliveryService.abortRedundantRedelivery(event, redelivery);
                } else {
                    final NotificationRedeliveryMessage redeliveryMessage = objectMapper.readValue(redelivery.getMessage(),
                        NotificationRedeliveryMessage.class);

                    final CertificateStatusUpdateForCareType statusUpdate = redeliveryMessage.get();
                    setCertificateIfRequired(statusUpdate, redeliveryMessage);

                    final String statusUpdateXml = marshal(statusUpdate);

                    LOG.info("Initiating redelivery of status update for care [notificationId: {}, event: {}, logicalAddress: {}"
                        + ", correlationId: {}]", event.getId(), event.getCode(), event.getEnhetsId(), redelivery.getCorrelationId());

                    jmsTemplate
                        .convertAndSend(statusUpdateXml.getBytes(), jmsMessage -> setJmsMessageHeaders(jmsMessage, redelivery, event));
                }

            // TODO Sort out these exception with regard to resend or fail, calls to service for execution
            } catch (NoSuchElementException e) { //when no handelse exists
                LOG.error(getLogInfoString(redelivery) + "Could not find a corresponding event in table Handelse.", e);
                //notificationRedeliveryService.setNotificationFailure(redelivery.getEventId(), redelivery.getCorrelationId());
            } catch (IOException | ModuleException | ModuleNotFoundException e) {
                LOG.error(getLogInfoString(redelivery) + "Error setting a certificate on status update object.", e);
                //notificationRedeliveryService.setNotificationFailure(redelivery.getEventId(), redelivery.getCorrelationId());
            } catch (WebCertServiceException e) {
                LOG.error(e.getMessage(), e);
                //notificationRedeliveryService.setNotificationFailure(redelivery.getEventId(), redelivery.getCorrelationId());
            } catch (Exception e) {
                LOG.error(getLogInfoString(redelivery) + "An exception occurred.", e);
                //notificationRedeliveryService.setNotificationFailure(redelivery.getEventId(), redelivery.getCorrelationId());
            }
        }
    }

    private void setCertificateIfRequired(CertificateStatusUpdateForCareType statusUpdate,
        NotificationRedeliveryMessage redeliveryMessage) throws ModuleNotFoundException, IOException, ModuleException {

        if (redeliveryMessage.isForSignedCertificate()) {

            if (!getCertificateFromWebcert(statusUpdate, redeliveryMessage)) {

                getCertificateFromIntygstjanst(statusUpdate, redeliveryMessage);
            }
        }
    }

    private boolean getCertificateFromWebcert(CertificateStatusUpdateForCareType statusUpdate,
        NotificationRedeliveryMessage redeliveryMessage) throws ModuleNotFoundException, ModuleException, IOException {
        try {
            Utkast draft = draftService.getDraft(redeliveryMessage.getCertId(), redeliveryMessage.getCertType(), false);
            ModuleApi moduleApi = moduleRegistry.getModuleApi(redeliveryMessage.getCertType(), redeliveryMessage.getVersion());
            Utlatande utlatande = moduleApi.getUtlatandeFromJson(draft.getModel());
            Intyg certificate = moduleApi.getIntygFromUtlatande(utlatande);
            certificate.setPatient(redeliveryMessage.getPatient());
            statusUpdate.setIntyg(certificate);
            return true;
        } catch (WebCertServiceException e) {
            LOG.warn("Could not find certificate {} of type {} in webcert's database. Will check intygstjanst...",
                redeliveryMessage.getCertId(), redeliveryMessage.getCertType(), e);
            return false;
        }
    }

    private void getCertificateFromIntygstjanst(CertificateStatusUpdateForCareType statusUpdate,
        NotificationRedeliveryMessage redeliveryMessage) throws ModuleNotFoundException, ModuleException, WebCertServiceException {
        try {
            IntygContentHolder certContentHolder = certService.fetchIntygDataForInternalUse(redeliveryMessage.getCertId(), true);
            Utlatande utlatande = certContentHolder.getUtlatande();
            ModuleApi moduleApi = moduleRegistry.getModuleApi(Objects.requireNonNull(utlatande).getTyp(), redeliveryMessage.getVersion());
            Intyg certificate = moduleApi.getIntygFromUtlatande(utlatande);
            certificate.setPatient(redeliveryMessage.getPatient());
            statusUpdate.setIntyg(certificate);
        } catch (WebCertServiceException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                String.format("Could not find certificate id: %s of type %s in intygstjanst's database", redeliveryMessage.getCertId(),
                    redeliveryMessage.getCertType()), e);
        }
    }

    private String marshal(CertificateStatusUpdateForCareType statusUpdate) {
        final QName qName = new QName(xmlNamespaceUrl, xmlLocalPart);
        final JAXBElement<CertificateStatusUpdateForCareType> jaxbElement =
            new JAXBElement<>(qName, CertificateStatusUpdateForCareType.class, JAXBElement.GlobalScope.class, statusUpdate);

        return XmlMarshallerHelper.marshal(jaxbElement);
    }

    private Message setJmsMessageHeaders(Message jmsMessage, NotificationRedelivery redelivery, Handelse event) {
         try {
            jmsMessage.setStringProperty(CORRELATION_ID, redelivery.getCorrelationId());
            jmsMessage.setStringProperty(INTYGS_ID, event.getIntygsId());
            jmsMessage.setStringProperty(LOGISK_ADRESS, event.getEnhetsId());
            jmsMessage.setStringProperty(USER_ID, event.getHanteratAv());
            jmsMessage.setLongProperty(JMS_TIMESTAMP, Instant.now().getEpochSecond());
            return jmsMessage;
        } catch (JMSException e) {
            throw new RuntimeException(String.format("Failure resending message [notificationId: %s, event: %s, "
                + "logicalAddress: %s, correlationId: %s]. Exception occurred setting JMs message headers.", event.getId(), event.getCode(),
                event.getEnhetsId(), redelivery.getCorrelationId()), e);
        }
    }

    private String getLogInfoString(NotificationRedelivery redelivery) {
        return String.format("Failure resending message [notificationId: %s, correlationId: %s]. ", redelivery.getEventId(),
            redelivery.getCorrelationId());
    }
}
