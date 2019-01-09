/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.xml.ws.WebServiceException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.common.support.integration.converter.util.ResultTypeUtil;
import se.inera.intyg.webcert.common.sender.exception.PermanentException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

@RunWith(MockitoJUnitRunner.class)
public class SendMessageToRecipientProcessorTest {

    private static final String INTYG_ID = "intyg-id";
    private static final String LOGICAL_ADDRESS = "logicalAddress";
    private static final String MESSAGE_ID = "b7360a70-80a3-4d24-b10e-621c3c0c826a";
    private static final String XML_BODY = "<SendMessageToRecipient xmlns=\"urn:riv:clinicalprocess:healthcond:certificate:SendMessageToRecipientResponder:2\"><meddelande-id>"
            + MESSAGE_ID + "</meddelande-id></SendMessageToRecipient>";
    @InjectMocks
    SendMessageToRecipientProcessor sendMessageProcessor;
    @Mock
    private SendMessageToRecipientResponderInterface sendMessageToRecipientResponder;

    @Test
    public void processTest() throws Exception {
        when(sendMessageToRecipientResponder.sendMessageToRecipient(anyString(), any(SendMessageToRecipientType.class)))
                .thenReturn(buildResponse(ResultTypeUtil.okResult()));
        sendMessageProcessor.process(XML_BODY, INTYG_ID, LOGICAL_ADDRESS);

        ArgumentCaptor<SendMessageToRecipientType> requestCaptor = ArgumentCaptor.forClass(SendMessageToRecipientType.class);
        verify(sendMessageToRecipientResponder).sendMessageToRecipient(eq(LOGICAL_ADDRESS), requestCaptor.capture());

        assertNotNull(requestCaptor.getValue());
        assertEquals(MESSAGE_ID, requestCaptor.getValue().getMeddelandeId());
    }

    @Test
    public void processInfoResponseTest() throws Exception {
        when(sendMessageToRecipientResponder.sendMessageToRecipient(anyString(), any(SendMessageToRecipientType.class)))
                .thenReturn(buildResponse(ResultTypeUtil.infoResult("info")));
        sendMessageProcessor.process(XML_BODY, INTYG_ID, LOGICAL_ADDRESS);

        ArgumentCaptor<SendMessageToRecipientType> requestCaptor = ArgumentCaptor.forClass(SendMessageToRecipientType.class);
        verify(sendMessageToRecipientResponder).sendMessageToRecipient(eq(LOGICAL_ADDRESS), requestCaptor.capture());

        assertNotNull(requestCaptor.getValue());
        assertEquals(MESSAGE_ID, requestCaptor.getValue().getMeddelandeId());
    }

    @Test(expected = PermanentException.class)
    public void processJaxbExceptionTest() throws Exception {
        sendMessageProcessor.process("invalid-xml", INTYG_ID, LOGICAL_ADDRESS);
    }

    @Test(expected = TemporaryException.class)
    public void processWebServiceExceptionTest() throws Exception {
        when(sendMessageToRecipientResponder.sendMessageToRecipient(anyString(), any(SendMessageToRecipientType.class)))
                .thenThrow(new WebServiceException());
        sendMessageProcessor.process(XML_BODY, INTYG_ID, LOGICAL_ADDRESS);
    }

    @Test(expected = PermanentException.class)
    public void processErrorIdRevokedTest() throws Exception {
        when(sendMessageToRecipientResponder.sendMessageToRecipient(anyString(), any(SendMessageToRecipientType.class)))
                .thenReturn(buildResponse(ResultTypeUtil.errorResult(ErrorIdType.REVOKED, "")));
        sendMessageProcessor.process(XML_BODY, INTYG_ID, LOGICAL_ADDRESS);
    }

    @Test(expected = PermanentException.class)
    public void processErrorIdValidationErrorTest() throws Exception {
        when(sendMessageToRecipientResponder.sendMessageToRecipient(anyString(), any(SendMessageToRecipientType.class)))
                .thenReturn(buildResponse(ResultTypeUtil.errorResult(ErrorIdType.VALIDATION_ERROR, "")));
        sendMessageProcessor.process(XML_BODY, INTYG_ID, LOGICAL_ADDRESS);
    }

    @Test(expected = TemporaryException.class)
    public void processErrorIdApplicationErrorTest() throws Exception {
        when(sendMessageToRecipientResponder.sendMessageToRecipient(anyString(), any(SendMessageToRecipientType.class)))
                .thenReturn(buildResponse(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR, "")));
        sendMessageProcessor.process(XML_BODY, INTYG_ID, LOGICAL_ADDRESS);
    }

    @Test(expected = TemporaryException.class)
    public void processErrorIdTechnicalErrorTest() throws Exception {
        when(sendMessageToRecipientResponder.sendMessageToRecipient(anyString(), any(SendMessageToRecipientType.class)))
                .thenReturn(buildResponse(ResultTypeUtil.errorResult(ErrorIdType.TECHNICAL_ERROR, "")));
        sendMessageProcessor.process(XML_BODY, INTYG_ID, LOGICAL_ADDRESS);
    }

    private SendMessageToRecipientResponseType buildResponse(ResultType result) {
        SendMessageToRecipientResponseType response = new SendMessageToRecipientResponseType();
        response.setResult(result);
        return response;
    }
}
