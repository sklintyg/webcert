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

package se.inera.intyg.webcert.notification_sender.notifications.services.v3;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import se.inera.intyg.common.support.Constants;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.helper.NotificationTestHelper;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareResponderInterface;
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
public class NotificationWSSenderTest {

    @Mock
    private CertificateStatusUpdateForCareResponderInterface statusUpdateForCareClient;

    @Mock
    @Qualifier("jmsTemplateNotificationPostProcessing")
    private JmsTemplate jmsTemplate;

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private NotificationWSSender notificationWSSender;

    private static final String CERTIFICATE_ID = "testCertificateId";
    private static final String LOGICAL_ADDRESS = "testLogicalAddress";
    private static final String USER_ID = "testUser";
    private static final String CORRELATION_ID = "testCorrelationId";
    private static final long TIMESTAMP = Instant.now().toEpochMilli();


    @Test
    public void testSendStatusUpdateOk() {
        CertificateStatusUpdateForCareType request = buildStatusUpdateRequest();
        CertificateStatusUpdateForCareResponseType response = buildStatusUpdateResponse(ResultCodeType.OK,null, null);
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
            .thenReturn(response);
        sendStatusUpdate(request);
        verify(statusUpdateForCareClient).certificateStatusUpdateForCare(eq(LOGICAL_ADDRESS),any(CertificateStatusUpdateForCareType.class));
        verify(jmsTemplate).send(any(MessageCreator.class));
        assertEquals(request.getHanteratAv().getExtension(), USER_ID);
        assertEquals(request.getHanteratAv().getRoot(), Constants.HSA_ID_OID);
    }

    @Test
    public void testSendStatusUpdateInfo() {
        CertificateStatusUpdateForCareType request = buildStatusUpdateRequest();
        CertificateStatusUpdateForCareResponseType response = buildStatusUpdateResponse(ResultCodeType.INFO, null, null);
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
            .thenReturn(response);
        sendStatusUpdate(request);
        verify(statusUpdateForCareClient).certificateStatusUpdateForCare(eq(LOGICAL_ADDRESS),any(CertificateStatusUpdateForCareType.class));
        verify(jmsTemplate).send(any(MessageCreator.class));
        assertEquals(request.getHanteratAv().getExtension(), USER_ID);
        assertEquals(request.getHanteratAv().getRoot(), Constants.HSA_ID_OID);
    }

    @Test
    public void testSendStatusUpdateTechnicalError() {
        CertificateStatusUpdateForCareType request = buildStatusUpdateRequest();
        CertificateStatusUpdateForCareResponseType response = buildStatusUpdateResponse(ResultCodeType.ERROR,
            ErrorIdType.TECHNICAL_ERROR, "Technical error");
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
            .thenReturn(response);
        sendStatusUpdate(request);
        verify(statusUpdateForCareClient).certificateStatusUpdateForCare(eq(LOGICAL_ADDRESS),any(CertificateStatusUpdateForCareType.class));
        verify(jmsTemplate).send(any(MessageCreator.class));
        assertEquals(request.getHanteratAv().getExtension(), USER_ID);
        assertEquals(request.getHanteratAv().getRoot(), Constants.HSA_ID_OID);
    }

    @Test
    public void testSendStatusUpdateValidationError() {
        CertificateStatusUpdateForCareType request = buildStatusUpdateRequest();
        CertificateStatusUpdateForCareResponseType response =
            buildStatusUpdateResponse(ResultCodeType.ERROR, ErrorIdType.VALIDATION_ERROR, "Validation error");
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
            .thenReturn(response);
        sendStatusUpdate(request);
        verify(statusUpdateForCareClient).certificateStatusUpdateForCare(eq(LOGICAL_ADDRESS),any(CertificateStatusUpdateForCareType.class));
        verify(jmsTemplate).send(any(MessageCreator.class));
        assertEquals(request.getHanteratAv().getExtension(), USER_ID);
        assertEquals(request.getHanteratAv().getRoot(), Constants.HSA_ID_OID);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSendStatusUpdateWhenJsonProcessingException() throws Exception {
        CertificateStatusUpdateForCareType request = buildStatusUpdateRequest();
        CertificateStatusUpdateForCareResponseType response = buildStatusUpdateResponse(ResultCodeType.OK, null, null);
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
            .thenReturn(response);
        when(objectMapper.writeValueAsString(any(NotificationResultMessage.class))).thenThrow(new JsonProcessingException("") { });
        sendStatusUpdate(request);
        verifyNoInteractions(jmsTemplate);
    }

    private void sendStatusUpdate(CertificateStatusUpdateForCareType request) {
        notificationWSSender.sendStatusUpdate(request, CERTIFICATE_ID, LOGICAL_ADDRESS, USER_ID, CORRELATION_ID, false, TIMESTAMP);
     }

    private CertificateStatusUpdateForCareType buildStatusUpdateRequest() {
        CertificateStatusUpdateForCareType res = new CertificateStatusUpdateForCareType();
        res.setIntyg(new Intyg());
        res.getIntyg().setIntygsId(new IntygId());
        res.getIntyg().getIntygsId().setExtension(CERTIFICATE_ID);
        res.setHandelse(buildEventV3());
        res.getIntyg().setPatient(buildPatient());
        res.getIntyg().setSkapadAv(buildHosPersonal());
        return res;
    }

    private se.riv.clinicalprocess.healthcond.certificate.v3.Handelse buildEventV3() {
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

    private CertificateStatusUpdateForCareResponseType buildStatusUpdateResponse(ResultCodeType code, ErrorIdType errorId,
        String resultText) {
        CertificateStatusUpdateForCareResponseType res = new CertificateStatusUpdateForCareResponseType();
        res.setResult(new ResultType());
        res.getResult().setResultCode(code);
        res.getResult().setErrorId(errorId);
        res.getResult().setResultText(resultText);
        return res;
    }
}
