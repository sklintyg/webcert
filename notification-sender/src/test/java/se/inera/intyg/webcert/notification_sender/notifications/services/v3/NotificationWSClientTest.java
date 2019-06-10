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
package se.inera.intyg.webcert.notification_sender.notifications.services.v3;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.Constants;
import se.inera.intyg.webcert.common.sender.exception.PermanentException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NotificationWSClientTest {

    private static final String LOGICAL_ADDRESS = "address1";
    private static final String USER_ID= "hsaId";

    @InjectMocks
    private NotificationWSClient notificationWsClient;

    @Mock
    private CertificateStatusUpdateForCareResponderInterface statusUpdateForCareClient;

    @Test(expected = TemporaryException.class)
    public void testSendStatusUpdateClientThrowsTemporaryException() throws Exception {
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
                .thenThrow(new WebServiceException());
        notificationWsClient.sendStatusUpdate(createRequest(), LOGICAL_ADDRESS, USER_ID);
    }

    @Test(expected = PermanentException.class)
    public void testSendStatusUpdateClientThrowsPermanentExceptionMarshallingError() throws Exception {
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
                .thenThrow(new SOAPFaultException(SOAPFactory.newInstance().createFault("Marshalling Error", new QName(""))));
        notificationWsClient.sendStatusUpdate(createRequest(), LOGICAL_ADDRESS, USER_ID);
    }

    @Test(expected = PermanentException.class)
    public void testSendStatusUpdateClientThrowsPermanentExceptionUnmarshallingError() throws Exception {
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
                .thenThrow(new SOAPFaultException(SOAPFactory.newInstance().createFault("Unmarshalling Error", new QName(""))));
        notificationWsClient.sendStatusUpdate(createRequest(), LOGICAL_ADDRESS, USER_ID);
    }

    @Test
    public void testSendStatusUpdateOk() throws Exception {
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
                .thenReturn(buildResponse(ResultCodeType.OK, null, null));
        CertificateStatusUpdateForCareType request = createRequest();
        notificationWsClient.sendStatusUpdate(request, LOGICAL_ADDRESS, USER_ID);
        verify(statusUpdateForCareClient).certificateStatusUpdateForCare(eq(LOGICAL_ADDRESS),
                any(CertificateStatusUpdateForCareType.class));
        assertEquals(request.getHanteratAv().getExtension(), USER_ID);
        assertEquals(request.getHanteratAv().getRoot(), Constants.HSA_ID_OID);
    }

    @Test
    public void testSendStatusUpdateInfo() throws Exception {
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
                .thenReturn(buildResponse(ResultCodeType.INFO, null, "info text"));
        CertificateStatusUpdateForCareType request = createRequest();
        notificationWsClient.sendStatusUpdate(request, LOGICAL_ADDRESS, null);
        verify(statusUpdateForCareClient).certificateStatusUpdateForCare(eq(LOGICAL_ADDRESS),
                any(CertificateStatusUpdateForCareType.class));
        verify(statusUpdateForCareClient).certificateStatusUpdateForCare(eq(LOGICAL_ADDRESS),
                any(CertificateStatusUpdateForCareType.class));
        assertNull(request.getHanteratAv());
    }

    @Test(expected = TemporaryException.class)
    public void testSendStatusUpdateErrorTechnical() throws Exception {
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
                .thenReturn(buildResponse(ResultCodeType.ERROR, ErrorIdType.TECHNICAL_ERROR, "error text"));
        notificationWsClient.sendStatusUpdate(createRequest(), LOGICAL_ADDRESS, USER_ID);
    }

    @Test(expected = PermanentException.class)
    public void testSendStatusUpdateErrorOther() throws Exception {
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
                .thenReturn(buildResponse(ResultCodeType.ERROR, ErrorIdType.VALIDATION_ERROR, "error text"));
        notificationWsClient.sendStatusUpdate(createRequest(), LOGICAL_ADDRESS, USER_ID);
    }

    @Test(expected = PermanentException.class)
    public void testSendStatusUpdateErrorIdNull() throws Exception {
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
                .thenReturn(buildResponse(ResultCodeType.ERROR, null, "error text"));
        notificationWsClient.sendStatusUpdate(createRequest(), LOGICAL_ADDRESS, USER_ID);
    }

    @Test(expected = PermanentException.class)
    public void xmlMarshallingErrorTest() throws Exception {
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
                .thenThrow(new RuntimeException("Lorem ipsum...Marshalling Error: WTF"));
        notificationWsClient.sendStatusUpdate(createRequest(), LOGICAL_ADDRESS, USER_ID);
    }

    @Test(expected = PermanentException.class)
    public void xmlUnarshallingErrorTest() throws Exception {
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
                .thenThrow(new RuntimeException("Unmarshalling Error: WTF"));
        notificationWsClient.sendStatusUpdate(createRequest(), LOGICAL_ADDRESS, USER_ID);
    }

    private CertificateStatusUpdateForCareType createRequest() {
        CertificateStatusUpdateForCareType res = new CertificateStatusUpdateForCareType();
        res.setIntyg(new Intyg());
        res.getIntyg().setIntygsId(new IntygId());
        res.getIntyg().getIntygsId().setExtension("intygsId");
        return res;
    }

    private CertificateStatusUpdateForCareResponseType buildResponse(ResultCodeType code, ErrorIdType errorId, String resultText) {
        CertificateStatusUpdateForCareResponseType res = new CertificateStatusUpdateForCareResponseType();
        res.setResult(new ResultType());
        res.getResult().setResultCode(code);
        res.getResult().setErrorId(errorId);
        res.getResult().setResultText(resultText);
        return res;
    }
}
