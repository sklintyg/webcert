/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.notification_sender.notifications.services.v2;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.xml.ws.WebServiceException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.webcert.common.sender.exception.PermanentException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v2.*;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v2.*;

@RunWith(MockitoJUnitRunner.class)
public class NotificationWSClientTest {

    private static final String LOGICAL_ADDRESS = "address1";

    @InjectMocks
    private NotificationWSClient notificationWsClient;

    @Mock
    private CertificateStatusUpdateForCareResponderInterface statusUpdateForCareClient;

    @Test(expected = TemporaryException.class)
    public void testSendStatusUpdateClientThrowsException() throws Exception {
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
                .thenThrow(new WebServiceException());
        notificationWsClient.sendStatusUpdate(createRequest(), LOGICAL_ADDRESS);
    }

    @Test
    public void testSendStatusUpdateOk() throws Exception {
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
                .thenReturn(buildResponse(ResultCodeType.OK, null, null));
        notificationWsClient.sendStatusUpdate(createRequest(), LOGICAL_ADDRESS);
        verify(statusUpdateForCareClient).certificateStatusUpdateForCare(eq(LOGICAL_ADDRESS), any(CertificateStatusUpdateForCareType.class));
    }

    @Test
    public void testSendStatusUpdateInfo() throws Exception {
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
        .thenReturn(buildResponse(ResultCodeType.INFO, null, "info text"));
        notificationWsClient.sendStatusUpdate(createRequest(), LOGICAL_ADDRESS);
        verify(statusUpdateForCareClient).certificateStatusUpdateForCare(eq(LOGICAL_ADDRESS), any(CertificateStatusUpdateForCareType.class));
    }

    @Test(expected = TemporaryException.class)
    public void testSendStatusUpdateErrorTechnical() throws Exception {
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
        .thenReturn(buildResponse(ResultCodeType.ERROR, ErrorIdType.TECHNICAL_ERROR, "error text"));
        notificationWsClient.sendStatusUpdate(createRequest(), LOGICAL_ADDRESS);
    }

    @Test(expected = PermanentException.class)
    public void testSendStatusUpdateErrorOther() throws Exception {
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
        .thenReturn(buildResponse(ResultCodeType.ERROR, ErrorIdType.VALIDATION_ERROR, "error text"));
        notificationWsClient.sendStatusUpdate(createRequest(), LOGICAL_ADDRESS);
    }

    @Test(expected = PermanentException.class)
    public void testSendStatusUpdateErrorIdNull() throws Exception {
        when(statusUpdateForCareClient.certificateStatusUpdateForCare(anyString(), any(CertificateStatusUpdateForCareType.class)))
        .thenReturn(buildResponse(ResultCodeType.ERROR, null, "error text"));
        notificationWsClient.sendStatusUpdate(createRequest(), LOGICAL_ADDRESS);
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
