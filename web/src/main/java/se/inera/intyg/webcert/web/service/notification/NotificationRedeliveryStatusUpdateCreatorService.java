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
package se.inera.intyg.webcert.web.service.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBException;
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
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.CertificateStatusUpdateForCareCreator;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;

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
    private IntygService intygService;

    @Autowired
    private NotificationMessageFactory notificationMessageFactory;

    /**
     * Returns an XML String of a {@link CertificateStatusUpdateForCareType} created based on the information received in e
     * {@link NotificationRedelivery} or, in case of manually initiated redeliveries, a {@link Handelse}.
     */
    public String getCertificateStatusUpdateXml(NotificationRedelivery redelivery, Handelse event)
        throws IOException, ModuleException, ModuleNotFoundException, TemporaryException, JAXBException {
        if (containsMessage(redelivery)) {
            return getStatusUpdateXmlFromNotificationRedelivery(redelivery);
        }

        final var statusUpdate = createStatusUpdateFromEvent(event);
        return certificateStatusUpdateForCareCreator.marshal(statusUpdate);
    }

    private boolean containsMessage(NotificationRedelivery redelivery) {
        return redelivery.getMessage() != null;
    }

    private String getStatusUpdateXmlFromNotificationRedelivery(NotificationRedelivery redelivery) throws IOException {
        return objectMapper.readValue(redelivery.getMessage(), String.class);
    }

    private CertificateStatusUpdateForCareType createStatusUpdateFromEvent(Handelse event)
        throws ModuleNotFoundException, IOException, ModuleException {

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
}
