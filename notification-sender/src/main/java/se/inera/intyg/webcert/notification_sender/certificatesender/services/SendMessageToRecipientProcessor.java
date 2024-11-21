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
package se.inera.intyg.webcert.notification_sender.certificatesender.services;

import jakarta.xml.ws.WebServiceException;
import org.apache.camel.Body;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.UnmarshallingFailureException;
import se.inera.intyg.webcert.common.Constants;
import se.inera.intyg.webcert.common.client.converter.SendMessageToRecipientTypeConverter;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

public class SendMessageToRecipientProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(SendMessageToRecipientProcessor.class);

    @Autowired
    private SendMessageToRecipientResponderInterface sendMessageToRecipientResponder;
    @Autowired
    private MdcHelper mdcHelper;

    public void process(@Body String xmlBody, @Header(Constants.INTYGS_ID) String intygsId,
        @Header(Constants.LOGICAL_ADDRESS) String logicalAddress) throws TemporaryException {

        try {
            MDC.put(MdcLogConstants.TRACE_ID_KEY, mdcHelper.traceId());
            MDC.put(MdcLogConstants.SPAN_ID_KEY, mdcHelper.spanId());
            MDC.put(MdcLogConstants.EVENT_CERTIFICATE_ID, intygsId);
            MDC.put(MdcLogConstants.EVENT_LOGICAL_ADDRESS, logicalAddress);

            SendMessageToRecipientType parameters = SendMessageToRecipientTypeConverter.fromXml(xmlBody);
            SendMessageToRecipientResponseType response = sendMessageToRecipientResponder.sendMessageToRecipient(logicalAddress,
                parameters);

            ResultType result = response.getResult();

            switch (result.getResultCode()) {
                case OK:
                case INFO:
                    return;
                case ERROR:
                    LOG.error(
                        "Call to sendMessageToRecipient for intyg {} caused an error: {}, ErrorId: {}."
                            + " Rethrowing as PermanentException", intygsId, result.getResultText(), result.getErrorId());
                    throw new TemporaryException(result.getResultText());
            }
        } catch (UnmarshallingFailureException e) {
            LOG.error("Call to sendMessageToRecipient for intyg {} caused an error: {}. Rethrowing as PermanentException",
                intygsId, e.getMessage());
            throw new TemporaryException(e.getMessage());
        } catch (WebServiceException e) {
            LOG.error("Call to sendMessageToRecipient for intyg {} caused an error: {}. Will retry",
                intygsId, e.getMessage());
            throw new TemporaryException(e.getMessage());
        } finally {
            MDC.clear();
        }
    }
}
