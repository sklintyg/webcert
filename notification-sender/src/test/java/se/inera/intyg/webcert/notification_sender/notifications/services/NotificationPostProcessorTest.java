/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType.APPLICATION_ERROR;
import static se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType.REVOKED;
import static se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType.TECHNICAL_ERROR;
import static se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType.VALIDATION_ERROR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import javax.xml.bind.MarshalException;
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
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.notification_sender.notifications.dto.ExceptionInfoTransporter;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationWSResultMessage;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Handelsekod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Vardgivare;


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
    private static final String USER_ID = "testUser";
    private static final String CORRELATION_ID = "testCorrelationId";
    private static final String EVENT = "testSKAPAT";
    private static final long TIMESTAMP = Instant.now().toEpochMilli();

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
        String invalidMessageJson = "{\"exceptionWrapper\":\"not a valid NotificationWSResultMessage\"}";
        when(message.getBody(String.class)).thenReturn(invalidMessageJson);
        postProcessor.process(message);
        verifyNoInteractions(notificationRedeliveryService);
    }

    @Test
    public void receiveMessageWithStatusOk() throws JsonProcessingException {
        String messageJson = buildWSResultMessage(OK, null, "", null);
        when(message.getBody(String.class)).thenReturn(messageJson);
        postProcessor.process(message);

        verify(notificationRedeliveryService).handleNotificationSuccess(any(NotificationWSResultMessage.class), any(Handelse.class));
    }

    @Test
    public void receiveMessageWithStatusInfo() throws JsonProcessingException {
        String messageJson = buildWSResultMessage(INFO, null, "Informative message", null);
        when(message.getBody(String.class)).thenReturn(messageJson);
        postProcessor.process(message);

        verify(notificationRedeliveryService).handleNotificationSuccess(any(NotificationWSResultMessage.class), any(Handelse.class));
    }

    @Test
    public void receiveMessageWithStatusTechnicalError() throws JsonProcessingException {
        String messageJson = buildWSResultMessage(ERROR, TECHNICAL_ERROR, "Technical error message", null);
        when(message.getBody(String.class)).thenReturn(messageJson);
        postProcessor.process(message);

        verify(notificationRedeliveryService).handleNotificationResend(any(NotificationWSResultMessage.class), any(Handelse.class));
    }

    @Test
    public void receiveMessageWithStatusValidationError() throws JsonProcessingException {
        String messageJson = buildWSResultMessage(ERROR, VALIDATION_ERROR, "Validation error message", null);
        when(message.getBody(String.class)).thenReturn(messageJson);
        postProcessor.process(message);

        verify(notificationRedeliveryService).handleNotificationFailure(any(NotificationWSResultMessage.class), any(Handelse.class));
    }

    @Test
    public void receiveMessageWithStatusApplicationError() throws JsonProcessingException {
        String messageJson = buildWSResultMessage(ERROR, APPLICATION_ERROR, "Application error message", null);
        when(message.getBody(String.class)).thenReturn(messageJson);
        postProcessor.process(message);

        verify(notificationRedeliveryService).handleNotificationFailure(any(NotificationWSResultMessage.class), any(Handelse.class));
    }

    @Test
    public void receiveMessageWithStatusRevoked() throws JsonProcessingException {
        String messageJson = buildWSResultMessage(ERROR, REVOKED, "Revoked error message", null);
        when(message.getBody(String.class)).thenReturn(messageJson);
        postProcessor.process(message);

        verify(notificationRedeliveryService).handleNotificationFailure(any(NotificationWSResultMessage.class), any(Handelse.class));
    }

    @Test
    public void receiveMessageWithException() throws JsonProcessingException {
        String messageJson = buildWSResultMessage(null, null, "Revoked error message",
            new ExceptionInfoTransporter(new RuntimeException("testRuntimeException")));
        when(message.getBody(String.class)).thenReturn(messageJson);
        postProcessor.process(message);

        verify(notificationRedeliveryService).handleNotificationResend(any(NotificationWSResultMessage.class), any(Handelse.class));
    }

    @Test
    public void receiveMessageWithSoapFaultExceptionAndMarshallingError() throws JsonProcessingException, SOAPException {
        SOAPFaultException soapFaultException = generateSoapFaultException("Marshalling Error");
        ExceptionInfoTransporter exceptionInfoTransporter = new ExceptionInfoTransporter(soapFaultException);
        String messageJson = buildWSResultMessage(null, null, "", exceptionInfoTransporter);
        when(message.getBody(String.class)).thenReturn(messageJson);
        postProcessor.process(message);

        verify(notificationRedeliveryService).handleNotificationFailure(any(NotificationWSResultMessage.class), any(Handelse.class));
    }

    @Test
    public void receiveMessageWithSoapFaultExceptionAndUnmarshallingError() throws JsonProcessingException, SOAPException {
        SOAPFaultException soapFaultException = generateSoapFaultException("Unmarshalling Error");
        ExceptionInfoTransporter exceptionInfoTransporter = new ExceptionInfoTransporter(soapFaultException);
        String messageJson = buildWSResultMessage(null, null, "", exceptionInfoTransporter);
        when(message.getBody(String.class)).thenReturn(messageJson);
        postProcessor.process(message);

        verify(notificationRedeliveryService).handleNotificationFailure(any(NotificationWSResultMessage.class), any(Handelse.class));
    }


    private String buildWSResultMessage(ResultCodeType code, ErrorIdType errorId, String resultText,
        ExceptionInfoTransporter exceptionInfoTransporter) throws JsonProcessingException {
        CertificateStatusUpdateForCareResponseType statusUpdateResponse = buildStatusUpdateResponse(code, errorId, resultText);
        NotificationWSResultMessage wsMessage = new NotificationWSResultMessage();
        wsMessage.setCertificateId(CERTIFICATE_ID);
        wsMessage.setCorrelationId(CORRELATION_ID);
        wsMessage.setLogicalAddress(LOGICAL_ADDRESS);
        wsMessage.setMessageTimestamp(TIMESTAMP);
        wsMessage.setUserId(USER_ID);
        wsMessage.setDeliveryStatus(null);
        wsMessage.setStatusUpdate(buildStatusUpdateRequest());
        wsMessage.setExceptionInfoTransporter(exceptionInfoTransporter);
        wsMessage.setResultType(statusUpdateResponse.getResult());
        return objectMapper.writeValueAsString(wsMessage);
    }

    private CertificateStatusUpdateForCareType buildStatusUpdateRequest() {
        CertificateStatusUpdateForCareType res = new CertificateStatusUpdateForCareType();
        res.setIntyg(new Intyg());
        res.getIntyg().setIntygsId(new IntygId());
        res.getIntyg().getIntygsId().setExtension(CERTIFICATE_ID);
        res.setHandelse(buildEvent());
        res.getIntyg().setPatient(buildPatient());
        res.getIntyg().setSkapadAv(buildHosPersonal());
        return res;
    }

    private CertificateStatusUpdateForCareResponseType buildStatusUpdateResponse(ResultCodeType code, ErrorIdType errorId, String resultText) {
        CertificateStatusUpdateForCareResponseType res = new CertificateStatusUpdateForCareResponseType();
        res.setResult(new ResultType());
        res.getResult().setResultCode(code);
        res.getResult().setErrorId(errorId);
        res.getResult().setResultText(resultText);
        return res;
    }

    private se.riv.clinicalprocess.healthcond.certificate.v3.Handelse buildEvent() {
        se.riv.clinicalprocess.healthcond.certificate.v3.Handelse event = new se.riv.clinicalprocess.healthcond.certificate.v3.Handelse();
        event.setHandelsekod(new Handelsekod());
        event.getHandelsekod().setCode(HandelsekodEnum.SKAPAT.name());
        return event;
    }

    private Patient buildPatient() {
        Patient patient = new Patient();
        patient.setFornamn("fornamn");
        patient.setEfternamn("efternamn");
        patient.setPersonId(new PersonId());
        patient.getPersonId().setExtension("1912121212");
        return patient;
    }

    private HosPersonal buildHosPersonal() {
        Vardgivare careProvider = new Vardgivare();
        HsaId careProviderHsaId = new HsaId();
        careProviderHsaId.setExtension("testCareProvider");
        careProvider.setVardgivareId(careProviderHsaId);

        Enhet unit = new Enhet();
        HsaId unitHsaId = new HsaId();
        unitHsaId.setExtension("testUnit");
        unit.setEnhetsId(unitHsaId);
        unit.setVardgivare(careProvider);

        HosPersonal hosPersonal = new HosPersonal();
        hosPersonal.setEnhet(unit);
        return hosPersonal;
    }

    private SOAPFaultException generateSoapFaultException(String message) throws SOAPException {
        SOAPFactory soapFactory = SOAPFactory.newInstance();
        SOAPFault soapFault = soapFactory.createFault();
        soapFault.setFaultString(message);
        SOAPFaultException soapFaultException = new SOAPFaultException(soapFault);
        soapFaultException.initCause(new MarshalException(message));
        return soapFaultException;
    }
}
