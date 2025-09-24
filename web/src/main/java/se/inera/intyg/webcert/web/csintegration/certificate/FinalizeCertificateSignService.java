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

import static se.inera.intyg.common.support.facade.model.CertificateRelationType.COMPLEMENTED;
import static se.inera.intyg.common.support.facade.model.CertificateRelationType.EXTENDED;
import static se.inera.intyg.common.support.facade.model.CertificateRelationType.REPLACED;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.link.ResourceLinkTypeEnum;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.integration.analytics.service.CertificateEventMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateEventMessage;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Service
@RequiredArgsConstructor
public class FinalizeCertificateSignService {

    private final PDLLogService pdlLogService;
    private final WebCertUserService webCertUserService;
    private final MonitoringLogService monitoringLogService;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    private final SendCertificateFromCertificateService sendCertificateFromCertificateService;
    private final CSIntegrationService csIntegrationService;
    private final PublishCertificateEventMessage publishCertificateEventMessage;
    private final CertificateEventMessageFactory certificateEventMessageFactory;

    public void finalizeSign(Certificate certificate) {
        final var user = webCertUserService.getUser();

        monitoringLogService.logIntygSigned(
            certificate.getMetadata().getId(),
            certificate.getMetadata().getType(),
            user.getHsaId(),
            user.getAuthenticationScheme(),
            null
        );

        pdlLogService.logSign(
            certificate
        );

        publishCertificateStatusUpdateService.publish(certificate, HandelsekodEnum.SIGNAT);

        publishCertificateEventMessage.publishEvent(
            certificateEventMessageFactory.certificateSigned(certificate)
        );

        if (shouldPublishHandledEventForParent(certificate)) {
            final var parentCertificateId = certificate.getMetadata().getRelations().getParent().getCertificateId();
            final var parentCertificate = csIntegrationService.getInternalCertificate(parentCertificateId);
            publishCertificateStatusUpdateService.publish(parentCertificate, HandelsekodEnum.HANFRFM);
        }

        if (certificate.getLinks().stream()
            .anyMatch(resourceLink -> resourceLink.getType().equals(ResourceLinkTypeEnum.SEND_AFTER_SIGN_CERTIFICATE))) {
            sendCertificateFromCertificateService.sendCertificate(certificate.getMetadata().getId());
        }
    }

    private boolean shouldPublishHandledEventForParent(Certificate certificate) {
        if (!hasParentRelationOfType(certificate, List.of(COMPLEMENTED, REPLACED, EXTENDED))) {
            return false;
        }
        final var parentCertificateId = certificate.getMetadata().getRelations().getParent().getCertificateId();
        final var questions = csIntegrationService.getQuestions(parentCertificateId);
        return questions.stream().anyMatch(question -> QuestionType.COMPLEMENT.equals(question.getType()));
    }

    private boolean hasParentRelationOfType(Certificate certificate, List<CertificateRelationType> relationTypes) {
        if (certificate.getMetadata().getRelations() == null || certificate.getMetadata().getRelations().getParent() == null) {
            return false;
        }
        return relationTypes.contains(
            certificate.getMetadata().getRelations().getParent().getType()
        );
    }
}
