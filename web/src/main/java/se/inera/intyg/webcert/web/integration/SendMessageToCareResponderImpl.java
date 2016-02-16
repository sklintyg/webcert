/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.webcert.web.converter.ArendeConverter;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.exception.WebcertServiceException;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.*;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultType;

public class SendMessageToCareResponderImpl implements SendMessageToCareResponderInterface {
    private static final Logger LOG = LoggerFactory.getLogger(SendMessageToCareResponderImpl.class);

    @Autowired
    private ArendeService arendeService;

    @Override
    public SendMessageToCareResponseType sendMessageToCare(String logicalAddress, SendMessageToCareType request) {
        LOG.debug("Received new message to care");

        SendMessageToCareResponseType response = new SendMessageToCareResponseType();
        ResultType result = new ResultType();

        try {
            arendeService.processIncomingMessage(ArendeConverter.convert(request));
            result.setResultCode(ResultCodeType.OK);
        } catch (WebcertServiceException e) { // TODO Error handling
            result.setResultCode(ResultCodeType.ERROR);
            result.setResultText(e.getMessage());
            LOG.error("Could not process incoming message to care. Cause is: {}", e.getMessage());
        }

        response.setResult(result);
        return response;
    }
}
