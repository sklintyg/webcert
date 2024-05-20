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

package se.inera.intyg.webcert.web.csintegration.message;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.web.csintegration.certificate.PublishCertificateStatusUpdateService;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
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
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final MonitoringLogService monitoringLogService;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    private final SendCertificateQuestionUpdateService sendCertificateQuestionUpdateService;

    public SendMessageToCareResponseType process(SendMessageToCareType sendMessageToCare) {
        csIntegrationService.postMessage(
            csIntegrationRequestFactory.getIncomingMessageRequest(sendMessageToCare)
        );

        final var certificate = csIntegrationService.getCertificate(
            sendMessageToCare.getIntygsId().getExtension(),
            csIntegrationRequestFactory.getCertificateRequest()
        );

        final var questionType = ArendeAmne.valueOf(sendMessageToCare.getAmne().getCode());
        final var isAnswer = sendMessageToCare.getSvarPa() != null;

        monitoringLogService.logArendeReceived(
            certificate.getMetadata().getId(),
            certificate.getMetadata().getType(),
            certificate.getMetadata().getUnit().getUnitId(),
            questionType,
            sendMessageToCare.getKomplettering().stream().map(Komplettering::getFrageId).collect(Collectors.toList()),
            isAnswer
        );

        publishCertificateStatusUpdateService.publish(certificate, getEventType(questionType, isAnswer));
        sendCertificateQuestionUpdateService.send(sendMessageToCare, certificate);

        final var sendMessageToCareResponseType = new SendMessageToCareResponseType();
        final var result = new ResultType();
        result.setResultCode(ResultCodeType.OK);
        sendMessageToCareResponseType.setResult(result);
        return sendMessageToCareResponseType;
    }

    private static HandelsekodEnum getEventType(ArendeAmne questionType, boolean isAnswer) {
        if (questionType.equals(ArendeAmne.PAMINN) || !isAnswer) {
            return HandelsekodEnum.NYFRFM;
        }
        return HandelsekodEnum.NYSVFM;
    }
}
