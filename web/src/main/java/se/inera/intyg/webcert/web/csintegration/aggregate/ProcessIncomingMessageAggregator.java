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

package se.inera.intyg.webcert.web.csintegration.aggregate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.converter.ArendeConverter;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.message.ProcessIncomingMessageService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessIncomingMessageAggregator {

    private final ArendeService arendeService;
    private final CSIntegrationService csIntegrationService;
    private final ProcessIncomingMessageService processIncomingMessageService;


    public SendMessageToCareResponseType process(SendMessageToCareType sendMessageToCareType) {
        final var certificateId = sendMessageToCareType.getIntygsId().getExtension();
        final var exists = csIntegrationService.certificateExists(certificateId);
        if (Boolean.FALSE.equals(exists)) {
            log.debug("Certificate '{}' does not exist in certificate service", certificateId);
            return processMessageForWebcert(sendMessageToCareType);
        }

        return processIncomingMessageService.process(sendMessageToCareType);
    }

    private SendMessageToCareResponseType processMessageForWebcert(SendMessageToCareType sendMessageToCareType) {
        final var response = new SendMessageToCareResponseType();
        final var result = new ResultType();
        arendeService.processIncomingMessage(ArendeConverter.convert(sendMessageToCareType));
        result.setResultCode(ResultCodeType.OK);
        response.setResult(result);
        return response;
    }
}
