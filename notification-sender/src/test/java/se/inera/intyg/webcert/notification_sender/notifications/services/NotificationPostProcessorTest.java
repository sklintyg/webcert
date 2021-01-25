/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.notification_sender.notifications.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType.APPLICATION_ERROR;
import static se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType.REVOKED;
import static se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType.TECHNICAL_ERROR;
import static se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType.VALIDATION_ERROR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.xml.bind.MarshalException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;
import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultType;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;


@RunWith(MockitoJUnitRunner.class)
public class NotificationPostProcessorTest {

    @Mock
    private NotificationRedeliveryService notificationRedeliveryService;

    @Mock
    private Message message;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationPostProcessor postProcessor;

    private static final String CERTIFICATE_ID = "testCertificateId";
    private static final String LOGICAL_ADDRESS = "testLogicalAddress";
    private static final String CORRELATION_ID = "testCorrelationId";
    private static final String EVENT = "testSKAPAT";

    private static final ResultCodeType OK = ResultCodeType.OK;
    private static final ResultCodeType INFO = ResultCodeType.INFO;
    private static final ResultCodeType ERROR = ResultCodeType.ERROR;


    @Before
    public void setup() {
        when(message.getHeader(NotificationRouteHeaders.INTYGS_ID)).thenReturn(CERTIFICATE_ID);
        when(message.getHeader(NotificationRouteHeaders.CORRELATION_ID)).thenReturn(CORRELATION_ID);
        when(message.getHeader(NotificationRouteHeaders.LOGISK_ADRESS)).thenReturn(LOGICAL_ADDRESS);
        when(message.getHeader(NotificationRouteHeaders.HANDELSE)).thenReturn(EVENT);
    }

    @Test
    public void receiveMessageWithInvalidType() {
        String invalidMessageJson = "{\"exceptionWrapper\":\"not a valid NotificationResultMessage\"}";
        when(message.getBody(String.class)).thenReturn(invalidMessageJson);
        postProcessor.process(message);
        verifyNoInteractions(notificationRedeliveryService);
    }

    @Test
    public void receiveMessageWithStatusOk() throws JsonProcessingException {
        NotificationResultMessage resultMessage = buildResultMessage(OK, null, "");
        String messageJson = objectMapper.writeValueAsString(resultMessage);
        when(message.getBody(String.class)).thenReturn(messageJson);
        postProcessor.process(message);
        verify(notificationRedeliveryService).handleNotificationSuccess(isA(NotificationResultMessage.class));
    }

    @Test
    public void receiveMessageWithStatusInfo() throws JsonProcessingException {
        NotificationResultMessage resultMessage = buildResultMessage(INFO, null, "Informative message");
        String messageJson = objectMapper.writeValueAsString(resultMessage);
        when(message.getBody(String.class)).thenReturn(messageJson);
        postProcessor.process(message);

        verify(notificationRedeliveryService).handleNotificationSuccess(any(NotificationResultMessage.class));
    }

    @Test
    public void receiveMessageWithStatusTechnicalError() throws JsonProcessingException {
        NotificationResultMessage resultMessage = buildResultMessage(ERROR, TECHNICAL_ERROR, "Technical error message");
        String messageJson = objectMapper.writeValueAsString(resultMessage);
        when(message.getBody(String.class)).thenReturn(messageJson);
        postProcessor.process(message);

        verify(notificationRedeliveryService).handleNotificationResend(any(NotificationResultMessage.class));
    }

    @Test
    public void receiveMessageWithStatusValidationError() throws JsonProcessingException {
        NotificationResultMessage resultMessage = buildResultMessage(ERROR, VALIDATION_ERROR, "Validation error message");
        String messageJson = objectMapper.writeValueAsString(resultMessage);
        when(message.getBody(String.class)).thenReturn(messageJson);
        postProcessor.process(message);

        verify(notificationRedeliveryService).handleNotificationFailure(any(NotificationResultMessage.class));
    }

    @Test
    public void receiveMessageWithStatusApplicationError() throws JsonProcessingException {
        NotificationResultMessage resultMessage = buildResultMessage(ERROR, APPLICATION_ERROR, "Application error message");
        String messageJson = objectMapper.writeValueAsString(resultMessage);
        when(message.getBody(String.class)).thenReturn(messageJson);
        postProcessor.process(message);

        verify(notificationRedeliveryService).handleNotificationFailure(any(NotificationResultMessage.class));
    }

    @Test
    public void receiveMessageWithStatusRevoked() throws JsonProcessingException {
        NotificationResultMessage resultMessage = buildResultMessage(ERROR, REVOKED, "Revoked error message");
        String messageJson = objectMapper.writeValueAsString(resultMessage);
        when(message.getBody(String.class)).thenReturn(messageJson);
        postProcessor.process(message);

        verify(notificationRedeliveryService).handleNotificationFailure(any(NotificationResultMessage.class));
    }

    @Test
    public void receiveMessageWithRuntimeException() throws JsonProcessingException {
        Exception exception = new RuntimeException("testRuntimeException");
        NotificationResultMessage resultMessage = buildResultMessageWithException(NotificationResultTypeEnum.ERROR,
            exception.getClass().getName(), exception.getMessage());
        String messageJson = objectMapper.writeValueAsString(resultMessage);
        when(message.getBody(String.class)).thenReturn(messageJson);
        postProcessor.process(message);

        verify(notificationRedeliveryService).handleNotificationResend(any(NotificationResultMessage.class));
    }

    @Test
    public void receiveMessageWithSoapFaultExceptionAndMarshallingError() throws JsonProcessingException, SOAPException {
        SOAPFaultException soapFaultException = generateSoapFaultException("Marshalling Error");
        NotificationResultMessage resultMessage = buildResultMessageWithException(NotificationResultTypeEnum.ERROR,
            soapFaultException.getClass().getName(), soapFaultException.getMessage());
        String messageJson = objectMapper.writeValueAsString(resultMessage);
        when(message.getBody(String.class)).thenReturn(messageJson);
        postProcessor.process(message);

        verify(notificationRedeliveryService).handleNotificationFailure(any(NotificationResultMessage.class));
    }

    @Test
    public void receiveMessageWithSoapFaultExceptionAndUnmarshallingError() throws JsonProcessingException, SOAPException {
        SOAPFaultException soapFaultException = generateSoapFaultException("Unmarshalling Error");
        NotificationResultMessage resultMessage = buildResultMessageWithException(NotificationResultTypeEnum.ERROR,
            soapFaultException.getClass().getName(), soapFaultException.getMessage());
        String messageJson = objectMapper.writeValueAsString(resultMessage);
        when(message.getBody(String.class)).thenReturn(messageJson);
        postProcessor.process(message);

        verify(notificationRedeliveryService).handleNotificationFailure(any(NotificationResultMessage.class));
    }

    @Test
    public void receiveMessageWithExceptionFromTransFormer() throws JsonProcessingException {
        Exception exception = new NullPointerException("testNullPointerException");
        NotificationResultMessage resultMessage = buildResultMessageWithException(NotificationResultTypeEnum.FAILURE,
            exception.getClass().getName(), exception.getMessage());
        String messageJson = objectMapper.writeValueAsString(resultMessage);
        when(message.getBody(String.class)).thenReturn(messageJson);
        postProcessor.process(message);

        verify(notificationRedeliveryService).handleNotificationFailure(any(NotificationResultMessage.class));
    }

    private NotificationResultMessage buildResultMessage(ResultCodeType code, ErrorIdType errorId, String resultText) {
        CertificateStatusUpdateForCareResponseType statusUpdateResponse = buildStatusUpdateResponse(code, errorId, resultText);
        NotificationResultMessage resultMessage = new NotificationResultMessage();
        resultMessage.setCorrelationId(CORRELATION_ID);
        resultMessage.setEvent(new Handelse());
        resultMessage.setResultType(new NotificationResultType(statusUpdateResponse.getResult()));
        return resultMessage;
    }

    private NotificationResultMessage buildResultMessageWithException(NotificationResultTypeEnum resultEnum, String exception,
        String resultText) {
        NotificationResultMessage resultMessage = new NotificationResultMessage();
        resultMessage.setCorrelationId(CORRELATION_ID);
        resultMessage.setEvent(new Handelse());
        resultMessage.setResultType(new NotificationResultType(resultEnum, exception, resultText));
        return resultMessage;
    }

    private CertificateStatusUpdateForCareResponseType buildStatusUpdateResponse(ResultCodeType code, ErrorIdType errorId, String resultText) {
        CertificateStatusUpdateForCareResponseType res = new CertificateStatusUpdateForCareResponseType();
        res.setResult(new ResultType());
        res.getResult().setResultCode(code);
        res.getResult().setErrorId(errorId);
        res.getResult().setResultText(resultText);
        return res;
    }

    private SOAPFaultException generateSoapFaultException(String message) throws SOAPException {
        SOAPFactory soapFactory = SOAPFactory.newInstance();
        SOAPFault soapFault = soapFactory.createFault(message, new QName(""));
        SOAPFaultException soapFaultException = new SOAPFaultException(soapFault);
        soapFaultException.initCause(new MarshalException(message));
        return soapFaultException;
    }
}
