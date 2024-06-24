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
package se.inera.intyg.webcert.web.integration.interactions.sendmessagetocare;

import lombok.RequiredArgsConstructor;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.csintegration.aggregate.ProcessIncomingMessageAggregator;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

@SchemaValidation
@RequiredArgsConstructor
public class SendMessageToCareResponderImpl implements SendMessageToCareResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(SendMessageToCareResponderImpl.class);

    private final ProcessIncomingMessageAggregator processIncomingMessage;

    @Override
    public SendMessageToCareResponseType sendMessageToCare(String logicalAddress, SendMessageToCareType request) {
        LOG.debug("Received new message to care");

        SendMessageToCareResponseType response = new SendMessageToCareResponseType();
        ResultType result = new ResultType();

        try {
            return processIncomingMessage.process(request);
        } catch (WebCertServiceException ex) {
            switch (ex.getErrorCode()) {
                case MESSAGE_ALREADY_EXISTS:
                    result.setResultCode(ResultCodeType.INFO);
                    result.setResultText(ex.getMessage());
                    LOG.info(
                        "Could not process incoming message to care. Message already exists. Question id {}. Certificate id {}. {} {}",
                        request.getMeddelandeId(),
                        request.getIntygsId().getExtension(),
                        ex.getErrorCode(),
                        ex.getMessage()
                    );
                    break;
                case INVALID_STATE:
                case DATA_NOT_FOUND:
                case EXTERNAL_SYSTEM_PROBLEM:
                    result.setResultCode(ResultCodeType.ERROR);
                    result.setErrorId(ErrorIdType.VALIDATION_ERROR);
                    result.setResultText(ex.getMessage());
                    LOG.error(
                        String.format(
                            "Could not process incoming message to care. Validation error. Question id %s. Certificate id %s. %s %s",
                            request.getMeddelandeId(), request.getIntygsId().getExtension(), ex.getErrorCode(), ex.getMessage()
                        ),
                        ex
                    );
                    break;
                default:
                    result.setResultCode(ResultCodeType.ERROR);
                    result.setErrorId(ErrorIdType.APPLICATION_ERROR);
                    result.setResultText(ex.getMessage());
                    LOG.error(
                        String.format(
                            "Could not process incoming message to care. Application error. Question id %s. Certificate id %s. %s %s",
                            request.getMeddelandeId(), request.getIntygsId().getExtension(), ex.getErrorCode(), ex.getMessage()
                        ),
                        ex
                    );
                    break;
            }
        } catch (Exception ex) {
            result.setResultCode(ResultCodeType.ERROR);
            result.setErrorId(ErrorIdType.APPLICATION_ERROR);
            result.setResultText(ex.getMessage());
            LOG.error(
                String.format("Could not process incoming message to care. Application error. Question id %s. Certificate id %s. %s",
                    request.getMeddelandeId(), request.getIntygsId().getExtension(), ex.getMessage()),
                ex
            );
        }

        response.setResult(result);
        return response;
    }
}
