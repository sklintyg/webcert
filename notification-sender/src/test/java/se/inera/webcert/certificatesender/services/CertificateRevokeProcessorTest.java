package se.inera.webcert.certificatesender.services;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.xml.ws.WebServiceException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ErrorIdEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.webcert.certificatesender.exception.PermanentException;
import se.inera.webcert.certificatesender.exception.TemporaryException;
import se.inera.webcert.certificatesender.services.converter.RevokeRequestConverter;
import se.inera.webcert.common.Constants;

/**
 * Created by eriklupander on 2015-05-22.
 */
@RunWith(MockitoJUnitRunner.class)
public class CertificateRevokeProcessorTest {

    private static final String BODY = "body";
    private static final String INTYGS_ID1 = "intygs-id-1";
    private static final String LOGICAL_ADDRESS1 = "logicalAddress1";

    @Mock
    RevokeMedicalCertificateResponderInterface revokeService;

    @Mock
    RevokeRequestConverter revokeRequestConverter;

    @InjectMocks
    CertificateRevokeProcessor certificateRevokeProcessor = new CertificateRevokeProcessor();

    @Test
    public void testRevokeCertificate() throws Exception {
        // Given
        RevokeMedicalCertificateResponseType response = createResponse(ResultCodeEnum.OK, null);
        when(revokeService.revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class)))
                .thenReturn(response);

        // When
        certificateRevokeProcessor.process(BODY, INTYGS_ID1, LOGICAL_ADDRESS1);

        // Then
        verify(revokeService).revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class));
    }

    @Test(expected = TemporaryException.class)
    public void testRevokeCertificateWhenWebServiceExceptionIsThrown() throws Exception {
        // Given
        when(revokeService.revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class)))
                .thenThrow(new WebServiceException());

        // When
        certificateRevokeProcessor.process(BODY, INTYGS_ID1, LOGICAL_ADDRESS1);

        // Then
        verify(revokeService).revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class));
    }

    @Test(expected = TemporaryException.class)
    public void testRevokeCertificateOnApplicationErrorResponse() throws Exception {
        // Given
        RevokeMedicalCertificateResponseType response = createResponse(ResultCodeEnum.ERROR, ErrorIdEnum.APPLICATION_ERROR);
        when(revokeService.revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class))).
                thenReturn(response);

        // When
        certificateRevokeProcessor.process(BODY, INTYGS_ID1, LOGICAL_ADDRESS1);

        // Then
        verify(revokeService).revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class));
    }

    @Test(expected = TemporaryException.class)
    public void testRevokeCertificateOnTechnicalErrorResponse() throws Exception {
        // Given
        RevokeMedicalCertificateResponseType response = createResponse(ResultCodeEnum.ERROR, ErrorIdEnum.TECHNICAL_ERROR);
        when(revokeService.revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class)))
                .thenReturn(response);

        // When
        certificateRevokeProcessor.process(BODY, INTYGS_ID1, LOGICAL_ADDRESS1);

        // Then
        verify(revokeService).revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class));
    }

    @Test(expected = PermanentException.class)
    public void testRevokeCertificateOnValidationErrorResponse() throws Exception {
        // Given
        RevokeMedicalCertificateResponseType response = createResponse(ResultCodeEnum.ERROR, ErrorIdEnum.VALIDATION_ERROR);
        when(revokeService.revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class)))
                .thenReturn(response);

        // When
        certificateRevokeProcessor.process(BODY, INTYGS_ID1, LOGICAL_ADDRESS1);

        // Then
        verify(revokeService).revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class));

    }

    @Test(expected = PermanentException.class)
    public void testRevokeCertificateOnTransformationErrorResponse() throws Exception {
        // Given
        RevokeMedicalCertificateResponseType response = createResponse(ResultCodeEnum.ERROR, ErrorIdEnum.TRANSFORMATION_ERROR);
        when(revokeService.revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class)))
                .thenReturn(response);

        // When
        certificateRevokeProcessor.process(BODY, INTYGS_ID1, LOGICAL_ADDRESS1);

        // Then
        verify(revokeService).revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class));
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
        try {
            certificateRevokeProcessor.process(BODY, INTYGS_ID1, null);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(Constants.LOGICAL_ADDRESS));
            throw e;
        }
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBodyIsMissing() throws Exception {
        try {
            certificateRevokeProcessor.process(null, INTYGS_ID1, LOGICAL_ADDRESS1);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(Constants.REVOKE_MESSAGE));
            throw e;
        }
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBodyIsEmpty() throws Exception {
        try {
            certificateRevokeProcessor.process("", INTYGS_ID1, LOGICAL_ADDRESS1);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(Constants.REVOKE_MESSAGE));
            throw e;
        }
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBodyIsEmptyButWithWhitespace() throws Exception {
        try {
            certificateRevokeProcessor.process(" ", INTYGS_ID1, LOGICAL_ADDRESS1);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(Constants.REVOKE_MESSAGE));
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
