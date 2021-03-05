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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaPersonService;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.notification_sender.notifications.dto.CertificateMessages;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationRedeliveryMessage;
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.CertificateStatusUpdateForCareCreator;
import se.inera.intyg.webcert.notification_sender.notifications.util.NotificationRedeliveryUtil;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.certificate.GetCertificateService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Arenden;

@Service
public class NotificationRedeliveryStatusUpdateCreatorService {
    
    @Autowired
    private UtkastRepository draftRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;

    @Autowired
    private HsaPersonService hsaPersonService;

    @Autowired
    private CertificateStatusUpdateForCareCreator certificateStatusUpdateForCareCreator;

    @Autowired
    private GetCertificateService getCertificateService;

    @Autowired
    private IntygService intygService;

    @Autowired
    private NotificationMessageFactory notificationMessageFactory;

    /**
     * Creates a {@link CertificateStatusUpdateForCareType} based on the information received in the {@link NotificationRedelivery}.
     */
    public CertificateStatusUpdateForCareType createCertificateStatusUpdate(NotificationRedelivery redelivery, Handelse event)
        throws IOException, ModuleException, ModuleNotFoundException, TemporaryException {
        if (containsMessage(redelivery)) {
            return createStatusUpdateFromExistingMessage(redelivery, event);
        }

        return createStatusUpdateFromEvent(event);
    }

    private boolean containsMessage(NotificationRedelivery redelivery) {
        return redelivery.getMessage() != null;
    }

    private CertificateStatusUpdateForCareType createStatusUpdateFromExistingMessage(NotificationRedelivery redelivery, Handelse event)
        throws IOException {
        final NotificationRedeliveryMessage redeliveryMessage = getRedeliveryMessage(redelivery);

        final var certificate = redeliveryMessage.getCert();
        final var messagesSent = createMessages(redeliveryMessage.getSent());
        final var messagesReceived = createMessages(redeliveryMessage.getReceived());
        final var handledBy = NotificationRedeliveryUtil.getIIType(new HsaId(), event.getHanteratAv(), HSA_ID_OID);
        final var statusUpdateEvent = NotificationRedeliveryUtil.getEventV3(event.getCode(), event.getTimestamp(), event.getAmne(),
            event.getSistaDatumForSvar());

        final var statusUpdate = new CertificateStatusUpdateForCareType();
        statusUpdate.setSkickadeFragor(messagesSent);
        statusUpdate.setMottagnaFragor(messagesReceived);
        statusUpdate.setRef(redeliveryMessage.getReference());
        statusUpdate.setIntyg(certificate);
        statusUpdate.setHanteratAv(handledBy);
        statusUpdate.setHandelse(statusUpdateEvent);

        return statusUpdate;
    }

    private CertificateStatusUpdateForCareType createStatusUpdateFromEvent(Handelse event)
        throws TemporaryException, ModuleNotFoundException, IOException, ModuleException {
        if (isDeletedEvent(event)) {
            final var careProvider = hsaOrganizationsService.getVardgivareInfo(event.getVardgivarId());
            final var careUnit = hsaOrganizationsService.getVardenhet(event.getEnhetsId());
            final var personInfo = hsaPersonService.getHsaPersonInfo(event.getCertificateIssuer()).get(0);
            return certificateStatusUpdateForCareCreator.create(event, careProvider, careUnit, personInfo);
        }

        final var notificationMessage = createNotificationMessage(event);
        return certificateStatusUpdateForCareCreator.create(notificationMessage, event.getCertificateVersion());
    }

    private boolean isDeletedEvent(Handelse event) {
        return event.getCode() == HandelsekodEnum.RADERA;
    }

    private NotificationMessage createNotificationMessage(Handelse event)
        throws ModuleNotFoundException, IOException, ModuleException {
        final var draft = draftRepo.findById(event.getIntygsId());
        if (draft.isPresent()) {
            return notificationMessageFactory.createNotificationMessage(event, draft.get().getModel());
        }

        final var certificateContentHolder = intygService.fetchIntygDataForInternalUse(event.getIntygsId(), true);
        return notificationMessageFactory.createNotificationMessage(event, certificateContentHolder.getContents());
    }

    private NotificationRedeliveryMessage getRedeliveryMessage(NotificationRedelivery redelivery) throws IOException {
        return objectMapper.readValue(redelivery.getMessage(), NotificationRedeliveryMessage.class);
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
}
