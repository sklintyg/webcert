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

package se.inera.intyg.webcert.notification_sender.certificatesender.services;

import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceException;

import org.apache.camel.Body;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.webcert.common.client.converter.SendMessageToRecipientTypeConverter;
import se.inera.intyg.webcert.common.common.Constants;
import se.inera.intyg.webcert.common.sender.exception.PermanentException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v1.*;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultType;

public class SendMessageToRecipientProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(SendMessageToRecipientProcessor.class);

    @Autowired
    private SendMessageToRecipientResponderInterface sendMessageToRecipientResponder;

    public void process(@Body String xmlBody, @Header(Constants.INTYGS_ID) String intygsId, @Header(Constants.LOGICAL_ADDRESS) String logicalAddress) throws TemporaryException, PermanentException {

        try {
            SendMessageToRecipientType parameters = SendMessageToRecipientTypeConverter.fromXml(xmlBody);
            SendMessageToRecipientResponseType response = sendMessageToRecipientResponder.sendMessageToRecipient(logicalAddress, parameters);

            ResultType result = response.getResult();

            switch (result.getResultCode()) {
                case OK:
                case INFO:
                    return;
                case ERROR:
                    switch (result.getErrorId()) {
                        case REVOKED:
                        case VALIDATION_ERROR:
                            LOG.error("Call to sendMessageToCare for intyg {} caused an error: {}, ErrorId: {}. Rethrowing as PermanentException",
                                    intygsId, result.getResultText(), result.getErrorId());
                            throw new PermanentException(result.getResultText());
                        case APPLICATION_ERROR:
                        case TECHNICAL_ERROR:
                        default:
                            LOG.error("Call to sendMessageToCare for intyg {} caused an error: {}, ErrorId: {}. Rethrowing as TemporaryException",
                                    intygsId, result.getResultText(), result.getErrorId());
                            throw new TemporaryException(result.getResultText());
                    }
                default:
                    throw new TemporaryException(result.getResultText());
            }
        } catch (JAXBException e) {
            LOG.error("Call to sendMessageToCare for intyg {} caused an error: {}. Rethrowing as PermanentException",
                    intygsId, e.getMessage());
            throw new PermanentException(e.getMessage());
        } catch (WebServiceException e) {
            LOG.error("Call to sendMessageToCare for intyg {} caused an error: {}. Will retry",
                    intygsId, e.getMessage());
            throw new TemporaryException(e.getMessage());
        }
    }

}
