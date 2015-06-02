package se.inera.webcert.certificatesender.services;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.xml.ws.WebServiceException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientType;
import se.inera.webcert.certificatesender.exception.PermanentException;
import se.inera.webcert.certificatesender.exception.TemporaryException;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultType;

/**
 * Created by eriklupander on 2015-05-22.
 */
@RunWith(MockitoJUnitRunner.class)
public class CertificateSendProcessorTest {

    private static final String INTYGS_ID1 = "intygs-id-1";
    private static final String PERSON_ID1 = "19121212-1212";
    private static final String RECIPIENT1 = "recipient1";
    private static final String LOGICAL_ADDRESS1 = "logicalAddress1";

    @Mock
    SendCertificateToRecipientResponderInterface sendService;

    @InjectMocks
    CertificateSendProcessor certificateSendProcessor = new CertificateSendProcessor();

    @Test
    public void testStoreCertificate() throws Exception {
        // Given
        SendCertificateToRecipientResponseType response = createResponse(ResultCodeType.OK, null);
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))).thenReturn(response);

        // When
        certificateSendProcessor.process(INTYGS_ID1, PERSON_ID1, RECIPIENT1, LOGICAL_ADDRESS1);
        
        // Then
        verify(sendService).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));
    }

    @Test(expected = TemporaryException.class)
    public void testStoreCertificateThrowsTemporaryOnApplicationError() throws Exception {
        // Given
        SendCertificateToRecipientResponseType response = createResponse(ResultCodeType.ERROR, ErrorIdType.APPLICATION_ERROR);
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))).thenReturn(response);

        // When
        certificateSendProcessor.process(INTYGS_ID1, PERSON_ID1, RECIPIENT1, LOGICAL_ADDRESS1);

        // Then
        verify(sendService).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));
    }

    @Test(expected = TemporaryException.class)
    public void testStoreCertificateThrowsTemporaryOnTechnicalError() throws Exception {
        // Given
        SendCertificateToRecipientResponseType response = createResponse(ResultCodeType.ERROR, ErrorIdType.TECHNICAL_ERROR);
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))).thenReturn(response);

        // When
        certificateSendProcessor.process(INTYGS_ID1, PERSON_ID1, RECIPIENT1, LOGICAL_ADDRESS1);

        // Then
        verify(sendService).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));
    }

    @Test(expected = PermanentException.class)
    public void testStoreCertificateThrowsPermanentOnRevokedError() throws Exception {
        // Given
        SendCertificateToRecipientResponseType response = createResponse(ResultCodeType.ERROR, ErrorIdType.REVOKED);
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))).thenReturn(response);

        // When
        certificateSendProcessor.process(INTYGS_ID1, PERSON_ID1, RECIPIENT1, LOGICAL_ADDRESS1);

        // Then
        verify(sendService).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));
    }

    @Test(expected = PermanentException.class)
    public void testStoreCertificateThrowsPermanentOnValidationError() throws Exception {
        // Given
        SendCertificateToRecipientResponseType response = createResponse(ResultCodeType.ERROR, ErrorIdType.VALIDATION_ERROR);
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))).thenReturn(response);

        // When
        certificateSendProcessor.process(INTYGS_ID1, PERSON_ID1, RECIPIENT1, LOGICAL_ADDRESS1);

        // Then
        verify(sendService).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));
    }

    @Test(expected = TemporaryException.class)
    public void testStoreCertificateThrowsPermanentOnWebServiceException() throws Exception {
        // Given
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))).thenThrow(new WebServiceException());

        // When
        certificateSendProcessor.process(INTYGS_ID1, PERSON_ID1, RECIPIENT1, LOGICAL_ADDRESS1);

        // Then
        verify(sendService).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));
    }

    @Test
    public void testStoreCertificateOnInfoMessage() throws Exception {
        // Given
        SendCertificateToRecipientResponseType response = createResponse(ResultCodeType.INFO, null);
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))).thenReturn(response);

        // When
        certificateSendProcessor.process(INTYGS_ID1, PERSON_ID1, RECIPIENT1, LOGICAL_ADDRESS1);

        // Then
        verify(sendService).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntygsIdIsMissing() throws Exception {
        certificateSendProcessor.process(null, PERSON_ID1, RECIPIENT1, LOGICAL_ADDRESS1);
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPersonIdIsMissing() throws Exception {
        certificateSendProcessor.process(INTYGS_ID1, null, RECIPIENT1, LOGICAL_ADDRESS1);
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRecipientIsMissing() throws Exception {
        certificateSendProcessor.process(INTYGS_ID1, PERSON_ID1, null, LOGICAL_ADDRESS1);
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLogicalAddressIsMissing() throws Exception {
        certificateSendProcessor.process(INTYGS_ID1, PERSON_ID1, RECIPIENT1, null);
        fail();
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
