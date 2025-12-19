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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.common.service.notification.AmneskodCreator;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.web.csintegration.certificate.IntegratedUnitNotificationEvaluator;
import se.inera.intyg.webcert.web.csintegration.certificate.PublishCertificateStatusUpdateService;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType.Komplettering;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

@Service
@RequiredArgsConstructor
public class ProcessIncomingMessageService {

    private final CSIntegrationService csIntegrationService;
    private final IntegreradeEnheterRegistry integreradeEnheterRegistry;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final IntegratedUnitNotificationEvaluator integratedUnitNotificationEvaluator;
    private final MonitoringLogService monitoringLogService;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    private final SendMailNotificationForReceivedMessageService sendMailNotificationForReceivedMessageService;
    private final CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;
    private final PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;

    public SendMessageToCareResponseType process(SendMessageToCareType sendMessageToCare) {
        final var incomingMessageRequest = csIntegrationRequestFactory.getIncomingMessageRequest(
            sendMessageToCare);
        csIntegrationService.postMessage(
            incomingMessageRequest
        );

        final var certificate = csIntegrationService.getInternalCertificate(
            sendMessageToCare.getIntygsId().getExtension()
        );

        final var questionType = ArendeAmne.valueOf(sendMessageToCare.getAmne().getCode());
        final var subjectCode = AmneskodCreator.create(questionType.name(),
            questionType.getDescription());
        final var isAnswer = sendMessageToCare.getSvarPa() != null;

        monitoringLogService.logArendeReceived(
            certificate.getMetadata().getId(),
            certificate.getMetadata().getType(),
            certificate.getMetadata().getUnit().getUnitId(),
            questionType,
            sendMessageToCare.getKomplettering().stream().map(Komplettering::getFrageId).toList(),
            isAnswer,
            sendMessageToCare.getMeddelandeId()
        );

        final var shouldReceiveMailNotifications = shouldReciveMailNotifications(certificate);

        if (!unitIsIntegrated(certificate) || shouldReceiveMailNotifications) {
            sendMailNotificationForReceivedMessageService.send(sendMessageToCare, certificate);
        } else {
            publishCertificateStatusUpdateService.publish(
                certificate,
                getEventType(questionType, isAnswer),
                subjectCode,
                incomingMessageRequest.getLastDateToAnswer()
            );
        }

        publishCertificateAnalyticsMessage.publishEvent(
            certificateAnalyticsMessageFactory.receivedMessage(certificate, incomingMessageRequest)
        );

        final var sendMessageToCareResponseType = new SendMessageToCareResponseType();
        final var result = new ResultType();
        result.setResultCode(ResultCodeType.OK);
        sendMessageToCareResponseType.setResult(result);
        return sendMessageToCareResponseType;
    }

    private boolean shouldReciveMailNotifications(Certificate certificate) {
        return integratedUnitNotificationEvaluator.mailNotification(
            certificate.getMetadata().getCareProvider().getUnitId(),
            certificate.getMetadata().getUnit().getUnitId(),
            certificate.getMetadata().getId(),
            certificate.getMetadata().getSigned()
        );
    }

    private static HandelsekodEnum getEventType(ArendeAmne questionType, boolean isAnswer) {
        if (questionType.equals(ArendeAmne.PAMINN) || !isAnswer) {
            return HandelsekodEnum.NYFRFM;
        }
        return HandelsekodEnum.NYSVFM;
    }

    private boolean unitIsIntegrated(Certificate certificate) {
        return integreradeEnheterRegistry.getIntegreradEnhet(certificate.getMetadata().getUnit().getUnitId()) != null;
    }
}