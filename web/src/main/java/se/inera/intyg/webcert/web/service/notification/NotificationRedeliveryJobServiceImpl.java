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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaPersonService;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.notification_sender.notifications.dto.CertificateMessages;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationRedeliveryMessage;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationTypeConverter;
import se.inera.intyg.webcert.notification_sender.notifications.services.redelivery.NotificationRedeliveryService;
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.CertificateStatusUpdateForCareCreator;
import se.inera.intyg.webcert.notification_sender.notifications.util.NotificationRedeliveryUtil;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.referens.ReferensService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Arenden;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@Service
public class NotificationRedeliveryJobServiceImpl implements NotificationRedeliveryJobService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryJobServiceImpl.class);

    @Autowired
    private HandelseRepository handelseRepo;
    @Autowired
    private IntygModuleRegistry moduleRegistry;
    @Autowired
    private NotificationRedeliveryService notificationRedeliveryService;
    @Autowired
    private IntygService certificateService;
    @Autowired
    private ObjectMapper objectMapper;
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
    private CertificateStatusUpdateForCareCreator certificateStatusUpdateForCareCreator;
    @Autowired
    private NotificationMessageFactory notificationMessageFactory;

    @Override
    public void resendScheduledNotifications() {

        // TODO Add handling of job not finished when scheduled to run next time.

        final long startTime = System.currentTimeMillis();

        final List<NotificationRedelivery> redeliveryList = notificationRedeliveryService.getNotificationsForRedelivery();

        int remainingRedeliveries = redeliveryList.size();

        for (NotificationRedelivery redelivery : redeliveryList) {
            try {
                final var messageAsBytes = getMessageAsBytes(redelivery);
                notificationRedeliveryService.resend(redelivery, messageAsBytes);
                remainingRedeliveries--;

                // TODO Sort out these exception with regard to resend or fail, and which action to perform.
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

        int failedRedeliveries = remainingRedeliveries;

        final long endTime = System.currentTimeMillis();

        LOG.info("Initiated {} notification redeliveries in {} milliseconds. Number of initiation failures: {}.", redeliveryList.size(),
            endTime - startTime, failedRedeliveries);
    }

    private byte[] getMessageAsBytes(NotificationRedelivery notificationRedelivery)
        throws ModuleNotFoundException, TemporaryException, ModuleException, IOException {
        final var statusUpdate = getCertificateStatusUpdate(notificationRedelivery);
        final var statusUpdateXml = certificateStatusUpdateForCareCreator.marshal(statusUpdate);
        return statusUpdateXml.getBytes();
    }

    private CertificateStatusUpdateForCareType getCertificateStatusUpdate(NotificationRedelivery redelivery)
        throws IOException, ModuleException, ModuleNotFoundException, TemporaryException {
        final Handelse event = getEventById(redelivery.getEventId());
        if (redelivery.getMessage() == null) {
            return createStatusUpdate(event);
        } else {
            final NotificationRedeliveryMessage redeliveryMessage = objectMapper.readValue(redelivery.getMessage(),
                NotificationRedeliveryMessage.class);
            final var statusUpdate = new CertificateStatusUpdateForCareType();
            statusUpdate.setSkickadeFragor(createMessages(redeliveryMessage.getSent()));
            statusUpdate.setMottagnaFragor(createMessages(redeliveryMessage.getReceived()));
            statusUpdate.setRef(redeliveryMessage.getReference());
            if (!redeliveryMessage.hasCertificate()) {
                final var certificate = getCertificate(event.getIntygsId(), event.getCertificateType(), event.getCertificateVersion());
                certificate.setPatient(redeliveryMessage.getPatient());
                NotificationTypeConverter.complementIntyg(certificate);
                statusUpdate.setIntyg(certificate);
            } else {
                statusUpdate.setIntyg(redeliveryMessage.getCert());
            }

            // TODO Why update
            statusUpdate.setHanteratAv(NotificationRedeliveryUtil.getIIType(new HsaId(), event.getHanteratAv(), HSA_ID_OID));
            statusUpdate.setHandelse(NotificationRedeliveryUtil.getEventV3(event.getCode(), event.getTimestamp(), event.getAmne(),
                event.getSistaDatumForSvar()));
            return statusUpdate;
        }
    }

    private Handelse getEventById(Long id) {
        return handelseRepo.findById(id).orElseThrow();
    }

    private Arenden createMessages(CertificateMessages certificateMessages) {
        if (certificateMessages == null) {
            return new Arenden();
        }

        final var messages = new Arenden();
        messages.setTotalt(certificateMessages.getTotal());
        messages.setEjBesvarade(certificateMessages.getUnanswered());
        messages.setBesvarade(certificateMessages.getAnswered());
        messages.setHanterade(certificateMessages.getHandled());
        return messages;
    }

    private Intyg getCertificate(String certificateId, String certificateType, String certificateVersion)
        throws ModuleNotFoundException, ModuleException, IOException {
        var certificate = getCertificateFromWebcert(certificateId, certificateType, certificateVersion);
        if (certificate == null) {
            certificate = getCertificateFromIntygstjanst(certificateId, certificateType, certificateVersion);
        }
        return certificate;
    }

    private Intyg getCertificateFromWebcert(String certificateId, String certificateType, String certificateVersion)
        throws ModuleNotFoundException, ModuleException, IOException {
        try {
            final var draft = draftService.getDraft(certificateId, moduleRegistry.getModuleIdFromExternalId(certificateType), false);
            final var moduleApi = moduleRegistry
                .getModuleApi(moduleRegistry.getModuleIdFromExternalId(certificateType), certificateVersion);
            final var utlatande = moduleApi.getUtlatandeFromJson(draft.getModel());
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
            final var certificateContentHolder = certificateService.fetchIntygDataForInternalUse(certificateId, true);
            final var moduleApi = moduleRegistry
                .getModuleApi(moduleRegistry.getModuleIdFromExternalId(certificateType), certificateVersion);
            final var utlatande = certificateContentHolder.getUtlatande();
            return moduleApi.getIntygFromUtlatande(utlatande);
        } catch (WebCertServiceException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                String.format("Could not find certificate id: %s of type %s in intygstjanst's database", certificateId,
                    certificateType), e);
        }
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
        final var draft = draftRepo.findById(event.getIntygsId()).orElse(null);
        if (draft != null) {
            return notificationMessageFactory.createNotificationMessage(event, draft.getModel());
        } else {
            final var certificateContentHolder = certificateService.fetchIntygDataForInternalUse(event.getIntygsId(), true);
            return notificationMessageFactory.createNotificationMessage(event, certificateContentHolder.getContents());
        }
    }

    private String getLogInfoString(NotificationRedelivery redelivery) {
        return String.format("Failure resending message [notificationId: %s, correlationId: %s]. ", redelivery.getEventId(),
            redelivery.getCorrelationId());
    }
}
