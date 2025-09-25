/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.integration.analytics.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.common.service.user.LoggedInWebcertUserService;
import se.inera.intyg.webcert.integration.analytics.model.AnalyticsCertificate;
import se.inera.intyg.webcert.integration.analytics.model.AnalyticsEvent;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

@Component
@RequiredArgsConstructor
public class CertificateAnalyticsMessageFactory {

    private final LoggedInWebcertUserService loggedInWebcertUserService;

    public CertificateAnalyticsMessage draftCreated(Certificate certificate) {
        return create(certificate, CertificateAnalyticsMessageType.DRAFT_CREATED);
    }

    public CertificateAnalyticsMessage draftCreated(Utkast utkast) {
        return create(utkast, CertificateAnalyticsMessageType.DRAFT_CREATED);
    }

    public CertificateAnalyticsMessage certificateSigned(Certificate certificate) {
        return create(certificate, CertificateAnalyticsMessageType.CERTIFICATE_SIGNED);
    }

    public CertificateAnalyticsMessage certificateSigned(Utkast utkast) {
        return create(utkast, CertificateAnalyticsMessageType.CERTIFICATE_SIGNED);
    }

    public CertificateAnalyticsMessage certificateSent(Certificate certificate) {
        return create(certificate, CertificateAnalyticsMessageType.CERTIFICATE_SENT);
    }

    public CertificateAnalyticsMessage certificateSent(Utkast utkast) {
        return create(utkast, CertificateAnalyticsMessageType.CERTIFICATE_SENT);
    }

    private CertificateAnalyticsMessage create(Certificate certificate, CertificateAnalyticsMessageType type) {
        return CertificateAnalyticsMessage.builder()
            .event(
                createAnalyticsEvent(type)
            )
            .certificate(
                AnalyticsCertificate.builder()
                    .id(certificate.getMetadata().getId())
                    .type(certificate.getMetadata().getType())
                    .typeVersion(certificate.getMetadata().getTypeVersion())
                    .patientId(certificate.getMetadata().getPatient().getPersonId().getId())
                    .unitId(certificate.getMetadata().getUnit().getUnitId())
                    .careProviderId(certificate.getMetadata().getCareProvider().getUnitId())
                    .build()
            )
            .build();
    }

    private CertificateAnalyticsMessage create(Utkast utkast, CertificateAnalyticsMessageType type) {
        return CertificateAnalyticsMessage.builder()
            .event(
                createAnalyticsEvent(type)
            )
            .certificate(
                AnalyticsCertificate.builder()
                    .id(utkast.getIntygsId())
                    .type(utkast.getIntygsTyp())
                    .typeVersion(utkast.getIntygTypeVersion())
                    .patientId(utkast.getPatientPersonnummer().getPersonnummerWithDash())
                    .unitId(utkast.getEnhetsId())
                    .careProviderId(utkast.getVardgivarId())
                    .build()
            )
            .build();
    }

    private AnalyticsEvent createAnalyticsEvent(CertificateAnalyticsMessageType type) {
        final var loggedInWebcertUser = loggedInWebcertUserService.getLoggedInWebcertUser();
        return AnalyticsEvent.builder()
            .timestamp(LocalDateTime.now())
            .messageType(type)
            .staffId(loggedInWebcertUser.getStaffId())
            .role(loggedInWebcertUser.getRole())
            .unitId(loggedInWebcertUser.getUnitId())
            .careProviderId(loggedInWebcertUser.getCareProviderId())
            .origin(loggedInWebcertUser.getOrigin())
            .build();
    }
}
