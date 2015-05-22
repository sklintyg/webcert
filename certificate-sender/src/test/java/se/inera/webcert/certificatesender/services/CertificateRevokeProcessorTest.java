package se.inera.webcert.certificatesender.services;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.camel.Message;
import org.junit.Before;
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
import se.inera.webcert.certificatesender.services.validator.CertificateMessageValidator;
import se.inera.webcert.common.Constants;

import javax.xml.ws.WebServiceException;

/**
 * Created by eriklupander on 2015-05-22.
 */
@RunWith(MockitoJUnitRunner.class)
public class CertificateRevokeProcessorTest {

    @Mock
    Message message;

    @Mock
    RevokeMedicalCertificateResponderInterface revokeService;

    @Mock
    RevokeRequestConverter revokeRequestConverter;

    @Mock
    CertificateMessageValidator certificateRevokeMessageValidator;

    @InjectMocks
    CertificateRevokeProcessor certificateRevokeProcessor = new CertificateRevokeProcessor();



    @Mock
    RevokeMedicalCertificateResponseType response;

    @Mock
    ResultOfCall resultOfCall;

    @Before
    public void setupSendMessage() {
        when(message.getHeader(Constants.INTYGS_ID)).thenReturn("test-message-1");
        when(message.getHeader(Constants.MESSAGE_TYPE)).thenReturn(Constants.SEND_MESSAGE);
    }

    @Test
    public void testRevokeCertificate() throws Exception {
        // Given
        when(resultOfCall.getResultCode()).thenReturn(ResultCodeEnum.OK);
        when(response.getResult()).thenReturn(resultOfCall);
        when(revokeService.revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class)))
                .thenReturn(response);

        // When
        certificateRevokeProcessor.process(message);

        // Then
        verify(revokeService).revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class));
    }

    @Test(expected = TemporaryException.class)
    public void testRevokeCertificateWhenWebServiceExceptionIsThrown() throws Exception {
        // GIVEN
        when(revokeService.revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class)))
                .thenThrow(new WebServiceException());

        // When
        certificateRevokeProcessor.process(message);

        // Then
        verify(revokeService).revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class));

    }

    @Test(expected = TemporaryException.class)
    public void testRevokeCertificateOnApplicationErrorResponse() throws Exception {
        // GIVEN
        when(resultOfCall.getErrorId()).thenReturn(ErrorIdEnum.APPLICATION_ERROR);
        when(resultOfCall.getResultCode()).thenReturn(ResultCodeEnum.ERROR);
        when(response.getResult()).thenReturn(resultOfCall);
        when(revokeService.revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class)))
                .thenReturn(response);

        // When
        certificateRevokeProcessor.process(message);

        // Then
        verify(revokeService).revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class));

    }

    @Test(expected = TemporaryException.class)
    public void testRevokeCertificateOnTechnicalErrorResponse() throws Exception {
        // GIVEN
        when(resultOfCall.getErrorId()).thenReturn(ErrorIdEnum.TECHNICAL_ERROR);
        when(resultOfCall.getResultCode()).thenReturn(ResultCodeEnum.ERROR);
        when(response.getResult()).thenReturn(resultOfCall);
        when(revokeService.revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class)))
                .thenReturn(response);

        // When
        certificateRevokeProcessor.process(message);

        // Then
        verify(revokeService).revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class));

    }

    @Test(expected = PermanentException.class)
    public void testRevokeCertificateOnValidationErrorResponse() throws Exception {
        // GIVEN
        when(resultOfCall.getErrorId()).thenReturn(ErrorIdEnum.VALIDATION_ERROR);
        when(resultOfCall.getResultCode()).thenReturn(ResultCodeEnum.ERROR);
        when(response.getResult()).thenReturn(resultOfCall);
        when(revokeService.revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class)))
                .thenReturn(response);

        // When
        certificateRevokeProcessor.process(message);

        // Then
        verify(revokeService).revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class));

    }

    @Test(expected = PermanentException.class)
    public void testRevokeCertificateOnTransformationErrorResponse() throws Exception {
        // GIVEN
        when(resultOfCall.getErrorId()).thenReturn(ErrorIdEnum.TRANSFORMATION_ERROR);
        when(resultOfCall.getResultCode()).thenReturn(ResultCodeEnum.ERROR);
        when(response.getResult()).thenReturn(resultOfCall);
        when(revokeService.revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class)))
                .thenReturn(response);

        // When
        certificateRevokeProcessor.process(message);

        // Then
        verify(revokeService).revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class));

    }

}
