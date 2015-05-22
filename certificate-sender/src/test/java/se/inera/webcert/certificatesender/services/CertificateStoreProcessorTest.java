package se.inera.webcert.certificatesender.services;

import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.registry.ModuleNotFoundException;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.exception.ExternalServiceCallException;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.certificatesender.exception.PermanentException;
import se.inera.webcert.certificatesender.exception.TemporaryException;
import se.inera.webcert.common.Constants;

import javax.xml.ws.WebServiceException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2015-05-22.
 */
@RunWith(MockitoJUnitRunner.class)
public class CertificateStoreProcessorTest {

    @Mock
    IntygModuleRegistry moduleRegistry;

    @Mock
    ModuleApi moduleApi;

    @Mock
    Message message;

    @InjectMocks
    CertificateStoreProcessor certificateStoreProcessor = new CertificateStoreProcessor();

    Throwable technicalErrorException;
    Throwable applicationErrorException;
    Throwable validationErrorException;
    Throwable transformationErrorException;

    @Before
    public void setup() throws ModuleNotFoundException {
        when(moduleRegistry.getModuleApi(anyString())).thenReturn(moduleApi);
    }

    @Before
    public void setupFkMessage() {
        when(message.getHeader(Constants.INTYGS_ID)).thenReturn("test-message-1");
        when(message.getHeader(Constants.MESSAGE_TYPE)).thenReturn(Constants.STORE_MESSAGE);
        when(message.getBody()).thenReturn("body");
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

        // When
        certificateStoreProcessor.process(message);

        // Then
        verify(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());
    }

    @Test(expected = TemporaryException.class)
    public void testStoreCertificateThrowsTemporaryOnTechnicalError() throws Exception {
        // Given
        doThrow(technicalErrorException).when(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());

        // When
        certificateStoreProcessor.process(message);

        // Then
        verify(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());
    }

    @Test(expected = TemporaryException.class)
    public void testStoreCertificateThrowsTemporaryOnApplicationError() throws Exception {
        // Given
        doThrow(applicationErrorException).when(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());

        // When
        certificateStoreProcessor.process(message);

        // Then
        verify(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());
    }

    @Test(expected = PermanentException.class)
    public void testStoreCertificateThrowsPermanentOnValidationError() throws Exception {
        // Given
        doThrow(validationErrorException).when(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());

        // When
        certificateStoreProcessor.process(message);

        // Then
        verify(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());
    }

    @Test(expected = PermanentException.class)
    public void testStoreCertificateThrowsPermanentOnTransformationError() throws Exception {
        // Given
        doThrow(transformationErrorException).when(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());

        // When
        certificateStoreProcessor.process(message);

        // Then
        verify(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());
    }

    @Test(expected = PermanentException.class)
    public void testStoreCertificateThrowsPermanentOnModuleException() throws Exception {
        // Given
        doThrow(new ModuleException()).when(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());

        // When
        certificateStoreProcessor.process(message);

        // Then
        verify(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());
    }

    @Test(expected = TemporaryException.class)
    public void testStoreCertificateThrowsTemporaryOnWebServiceException() throws Exception {
        // Given
        doThrow(new WebServiceException()).when(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());

        // When
        certificateStoreProcessor.process(message);

        // Then
        verify(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());
    }

    @Test(expected = PermanentException.class)
    public void testStoreCertificateThrowsPermanentOnException() throws Exception {
        // Given
        doThrow(new RuntimeException()).when(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());

        // When
        certificateStoreProcessor.process(message);

        // Then
        verify(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());
    }
}
