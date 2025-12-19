/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.csintegration.certificate;

import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.common.service.notification.AmneskodCreator;
import se.inera.intyg.webcert.notification_sender.notifications.services.redelivery.NotificationRedeliveryService;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;

@Service
@RequiredArgsConstructor
public class PublishCertificateStatusUpdateService {

    private final IntegreradeEnheterRegistry integreradeEnheterRegistry;
    private final CSIntegrationService csIntegrationService;
    private final NotificationMessageFactory notificationMessageFactory;
    private final NotificationRedeliveryService notificationRedeliveryService;
    private final NotificationService notificationService;
    private final WebCertUserService webCertUserService;

    public void publish(Certificate certificate, HandelsekodEnum eventType, String xml) {
        publish(certificate, eventType, Optional.empty(), Optional.of(xml), null, null);
    }

    public void publish(Certificate certificate, HandelsekodEnum eventType) {
        publish(certificate, eventType, Optional.empty(), Optional.empty(), null, null);
    }

    public void publish(Certificate certificate, HandelsekodEnum eventType, Amneskod subjectCode, LocalDate lastDateToAnswer) {
        publish(certificate, eventType, Optional.empty(), Optional.empty(), subjectCode, lastDateToAnswer);
    }

    public void publish(Certificate certificate, HandelsekodEnum eventType, Optional<IntygUser> intygUser, Optional<String> xml,
        Amneskod subjectCode, LocalDate lastDateToAnswer) {
        if (unitIsNotIntegrated(certificate)) {
            return;
        }

        final var certificateXml = xml.orElseGet(
            () -> csIntegrationService.getInternalCertificateXml(certificate.getMetadata().getId())
        );

        final var handledByUserHsaId = intygUser.map(IntygUser::getHsaId).orElseGet(
            () -> webCertUserService.hasAuthenticationContext() ? webCertUserService.getUser()
                .getHsaId() : null);

        final var notificationMessage = notificationMessageFactory.create(
            certificate,
            certificateXml,
            eventType,
            handledByUserHsaId,
            subjectCode,
            lastDateToAnswer
        );

        notificationService.send(
            notificationMessage,
            certificate.getMetadata().getUnit().getUnitId(),
            certificate.getMetadata().getTypeVersion()
        );
    }

    public void resend(Certificate certificate, Handelse event, NotificationRedelivery notificationRedelivery) {
        resendEvent(certificate, event, notificationRedelivery);
    }

    private void resendEvent(Certificate certificate, Handelse event,
        NotificationRedelivery notificationRedelivery) {
        if (unitIsNotIntegrated(certificate)) {
            return;
        }

        final var certificateXml = csIntegrationService.getInternalCertificateXml(
            certificate.getMetadata().getId());

        final var notificationMessage = notificationMessageFactory.create(
            certificate,
            certificateXml,
            event.getCode(),
            event.getHanteratAv(),
            getSubjectCode(event),
            event.getSistaDatumForSvar()
        );

        notificationRedeliveryService.resend(
            notificationRedelivery,
            event,
            notificationMessage.getStatusUpdateXml()
        );
    }

    private boolean unitIsNotIntegrated(Certificate certificate) {
        return integreradeEnheterRegistry.getIntegreradEnhet(
            certificate.getMetadata().getUnit().getUnitId()) == null;
    }

    private static Amneskod getSubjectCode(Handelse event) {
        return event.getAmne() != null ? AmneskodCreator.create(event.getAmne().name(), event.getAmne().getDescription()) : null;
    }
}