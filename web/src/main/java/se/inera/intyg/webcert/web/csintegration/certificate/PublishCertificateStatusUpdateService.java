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

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Service
@RequiredArgsConstructor
public class PublishCertificateStatusUpdateService {

    private final IntegreradeEnheterRegistry integreradeEnheterRegistry;
    private final CSIntegrationService csIntegrationService;
    private final NotificationMessageFactory notificationMessageFactory;
    private final NotificationService notificationService;
    private final WebCertUserService webCertUserService;

    public void publish(Certificate certificate, HandelsekodEnum eventType, String xml) {
        publish(certificate, eventType, Optional.empty(), Optional.of(xml));
    }

    public void publish(Certificate certificate, HandelsekodEnum eventType) {
        publish(certificate, eventType, Optional.empty(), Optional.empty());
    }

    public void publish(Certificate certificate, HandelsekodEnum eventType, Optional<IntygUser> intygUser, Optional<String> xml) {
        if (unitIsNotIntegrated(certificate)) {
            return;
        }

        final var certificateXml = xml.orElseGet(
            () -> csIntegrationService.getInternalCertificateXml(certificate.getMetadata().getId())
        );

        final var handledByUserHsaId = intygUser.map(IntygUser::getHsaId).orElseGet(
            () -> webCertUserService.hasAuthenticationContext() ? webCertUserService.getUser().getHsaId() : null);

        final var notificationMessage = notificationMessageFactory.create(
            certificate,
            certificateXml,
            eventType,
            handledByUserHsaId
        );

        notificationService.send(
            notificationMessage,
            certificate.getMetadata().getUnit().getUnitId(),
            certificate.getMetadata().getTypeVersion()
        );
    }

    private boolean unitIsNotIntegrated(Certificate certificate) {
        return integreradeEnheterRegistry.getIntegreradEnhet(certificate.getMetadata().getUnit().getUnitId()) == null;
    }
}
