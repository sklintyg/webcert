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

import static se.inera.intyg.common.support.Constants.HSA_ID_OID;
import static se.inera.intyg.webcert.common.Constants.JMS_TIMESTAMP;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.CORRELATION_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.INTYGS_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.IS_MANUAL_REDELIVERY;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.LOGISK_ADRESS;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.USER_ID;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.modules.support.api.notification.FragorOchSvar;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.integration.hsa.services.HsaOrganizationsService;
import se.inera.intyg.infra.integration.hsa.services.HsaPersonService;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.common.service.notification.AmneskodCreator;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationPatientEnricher;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationRedeliveryService;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationRedeliveryMessage;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationTypeConverter;
import se.inera.intyg.webcert.notification_sender.notifications.util.NotificationRedeliveryUtil;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.integration.util.HoSPersonHelper;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.notification.FragorOchSvarCreator;
import se.inera.intyg.webcert.web.service.notification.SendNotificationStrategy;
import se.inera.intyg.webcert.web.service.referens.ReferensService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.infrastructure.directory.v1.PersonInformationType;

@Component
public class NotificationRedeliveryJob {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryJob.class);

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
    private NotificationPatientEnricher notificationPatientEnricher;
    @Autowired
    private FragorOchSvarCreator qaCreator;
    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;
    @Autowired
    private HsaPersonService hsaPersonService;


    private static final String JOB_NAME = "NotificationRedeliveryJob.run";
    private static final String LOCK_AT_MOST = "PT59S";
    private static final String LOCK_AT_LEAST = "PT55S";
    private static final List<HandelsekodEnum> USES_QA = Arrays.asList(HandelsekodEnum.NYFRFM,
        HandelsekodEnum.NYSVFM, HandelsekodEnum.NYFRFV, HandelsekodEnum.HANFRFM,
        HandelsekodEnum.HANFRFV, HandelsekodEnum.MAKULE);


    @Value("${notification.redelivery.xml.local.part}")
    private String xmlLocalPart;

    @Value("${notification.redelivery.xml.namespace.url}")
    private String xmlNamespaceUrl;


    @Scheduled(cron = "${job.notification.redelivery.cron:-}")
    @SchedulerLock(name = JOB_NAME, lockAtLeastFor = LOCK_AT_LEAST, lockAtMostFor = LOCK_AT_MOST)
    public void run() {
        LOG.info("Running notification redelivery job...");

        final List<NotificationRedelivery> redeliveryList = notificationRedeliveryService.getNotificationsForRedelivery();

        for (NotificationRedelivery redelivery : redeliveryList) {

            // TODO Investigate the perceived behaviour where ts certificates appear to never be found in
            // the webcert database. When does this happen and why?
            try {
                final Handelse event = notificationRedeliveryService.getEventById(redelivery.getEventId());

                if (redelivery.getCorrelationId() == null)  {
                    createManualNotification(event, redelivery);
                } else if (notificationRedeliveryService.isRedundantRedelivery(event)) {
                    notificationRedeliveryService.discardRedundantRedelivery(event, redelivery);
                } else {
                    final NotificationRedeliveryMessage redeliveryMessage = objectMapper.readValue(redelivery.getMessage(),
                        NotificationRedeliveryMessage.class);

                    final CertificateStatusUpdateForCareType statusUpdate = redeliveryMessage.getStatusUpdateV3();
                    statusUpdate.setHandelse(NotificationRedeliveryUtil.getEventV3(event.getCode(), event.getTimestamp(), event.getAmne(),
                        event.getSistaDatumForSvar()));
                    statusUpdate.setHanteratAv(NotificationRedeliveryUtil.getIIType(new HsaId(), event.getHanteratAv(), HSA_ID_OID));
                    setCertificateIfRequired(statusUpdate, redeliveryMessage);

                    final String statusUpdateXml = marshal(statusUpdate);

                    LOG.info("Initiating redelivery of status update for care [notificationId: {}, event: {}, logicalAddress: {}"
                        + ", correlationId: {}]", event.getId(), event.getCode(), event.getEnhetsId(), redelivery.getCorrelationId());

                    sendJmsMessage(statusUpdateXml, event, redelivery, false);
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

        Intyg certificate;
        if (redeliveryMessage.hasSignedCertificate()) {
            certificate = getCertificateFromWebcert(redeliveryMessage);
            if (certificate == null) {
                certificate = getCertificateFromIntygstjanst(redeliveryMessage);
            }
            certificate.setPatient(redeliveryMessage.getPatient());
            NotificationTypeConverter.complementIntyg(certificate);
            statusUpdate.setIntyg(certificate);
        }
    }

    private Intyg getCertificateFromWebcert(NotificationRedeliveryMessage redeliveryMessage) throws ModuleNotFoundException,
        ModuleException, IOException {
        try {
            Utkast draft = draftService.getDraft(redeliveryMessage.getCertId(), redeliveryMessage.getCertType(), false);
            ModuleApi moduleApi = moduleRegistry.getModuleApi(draft.getIntygsTyp(), draft.getIntygTypeVersion());
            Utlatande utlatande = moduleApi.getUtlatandeFromJson(draft.getModel());
            return moduleApi.getIntygFromUtlatande(utlatande);
        } catch (WebCertServiceException e) {
            LOG.warn("Could not find certificate {} of type {} in webcert's database. Will check intygstjanst...",
                redeliveryMessage.getCertId(), redeliveryMessage.getCertType(), e);
            return null;
        }
    }

    private Intyg getCertificateFromIntygstjanst(NotificationRedeliveryMessage redeliveryMessage) throws ModuleNotFoundException,
        ModuleException, WebCertServiceException {
        try {
            IntygContentHolder certContentHolder = certificateService.fetchIntygDataForInternalUse(redeliveryMessage.getCertId(), true);
            Utlatande utlatande = certContentHolder.getUtlatande();
            ModuleApi moduleApi = moduleRegistry.getModuleApi(Objects.requireNonNull(utlatande).getTyp(), utlatande.getTextVersion());
            return moduleApi.getIntygFromUtlatande(utlatande);
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

    private String getLogInfoString(NotificationRedelivery redelivery) {
        return String.format("Failure resending message [notificationId: %s, correlationId: %s]. ", redelivery.getEventId(),
            redelivery.getCorrelationId());
    }

    private void createManualNotification(Handelse event, NotificationRedelivery redelivery)
        throws ModuleNotFoundException, IOException, ModuleException, TemporaryException, InvalidPersonNummerException {

        if (event.getDeliveryStatus() == NotificationDeliveryStatusEnum.FAILURE) {

            CertificateStatusUpdateForCareType statusUpdate;
            String certificateId = event.getIntygsId();
            LocalDateTime eventTime = event.getTimestamp();
            HandelsekodEnum eventType = event.getCode();
            String logicalAddress = event.getEnhetsId();
            String certificateType = event.getCertificateType();
            FragorOchSvar qa = FragorOchSvar.getEmpty();
            ArendeCount sentQuestions = ArendeCount.getEmpty();
            ArendeCount receivedQuestions = ArendeCount.getEmpty();
            Amneskod topicCode = event.getAmne() != null ? AmneskodCreator.create(event.getAmne().name(), event.getAmne().getDescription())
                : null;
            LocalDate lastReplyDate = event.getSistaDatumForSvar();

            if (eventType == HandelsekodEnum.RADERA) {
                Vardgivare careProvider = hsaOrganizationsService.getVardgivareInfo(event.getVardgivarId());
                Vardenhet careUnit = hsaOrganizationsService.getVardenhet(logicalAddress);
                PersonInformationType personInfo = hsaPersonService.getHsaPersonInfo(event.getCertificateIssuer()).get(0);

                Intyg certificate = new Intyg();
                certificate.setIntygsId(NotificationRedeliveryUtil.getIIType(new IntygId(), certificateId, logicalAddress));
                certificate.setTyp(NotificationRedeliveryUtil.getCertificateType(certificateType));
                certificate.setVersion(event.getCertificateVersion());
                certificate.setPatient(NotificationRedeliveryUtil.getPatient(event.getPersonnummer()));
                certificate.setSkapadAv(NotificationRedeliveryUtil.getHosPersonal(careProvider, careUnit, personInfo));
                notificationPatientEnricher.enrichWithPatient(certificate);
                NotificationTypeConverter.complementIntyg(certificate);

                statusUpdate = new CertificateStatusUpdateForCareType();
                statusUpdate.setIntyg(certificate);
                statusUpdate.setHandelse(NotificationRedeliveryUtil.getEventV3(eventType, eventTime, event.getAmne(), lastReplyDate));
                statusUpdate.setSkickadeFragor(NotificationTypeConverter.toArenden(sentQuestions));
                statusUpdate.setMottagnaFragor(NotificationTypeConverter.toArenden(receivedQuestions));
                statusUpdate.setHanteratAv(NotificationRedeliveryUtil.getIIType(new HsaId(), event.getHanteratAv(), HSA_ID_OID));
            } else {
                Intyg certificate;
                String draftJson;
                ModuleApi moduleApi;
                Utlatande utlatande;
                Utkast draft = draftRepo.findById(certificateId).orElse(null);
                if (draft != null) {
                    moduleApi = moduleRegistry.getModuleApi(draft.getIntygsTyp(), draft.getIntygTypeVersion());
                    draftJson = draft.getModel();
                    utlatande = moduleApi.getUtlatandeFromJson(draftJson);
                } else {
                    IntygContentHolder certContentHolder = certificateService.fetchIntygDataForInternalUse(certificateId, true);
                    utlatande = certContentHolder.getUtlatande();
                    moduleApi = moduleRegistry.getModuleApi(Objects.requireNonNull(utlatande).getTyp(), utlatande.getTextVersion());
                    draftJson = certContentHolder.getContents();
                }
                certificate = moduleApi.getIntygFromUtlatande(utlatande);
                notificationPatientEnricher.enrichWithPatient(certificate);
                SchemaVersion schemaVersion = sendNotificationStrategy.decideNotificationForIntyg(utlatande)
                    .orElse(SchemaVersion.VERSION_3);
                String reference = referenceService.getReferensForIntygsId(certificateId);

                if (USES_QA.contains(eventType)) {
                    qa = qaCreator.createFragorOchSvar(certificateId);
                    Pair<ArendeCount, ArendeCount> arenden = qaCreator.createArenden(certificateId, certificateType);
                    sentQuestions = arenden.getLeft();
                    receivedQuestions = arenden.getRight();
                }
                NotificationMessage notificationMessage = new NotificationMessage(certificateId, certificateType, eventTime, eventType,
                    logicalAddress, draftJson, qa, sentQuestions, receivedQuestions, schemaVersion, reference, topicCode, lastReplyDate);
                statusUpdate = NotificationTypeConverter.convert(notificationMessage, certificate);
            }
            redelivery.setCorrelationId(UUID.randomUUID().toString());
            event.setDeliveryStatus(NotificationDeliveryStatusEnum.RESEND);
            notificationRedeliveryService.initiateManualNotification(redelivery, event);
            String statusUpdateXml = marshal(statusUpdate);
            sendJmsMessage(statusUpdateXml, event, redelivery, true);
        }
    }

   private void sendJmsMessage(String statusUpdateXml, Handelse event, NotificationRedelivery redelivery, Boolean isManualRedelivery) {

        LOG.info("Initiating redelivery of status update for care [notificationId: {}, event: {}, logicalAddress: {}"
            + ", correlationId: {}]", event.getId(), event.getCode(), event.getEnhetsId(), redelivery.getCorrelationId());

        try {
            jmsTemplate.convertAndSend(statusUpdateXml.getBytes(), jmsMessage -> {
                jmsMessage.setStringProperty(CORRELATION_ID, redelivery.getCorrelationId());
                jmsMessage.setStringProperty(INTYGS_ID, event.getIntygsId());
                jmsMessage.setStringProperty(LOGISK_ADRESS, event.getEnhetsId());
                jmsMessage.setStringProperty(USER_ID, event.getHanteratAv());
                jmsMessage.setLongProperty(JMS_TIMESTAMP, Instant.now().getEpochSecond());
                jmsMessage.setBooleanProperty(IS_MANUAL_REDELIVERY, isManualRedelivery);
                return jmsMessage;
            });
        } catch (JmsException e) {
            throw new RuntimeException(String.format("Failure resending message [notificationId: %s, event: %s, "
                    + "logicalAddress: %s, correlationId: %s]. Exception occurred setting JMs message headers.", event.getId(),
                event.getCode(), event.getEnhetsId(), redelivery.getCorrelationId()), e);
        }
    }
}
