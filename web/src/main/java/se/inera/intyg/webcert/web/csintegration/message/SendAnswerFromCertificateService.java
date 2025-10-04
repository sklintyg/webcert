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

package se.inera.intyg.webcert.web.csintegration.message;

import static se.inera.intyg.webcert.web.service.facade.question.util.QuestionUtil.getSubject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.web.csintegration.certificate.PublishCertificateStatusUpdateService;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.facade.question.SendQuestionAnswerFacadeService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@Slf4j
@RequiredArgsConstructor
@Service("sendAnswerFromCS")
public class SendAnswerFromCertificateService implements SendQuestionAnswerFacadeService {

    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    private final MonitoringLogService monitoringLogService;
    private final PDLLogService pdlLogService;
    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;
    private final PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;

    @Override
    public Question send(String questionId, String message) {
        if (Boolean.FALSE.equals(csIntegrationService.messageExists(questionId))) {
            log.debug("Message '{}' does not exist in certificate service", questionId);
            return null;
        }

        final var certificate = csIntegrationService.getCertificate(
            csIntegrationRequestFactory.getCertificateFromMessageRequestDTO(),
            questionId
        );

        final var sentMessage = csIntegrationService.sendAnswer(
            csIntegrationRequestFactory.sendAnswerRequest(
                certificate.getMetadata().getPatient().getPersonId().getId(),
                message
            ),
            questionId
        );

        pdlLogService.logCreateMessage(
            certificate.getMetadata().getPatient().getPersonId().getId(),
            certificate.getMetadata().getId()
        );

        publishCertificateStatusUpdateService.publish(certificate, HandelsekodEnum.HANFRFM);

        publishCertificateAnalyticsMessage.publishEvent(
            certificateAnalyticsMessageFactory.sentMessage(certificate, sentMessage)
        );

        monitoringLogService.logArendeCreated(
            certificate.getMetadata().getId(),
            certificate.getMetadata().getType(),
            certificate.getMetadata().getUnit().getUnitId(),
            ArendeAmne.valueOf(getSubject(sentMessage.getType()).toString()),
            false,
            questionId
        );

        return sentMessage;
    }
}
