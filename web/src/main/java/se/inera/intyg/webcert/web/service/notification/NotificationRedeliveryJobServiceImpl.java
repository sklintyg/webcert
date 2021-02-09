/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.notification;

import static se.inera.intyg.common.support.Constants.HSA_ID_OID;
import static se.inera.intyg.webcert.common.Constants.JMS_TIMESTAMP;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.CORRELATION_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.INTYGS_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.LOGISK_ADRESS;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.USER_ID;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.infra.integration.hsa.services.HsaOrganizationsService;
import se.inera.intyg.infra.integration.hsa.services.HsaPersonService;
import se.inera.intyg.infra.security.authorities.FeaturesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.common.service.notification.AmneskodCreator;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationRedeliveryMessage;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationTypeConverter;
import se.inera.intyg.webcert.notification_sender.notifications.services.redelivery.NotificationRedeliveryService;
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.CertificateStatusUpdateForCareCreator;
import se.inera.intyg.webcert.notification_sender.notifications.util.NotificationRedeliveryUtil;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.referens.ReferensService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@Service
public class NotificationRedeliveryJobServiceImpl implements NotificationRedeliveryJobService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryJobServiceImpl.class);

    @Autowired
    private IntygModuleRegistry moduleRegistry;
    @Autowired
    private NotificationRedeliveryService notificationRedeliveryService;
    @Autowired
    private IntygService certificateService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    @Qualifier("jmsTemplateNotificationWSSender")
    private JmsTemplate jmsTemplate;
    @Autowired
    private UtkastService draftService;
    @Autowired
    private UtkastRepository draftRepo;
    @Autowired
    private SendNotificationStrategy sendNotificationStrategy;
    @Autowired
    private ReferensService referenceService;
    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;
    @Autowired
    private HsaPersonService hsaPersonService;
    @Autowired
    private FeaturesHelper featuresHelper;
    @Autowired
    private CertificateStatusUpdateForCareCreator certificateStatusUpdateForCareCreator;
    @Autowired
    private NotificationMessageFactory notificationMessageFactory;

    //@Value("${notification.redelivery.xml.local.part}")
    private String xmlLocalPart = "CertificateStatusUpdateForCare";

    //@Value("${notification.redelivery.xml.namespace.url}")
    private String xmlNamespaceUrl = "urn:riv:clinicalprocess:healthcond:certificate:CertificateStatusUpdateForCareResponder:3";

    @Override
    public void resendNotifications() {

        // TODO Add handling of delays in the queues to make sure events don't get redelivered twice.
        // TODO Add handling of job not finished when scheduled to run next time.

        final long startTime = System.currentTimeMillis();

        // TODO Move method to job service.
        final List<NotificationRedelivery> redeliveryList = notificationRedeliveryService.getNotificationsForRedelivery();
        redeliveryList.sort(Comparator.comparing(NotificationRedelivery::getEventId));

        int failedRedeliveries = initiateNotificationRedeliveries(redeliveryList);

        final long endTime = System.currentTimeMillis();

        LOG.info("Initiated {} notification redeliveries in {} milliseconds. Number of initiation failures: {}.", redeliveryList.size(),
            endTime - startTime, failedRedeliveries);
    }

    private int initiateNotificationRedeliveries(List<NotificationRedelivery> redeliveryList) {

        int remainingRedeliveries = redeliveryList.size();

        for (NotificationRedelivery redelivery : redeliveryList) {
            remainingRedeliveries = redeliver(redelivery, remainingRedeliveries);
        }

        return remainingRedeliveries;
    }

    private int redeliver(NotificationRedelivery notificationRedelivery, int remainingRedeliveries) {
        try {
            final Handelse event = notificationRedeliveryService.getEventById(notificationRedelivery.getEventId());

            // TODO: Need to handle if no manual delivery should be done.... statusUpdate === null.
            final var statusUpdate = getCertificateStatusUpdate(notificationRedelivery, event);

            sendJmsMessage(statusUpdate, event, notificationRedelivery);

            // If the feature is not active, then make sure that the redelivery is removed.
            if (!featuresHelper.isFeatureActive(AuthoritiesConstants.FEATURE_USE_WEBCERT_MESSAGING)) {
                // TODO: Change name of method.
                notificationRedeliveryService.setSentWithV3Client(event, notificationRedelivery);
            }

            // TODO: How do we update the redelivery once it is delivered.

            remainingRedeliveries--;

            // TODO Sort out these exception with regard to resend or fail, and which action to perform.
        } catch (NoSuchElementException e) { //when no handelse exists
            LOG.error(getLogInfoString(notificationRedelivery) + "Could not find a corresponding event in table Handelse.", e);
            //notificationRedeliveryService.setNotificationFailure(redelivery.getEventId(), redelivery.getCorrelationId());
        } catch (IOException | ModuleException | ModuleNotFoundException e) {
            LOG.error(getLogInfoString(notificationRedelivery) + "Error setting a certificate on status update object.", e);
            //notificationRedeliveryService.setNotificationFailure(redelivery.getEventId(), redelivery.getCorrelationId());
        } catch (WebCertServiceException e) {
            LOG.error(e.getMessage(), e);
            //notificationRedeliveryService.setNotificationFailure(redelivery.getEventId(), redelivery.getCorrelationId());
        } catch (Exception e) {
            LOG.error(getLogInfoString(notificationRedelivery) + "An exception occurred.", e);
            //notificationRedeliveryService.setNotificationFailure(redelivery.getEventId(), redelivery.getCorrelationId());
        }
        return remainingRedeliveries;
    }

    private CertificateStatusUpdateForCareType getCertificateStatusUpdate(NotificationRedelivery redelivery, Handelse event)
        throws IOException, ModuleException, ModuleNotFoundException, TemporaryException {
        // TODO: Check if it contains a redelivery message. Correlation id will be set.
        if (redelivery.getCorrelationId() == null) {
            return createManualNotification(event, redelivery);
        } else {
            final NotificationRedeliveryMessage redeliveryMessage = objectMapper.readValue(redelivery.getMessage(),
                NotificationRedeliveryMessage.class);
            final var statusUpdate = redeliveryMessage.getV3();
            completeStatusUpdate(statusUpdate, redeliveryMessage, event);
            return statusUpdate;
        }
    }

    private void completeStatusUpdate(CertificateStatusUpdateForCareType statusUpdate, NotificationRedeliveryMessage redeliveryMessage,
        Handelse event) throws ModuleNotFoundException, IOException, ModuleException {

        Intyg certificate;
        if (!redeliveryMessage.hasCertificate()) {
            certificate = getCertificateFromWebcert(event.getIntygsId(), event.getCertificateType(), event.getCertificateVersion());
            if (certificate == null) {
                certificate = getCertificateFromIntygstjanst(event.getIntygsId(), event.getCertificateType(),
                    event.getCertificateVersion());
            }
            certificate.setPatient(redeliveryMessage.getPatient());
            NotificationTypeConverter.complementIntyg(certificate);
            statusUpdate.setIntyg(certificate);
        }

        statusUpdate.setHanteratAv(NotificationRedeliveryUtil.getIIType(new HsaId(), event.getHanteratAv(), HSA_ID_OID));
        statusUpdate.setHandelse(NotificationRedeliveryUtil.getEventV3(event.getCode(), event.getTimestamp(), event.getAmne(),
            event.getSistaDatumForSvar()));
    }

    private Intyg getCertificateFromWebcert(String certificateId, String certificateType, String certificateVersion)
        throws ModuleNotFoundException, ModuleException, IOException {
        try {
            Utkast draft = draftService.getDraft(certificateId, moduleRegistry.getModuleIdFromExternalId(certificateType), false);
            ModuleApi moduleApi = moduleRegistry.getModuleApi(moduleRegistry.getModuleIdFromExternalId(certificateType),
                certificateVersion);
            Utlatande utlatande = moduleApi.getUtlatandeFromJson(draft.getModel());
            return moduleApi.getIntygFromUtlatande(utlatande);
        } catch (WebCertServiceException e) {
            LOG.warn("Could not find certificate {} of type {} in webcert's database. Will check intygstjanst...", certificateId,
                certificateType, e);
            return null;
        }
    }

    private Intyg getCertificateFromIntygstjanst(String certificateId, String certificateType, String certificateVersion)
        throws ModuleNotFoundException, ModuleException, WebCertServiceException {
        try {
            IntygContentHolder certContentHolder = certificateService.fetchIntygDataForInternalUse(certificateId, true);
            Utlatande utlatande = certContentHolder.getUtlatande();
            ModuleApi moduleApi = moduleRegistry.getModuleApi(moduleRegistry.getModuleIdFromExternalId(certificateType),
                certificateVersion);
            return moduleApi.getIntygFromUtlatande(utlatande);
        } catch (WebCertServiceException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                String.format("Could not find certificate id: %s of type %s in intygstjanst's database", certificateId,
                    certificateType), e);
        }
    }

    private String marshal(CertificateStatusUpdateForCareType statusUpdate) {
        final QName qName = new QName(xmlNamespaceUrl, xmlLocalPart);
        final JAXBElement<CertificateStatusUpdateForCareType> jaxbElement =
            new JAXBElement<>(qName, CertificateStatusUpdateForCareType.class, JAXBElement.GlobalScope.class, statusUpdate);

        return XmlMarshallerHelper.marshal(jaxbElement);
    }

    private String getLogInfoString(NotificationRedelivery redelivery) {
        return String.format("Failure resending message [notificationId: %s, correlationId: %s]. ", redelivery.getEventId(),
            redelivery.getCorrelationId());
    }

    private CertificateStatusUpdateForCareType createManualNotification(Handelse event, NotificationRedelivery redelivery)
        throws ModuleNotFoundException, IOException, ModuleException, TemporaryException {

        // If it is a failure it can be resent, otherwise it can´t... but should we let all events to be resent? No, this is probably a good idea.
        if (event.getDeliveryStatus() != NotificationDeliveryStatusEnum.FAILURE) {
            return null;
        }

        final var statusUpdate = createStatusUpdate(event);

        // Create a correlation id for the redelivery.
        // TODO: This can be moved to redelivery service.
        redelivery.setCorrelationId(UUID.randomUUID().toString());

        // Set that the event will be resent
        event.setDeliveryStatus(NotificationDeliveryStatusEnum.RESEND);

        // This saves changes to the event and redelivery
        // Shall this be done after it has been sent?
        notificationRedeliveryService.initiateManualNotification(redelivery, event);

        return statusUpdate;
    }

    private CertificateStatusUpdateForCareType createStatusUpdate(Handelse event)
        throws TemporaryException, ModuleNotFoundException, IOException, ModuleException {
        if (event.getCode() == HandelsekodEnum.RADERA) {
            final var careProvider = hsaOrganizationsService.getVardgivareInfo(event.getVardgivarId());
            final var careUnit = hsaOrganizationsService.getVardenhet(event.getEnhetsId());
            final var personInfo = hsaPersonService.getHsaPersonInfo(event.getCertificateIssuer()).get(0);
            return certificateStatusUpdateForCareCreator.create(event, careProvider, careUnit, personInfo);
        } else {
            final var notificationMessage = createNotificationMessage(event);
            return certificateStatusUpdateForCareCreator.create(notificationMessage, event.getCertificateVersion());
        }
    }

    private NotificationMessage createNotificationMessage(Handelse event)
        throws ModuleNotFoundException, IOException, ModuleException {
        String draftJson;
        final var draft = draftRepo.findById(event.getIntygsId()).orElse(null);
        if (draft != null) {
            draftJson = draft.getModel();
        } else {
            IntygContentHolder certContentHolder = certificateService.fetchIntygDataForInternalUse(event.getIntygsId(), true);
            draftJson = certContentHolder.getContents();
        }

        final var moduleApi = moduleRegistry.getModuleApi(moduleRegistry.getModuleIdFromExternalId(event.getCertificateType()),
            event.getCertificateVersion());
        final var utlatande = moduleApi.getUtlatandeFromJson(draftJson);
        final var schemaVersion = sendNotificationStrategy.decideNotificationForIntyg(utlatande).orElse(SchemaVersion.VERSION_3);
        final var reference = referenceService.getReferensForIntygsId(event.getIntygsId());
        final var topicCode = event.getAmne() != null ? AmneskodCreator.create(event.getAmne().name(), event.getAmne().getDescription())
            : null;

        return notificationMessageFactory.createNotificationMessage(event.getIntygsId(), event.getCertificateType(), event.getEnhetsId(),
            draftJson, event.getCode(), schemaVersion, reference, topicCode, event.getSistaDatumForSvar());
    }

    private void sendJmsMessage(CertificateStatusUpdateForCareType statusUpdate, Handelse event, NotificationRedelivery redelivery) {

        LOG.info("Initiating redelivery of status update for care [notificationId: {}, event: {}, logicalAddress: {}"
            + ", correlationId: {}]", event.getId(), event.getCode(), event.getEnhetsId(), redelivery.getCorrelationId());

        try {
            final var statusUpdateXml = marshal(statusUpdate);

            jmsTemplate.convertAndSend(statusUpdateXml.getBytes(), jmsMessage -> {
                jmsMessage.setStringProperty(CORRELATION_ID, redelivery.getCorrelationId());
                jmsMessage.setStringProperty(INTYGS_ID, event.getIntygsId());
                jmsMessage.setStringProperty(LOGISK_ADRESS, event.getEnhetsId());
                jmsMessage.setStringProperty(USER_ID, event.getHanteratAv());
                jmsMessage.setLongProperty(JMS_TIMESTAMP, Instant.now().getEpochSecond());
                return jmsMessage;
            });
        } catch (JmsException e) {
            throw new RuntimeException(String.format("Failure resending message [notificationId: %s, event: %s, "
                    + "logicalAddress: %s, correlationId: %s]. Exception occurred setting JMs message headers.", event.getId(),
                event.getCode(), event.getEnhetsId(), redelivery.getCorrelationId()), e);
        }
    }
}
