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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.xml.ws.WebServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.webcert.common.client.SendCertificateServiceClient;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientResponseType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

@RunWith(MockitoJUnitRunner.class)
public class CertificateSendProcessorTest {

    private static final String INTYGS_ID1 = "intygs-id-1";
    private static final String PERSON_ID1 = "19121212-1212";
    private static final String SKICKAT_AV = "skickatAv";
    private static final String RECIPIENT1 = "recipient1";
    private static final String LOGICAL_ADDRESS1 = "logicalAddress1";

    @Mock
    private SendCertificateServiceClient sendServiceClient;
    @Spy
    private MdcHelper mdcHelper;

    @InjectMocks
    private CertificateSendProcessor certificateSendProcessor = new CertificateSendProcessor();

    @Test
    public void testSendCertificate() throws Exception {
        // Given
        SendCertificateToRecipientResponseType response = createResponse(ResultCodeType.OK, null);
        when(sendServiceClient.sendCertificate(INTYGS_ID1, PERSON_ID1, SKICKAT_AV, RECIPIENT1, LOGICAL_ADDRESS1)).thenReturn(response);

        // When
        certificateSendProcessor.process(SKICKAT_AV, INTYGS_ID1, PERSON_ID1, RECIPIENT1, LOGICAL_ADDRESS1);

        // Then
        verify(sendServiceClient).sendCertificate(INTYGS_ID1, PERSON_ID1, SKICKAT_AV, RECIPIENT1, LOGICAL_ADDRESS1);
    }

    @Test(expected = TemporaryException.class)
    public void testSendCertificateThrowsTemporaryOnApplicationError() throws Exception {
        // Given
        SendCertificateToRecipientResponseType response = createResponse(ResultCodeType.ERROR, ErrorIdType.APPLICATION_ERROR);
        when(sendServiceClient.sendCertificate(INTYGS_ID1, PERSON_ID1, SKICKAT_AV, RECIPIENT1, LOGICAL_ADDRESS1)).thenReturn(response);

        // When
        certificateSendProcessor.process(SKICKAT_AV, INTYGS_ID1, PERSON_ID1, RECIPIENT1, LOGICAL_ADDRESS1);

        // Then
        verify(sendServiceClient).sendCertificate(INTYGS_ID1, PERSON_ID1, SKICKAT_AV, RECIPIENT1, LOGICAL_ADDRESS1);
    }

    @Test(expected = TemporaryException.class)
    public void testSendCertificateThrowsTemporaryOnTechnicalError() throws Exception {
        // Given
        SendCertificateToRecipientResponseType response = createResponse(ResultCodeType.ERROR, ErrorIdType.TECHNICAL_ERROR);
        when(sendServiceClient.sendCertificate(INTYGS_ID1, PERSON_ID1, SKICKAT_AV, RECIPIENT1, LOGICAL_ADDRESS1)).thenReturn(response);

        // When
        certificateSendProcessor.process(SKICKAT_AV, INTYGS_ID1, PERSON_ID1, RECIPIENT1, LOGICAL_ADDRESS1);

        // Then
        verify(sendServiceClient).sendCertificate(INTYGS_ID1, PERSON_ID1, SKICKAT_AV, RECIPIENT1, LOGICAL_ADDRESS1);
    }

    @Test(expected = TemporaryException.class)
    public void testSendCertificateThrowsTemporaryOnRevokedError() throws Exception {
        // Given
        SendCertificateToRecipientResponseType response = createResponse(ResultCodeType.ERROR, ErrorIdType.REVOKED);
        when(sendServiceClient.sendCertificate(INTYGS_ID1, PERSON_ID1, SKICKAT_AV, RECIPIENT1, LOGICAL_ADDRESS1)).thenReturn(response);

        // When
        certificateSendProcessor.process(SKICKAT_AV, INTYGS_ID1, PERSON_ID1, RECIPIENT1, LOGICAL_ADDRESS1);

        // Then
        verify(sendServiceClient).sendCertificate(INTYGS_ID1, PERSON_ID1, SKICKAT_AV, RECIPIENT1, LOGICAL_ADDRESS1);
    }

    @Test(expected = TemporaryException.class)
    public void testSendCertificateThrowsTemporaryOnValidationError() throws Exception {
        // Given
        SendCertificateToRecipientResponseType response = createResponse(ResultCodeType.ERROR, ErrorIdType.VALIDATION_ERROR);
        when(sendServiceClient.sendCertificate(INTYGS_ID1, PERSON_ID1, SKICKAT_AV, RECIPIENT1, LOGICAL_ADDRESS1)).thenReturn(response);

        // When
        certificateSendProcessor.process(SKICKAT_AV, INTYGS_ID1, PERSON_ID1, RECIPIENT1, LOGICAL_ADDRESS1);

        // Then
        verify(sendServiceClient).sendCertificate(INTYGS_ID1, PERSON_ID1, SKICKAT_AV, RECIPIENT1, LOGICAL_ADDRESS1);
    }

    @Test(expected = TemporaryException.class)
    public void testSendCertificateThrowsTemporaryOnWebServiceException() throws Exception {
        // Given
        when(sendServiceClient.sendCertificate(INTYGS_ID1, PERSON_ID1, SKICKAT_AV, RECIPIENT1, LOGICAL_ADDRESS1))
            .thenThrow(new WebServiceException());

        // When
        certificateSendProcessor.process(SKICKAT_AV, INTYGS_ID1, PERSON_ID1, RECIPIENT1, LOGICAL_ADDRESS1);

        // Then
        verify(sendServiceClient).sendCertificate(INTYGS_ID1, PERSON_ID1, SKICKAT_AV, RECIPIENT1, LOGICAL_ADDRESS1);
    }

    @Test
    public void testSendCertificateOnInfoMessage() throws Exception {
        // Given
        SendCertificateToRecipientResponseType response = createResponse(ResultCodeType.INFO, null);
        when(sendServiceClient.sendCertificate(INTYGS_ID1, PERSON_ID1, SKICKAT_AV, RECIPIENT1, LOGICAL_ADDRESS1)).thenReturn(response);

        // When
        certificateSendProcessor.process(SKICKAT_AV, INTYGS_ID1, PERSON_ID1, RECIPIENT1, LOGICAL_ADDRESS1);

        // Then
        verify(sendServiceClient).sendCertificate(INTYGS_ID1, PERSON_ID1, SKICKAT_AV, RECIPIENT1, LOGICAL_ADDRESS1);
    }

    private SendCertificateToRecipientResponseType createResponse(ResultCodeType resultCodeType, ErrorIdType errorType) {
        ResultType resultType = new ResultType();
        resultType.setResultCode(resultCodeType);
        if (errorType != null) {
            resultType.setErrorId(errorType);
        }
        SendCertificateToRecipientResponseType responseType = new SendCertificateToRecipientResponseType();

        responseType.setResult(resultType);
        return responseType;
    }
}
