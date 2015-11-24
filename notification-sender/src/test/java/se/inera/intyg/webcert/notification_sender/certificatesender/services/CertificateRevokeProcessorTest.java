package se.inera.intyg.webcert.notification_sender.certificatesender.services;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.xml.ws.WebServiceException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ErrorIdEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.intyg.webcert.notification_sender.exception.PermanentException;
import se.inera.intyg.webcert.notification_sender.exception.TemporaryException;
import se.inera.intyg.webcert.common.client.RevokeCertificateServiceClient;
import se.inera.intyg.webcert.common.common.Constants;

/**
 * Created by eriklupander on 2015-05-22.
 */
@RunWith(MockitoJUnitRunner.class)
public class CertificateRevokeProcessorTest {

    private static final String BODY = "body";
    private static final String INTYGS_ID1 = "intygs-id-1";
    private static final String LOGICAL_ADDRESS1 = "logicalAddress1";

    @Mock
    RevokeCertificateServiceClient revokeServiceClient;

    @InjectMocks
    CertificateRevokeProcessor certificateRevokeProcessor = new CertificateRevokeProcessor();

    @Test
    public void testRevokeCertificate() throws Exception {
        // Given
        RevokeMedicalCertificateResponseType response = createResponse(ResultCodeEnum.OK, null);
        when(revokeServiceClient.revokeCertificate(BODY, LOGICAL_ADDRESS1))
                .thenReturn(response);

        // When
        certificateRevokeProcessor.process(BODY, INTYGS_ID1, LOGICAL_ADDRESS1);

        // Then
        verify(revokeServiceClient).revokeCertificate(BODY, LOGICAL_ADDRESS1);
    }

    @Test(expected = TemporaryException.class)
    public void testRevokeCertificateWhenWebServiceExceptionIsThrown() throws Exception {
        // Given
        when(revokeServiceClient.revokeCertificate(BODY, LOGICAL_ADDRESS1))
                .thenThrow(new WebServiceException());

        // When
        certificateRevokeProcessor.process(BODY, INTYGS_ID1, LOGICAL_ADDRESS1);

        // Then
        verify(revokeServiceClient).revokeCertificate(BODY, LOGICAL_ADDRESS1);
    }

    @Test(expected = TemporaryException.class)
    public void testRevokeCertificateOnApplicationErrorResponse() throws Exception {
        // Given
        RevokeMedicalCertificateResponseType response = createResponse(ResultCodeEnum.ERROR, ErrorIdEnum.APPLICATION_ERROR);
        when(revokeServiceClient.revokeCertificate(BODY, LOGICAL_ADDRESS1))
                .thenReturn(response);

        // When
        certificateRevokeProcessor.process(BODY, INTYGS_ID1, LOGICAL_ADDRESS1);

        // Then
        verify(revokeServiceClient).revokeCertificate(BODY, LOGICAL_ADDRESS1);
    }

    @Test(expected = TemporaryException.class)
    public void testRevokeCertificateOnTechnicalErrorResponse() throws Exception {
        // Given
        RevokeMedicalCertificateResponseType response = createResponse(ResultCodeEnum.ERROR, ErrorIdEnum.TECHNICAL_ERROR);
        when(revokeServiceClient.revokeCertificate(BODY, LOGICAL_ADDRESS1))
                .thenReturn(response);

        // When
        certificateRevokeProcessor.process(BODY, INTYGS_ID1, LOGICAL_ADDRESS1);

        // Then
        verify(revokeServiceClient).revokeCertificate(BODY, LOGICAL_ADDRESS1);
    }

    @Test(expected = PermanentException.class)
    public void testRevokeCertificateOnValidationErrorResponse() throws Exception {
        // Given
        RevokeMedicalCertificateResponseType response = createResponse(ResultCodeEnum.ERROR, ErrorIdEnum.VALIDATION_ERROR);
        when(revokeServiceClient.revokeCertificate(BODY, LOGICAL_ADDRESS1))
                .thenReturn(response);

        // When
        certificateRevokeProcessor.process(BODY, INTYGS_ID1, LOGICAL_ADDRESS1);

        // Then
        verify(revokeServiceClient).revokeCertificate(BODY, LOGICAL_ADDRESS1);

    }

    @Test(expected = PermanentException.class)
    public void testRevokeCertificateOnTransformationErrorResponse() throws Exception {
        // Given
        RevokeMedicalCertificateResponseType response = createResponse(ResultCodeEnum.ERROR, ErrorIdEnum.TRANSFORMATION_ERROR);
        when(revokeServiceClient.revokeCertificate(BODY, LOGICAL_ADDRESS1))
                .thenReturn(response);

        // When
        certificateRevokeProcessor.process(BODY, INTYGS_ID1, LOGICAL_ADDRESS1);

        // Then
        verify(revokeServiceClient).revokeCertificate(BODY, LOGICAL_ADDRESS1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntygsIdIsMissing() throws Exception {
        try {
            certificateRevokeProcessor.process(BODY, null, LOGICAL_ADDRESS1);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(Constants.INTYGS_ID));
            throw e;
        }
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLogicalAddressIsMissing() throws Exception {
        when(revokeServiceClient.revokeCertificate(BODY, null))
                .thenThrow(new IllegalArgumentException("Logical address..."));
        try {
            certificateRevokeProcessor.process(BODY, INTYGS_ID1, null);
        } catch (IllegalArgumentException e) {
            throw e;
        }
        fail();
    }

    private RevokeMedicalCertificateResponseType createResponse(ResultCodeEnum resultCodeType, ErrorIdEnum errorType) {
        ResultOfCall resultType = new ResultOfCall();
        resultType.setResultCode(resultCodeType);
        if (errorType != null) {
            resultType.setErrorId(errorType);
        }
        RevokeMedicalCertificateResponseType responseType = new RevokeMedicalCertificateResponseType();

        responseType.setResult(resultType);
        return responseType;
    }

}
