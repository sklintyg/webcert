package se.inera.webcert.certificatesender.services;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import javax.xml.ws.WebServiceException;

import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.modules.support.api.exception.ExternalServiceCallException;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientType;
import se.inera.webcert.certificatesender.exception.PermanentException;
import se.inera.webcert.certificatesender.exception.TemporaryException;
import se.inera.webcert.certificatesender.services.validator.CertificateMessageValidator;
import se.inera.webcert.common.Constants;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultType;

/**
 * Created by eriklupander on 2015-05-22.
 */
@RunWith(MockitoJUnitRunner.class)
public class CertificateSendProcessorTest {

    @Mock
    Message message;

    @Mock
    SendCertificateToRecipientResponderInterface sendService;

    @Mock
    CertificateMessageValidator certificateSendMessageValidator;

    @InjectMocks
    CertificateSendProcessor certificateSendProcessor = new CertificateSendProcessor();

    Throwable technicalErrorException;
    Throwable applicationErrorException;
    Throwable validationErrorException;
    Throwable transformationErrorException;

    @Mock
    SendCertificateToRecipientResponseType response;

    @Mock
    ResultType resultType;

    @Before
    public void setupSendMessage() {
        when(message.getHeader(Constants.INTYGS_ID)).thenReturn("test-message-1");
        when(message.getHeader(Constants.MESSAGE_TYPE)).thenReturn(Constants.SEND_MESSAGE);
    }

    @Before
    public void setupExceptions() {
        technicalErrorException = new ExternalServiceCallException("", ExternalServiceCallException.ErrorIdEnum.TECHNICAL_ERROR);
        applicationErrorException = new ExternalServiceCallException("", ExternalServiceCallException.ErrorIdEnum.APPLICATION_ERROR);
        validationErrorException = new ExternalServiceCallException("", ExternalServiceCallException.ErrorIdEnum.VALIDATION_ERROR);
        transformationErrorException = new ExternalServiceCallException("", ExternalServiceCallException.ErrorIdEnum.TRANSFORMATION_ERROR);
    }

    @Test
    public void testStoreCertificate() throws Exception {
        // Given
        when(resultType.getResultCode()).thenReturn(ResultCodeType.OK);
        when(response.getResult()).thenReturn(resultType);
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))).thenReturn(response);

        // When
        certificateSendProcessor.process(message);

        // Then
        verify(sendService).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));
    }

    @Test(expected = TemporaryException.class)
    public void testStoreCertificateThrowsTemporaryOnApplicationError() throws Exception {
        // Given
        when(resultType.getResultCode()).thenReturn(ResultCodeType.ERROR);
        when(resultType.getErrorId()).thenReturn(ErrorIdType.APPLICATION_ERROR);
        when(response.getResult()).thenReturn(resultType);
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))).thenReturn(response);

        // When
        certificateSendProcessor.process(message);

        // Then
        verify(sendService).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));
    }

    @Test(expected = TemporaryException.class)
    public void testStoreCertificateThrowsTemporaryOnTechnicalError() throws Exception {
        // Given
        when(resultType.getResultCode()).thenReturn(ResultCodeType.ERROR);
        when(resultType.getErrorId()).thenReturn(ErrorIdType.TECHNICAL_ERROR);
        when(response.getResult()).thenReturn(resultType);
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))).thenReturn(response);

        // When
        certificateSendProcessor.process(message);

        // Then
        verify(sendService).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));
    }

    @Test(expected = PermanentException.class)
    public void testStoreCertificateThrowsPermanentOnRevokedError() throws Exception {
        // Given
        when(resultType.getResultCode()).thenReturn(ResultCodeType.ERROR);
        when(resultType.getErrorId()).thenReturn(ErrorIdType.REVOKED);
        when(response.getResult()).thenReturn(resultType);
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))).thenReturn(response);

        // When
        certificateSendProcessor.process(message);

        // Then
        verify(sendService).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));
    }

    @Test(expected = PermanentException.class)
    public void testStoreCertificateThrowsPermanentOnValidationError() throws Exception {
        // Given
        when(resultType.getResultCode()).thenReturn(ResultCodeType.ERROR);
        when(resultType.getErrorId()).thenReturn(ErrorIdType.VALIDATION_ERROR);
        when(response.getResult()).thenReturn(resultType);
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))).thenReturn(response);

        // When
        certificateSendProcessor.process(message);

        // Then
        verify(sendService).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));
    }

    @Test(expected = TemporaryException.class)
    public void testStoreCertificateThrowsPermanentOnWebServiceException() throws Exception {
        // Given
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))).thenThrow(new WebServiceException());

        // When
        certificateSendProcessor.process(message);

        // Then
        verify(sendService).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));
    }

    @Test
    public void testStoreCertificateOnInfoMessage() throws Exception {
        // Given
        when(resultType.getResultCode()).thenReturn(ResultCodeType.INFO);
        when(response.getResult()).thenReturn(resultType);
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))).thenReturn(response);

        // When
        certificateSendProcessor.process(message);

        // Then
        verify(sendService).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));
    }
}
