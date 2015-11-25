package se.inera.intyg.webcert.notification_sender.certificatesender.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import javax.xml.ws.WebServiceException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.InternalModelHolder;
import se.inera.intyg.common.support.modules.support.api.exception.ExternalServiceCallException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.notification_sender.exception.PermanentException;
import se.inera.intyg.webcert.notification_sender.exception.TemporaryException;

/**
 * Created by eriklupander on 2015-05-22.
 */
@RunWith(MockitoJUnitRunner.class)
public class CertificateStoreProcessorTest {

    private static String LOGICAL_ADDRESS1 = "address-1";
    private static String BODY = "body";

    ExternalServiceCallException technicalErrorException = new ExternalServiceCallException("", ExternalServiceCallException.ErrorIdEnum.TECHNICAL_ERROR);
    ExternalServiceCallException applicationErrorException = new ExternalServiceCallException("", ExternalServiceCallException.ErrorIdEnum.APPLICATION_ERROR);
    ExternalServiceCallException validationErrorException = new ExternalServiceCallException("", ExternalServiceCallException.ErrorIdEnum.VALIDATION_ERROR);
    ExternalServiceCallException transformationErrorException = new ExternalServiceCallException("", ExternalServiceCallException.ErrorIdEnum.TRANSFORMATION_ERROR);

    @Mock
    IntygModuleRegistry moduleRegistry;

    @Mock
    ModuleApi moduleApi;

    @InjectMocks
    CertificateStoreProcessor certificateStoreProcessor = new CertificateStoreProcessor();

    @Before
    public void setup() throws ModuleNotFoundException {
        when(moduleRegistry.getModuleApi(anyString())).thenReturn(moduleApi);
    }

    @Test
    public void testStoreCertificate() throws Exception {
        // When
        certificateStoreProcessor.process(BODY, "fk7263", LOGICAL_ADDRESS1);

        // Then
        ArgumentCaptor<InternalModelHolder> internalModelHolder = ArgumentCaptor.forClass(InternalModelHolder.class);
        verify(moduleApi).registerCertificate(internalModelHolder.capture(), eq(LOGICAL_ADDRESS1));
        assertEquals(BODY, internalModelHolder.getValue().getInternalModel());
    }

    @Test(expected = TemporaryException.class)
    public void testStoreCertificateThrowsTemporaryOnTechnicalError() throws Exception {
        // Given
        doThrow(technicalErrorException).when(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());

        // When
        certificateStoreProcessor.process(BODY, "fk7263", LOGICAL_ADDRESS1);

        // Then
        verify(moduleApi).registerCertificate(any(InternalModelHolder.class), eq(LOGICAL_ADDRESS1));
    }

    @Test(expected = TemporaryException.class)
    public void testStoreCertificateThrowsTemporaryOnApplicationError() throws Exception {
        // Given
        doThrow(applicationErrorException).when(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());

        // When
        certificateStoreProcessor.process(BODY, "fk7263", LOGICAL_ADDRESS1);

        // Then
        verify(moduleApi).registerCertificate(any(InternalModelHolder.class), eq(LOGICAL_ADDRESS1));
    }

    @Test(expected = PermanentException.class)
    public void testStoreCertificateThrowsPermanentOnValidationError() throws Exception {
        // Given
        doThrow(validationErrorException).when(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());

        // When
        certificateStoreProcessor.process(BODY, "fk7263", LOGICAL_ADDRESS1);

        // Then
        verify(moduleApi).registerCertificate(any(InternalModelHolder.class), eq(LOGICAL_ADDRESS1));
    }

    @Test(expected = PermanentException.class)
    public void testStoreCertificateThrowsPermanentOnTransformationError() throws Exception {
        // Given
        doThrow(transformationErrorException).when(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());

        // When
        certificateStoreProcessor.process(BODY, "fk7263", LOGICAL_ADDRESS1);

        // Then
        verify(moduleApi).registerCertificate(any(InternalModelHolder.class), eq(LOGICAL_ADDRESS1));
    }

    @Test(expected = PermanentException.class)
    public void testStoreCertificateThrowsPermanentOnModuleException() throws Exception {
        // Given
        doThrow(new ModuleException()).when(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());

        // When
        certificateStoreProcessor.process(BODY, "fk7263", LOGICAL_ADDRESS1);

        // Then
        verify(moduleApi).registerCertificate(any(InternalModelHolder.class), eq(LOGICAL_ADDRESS1));
    }

    @Test(expected = TemporaryException.class)
    public void testStoreCertificateThrowsTemporaryOnWebServiceException() throws Exception {
        // Given
        doThrow(new WebServiceException()).when(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());

        // When
        certificateStoreProcessor.process(BODY, "fk7263", LOGICAL_ADDRESS1);

        // Then
        verify(moduleApi).registerCertificate(any(InternalModelHolder.class), eq(LOGICAL_ADDRESS1));
    }

    @Test(expected = PermanentException.class)
    public void testStoreCertificateThrowsPermanentOnException() throws Exception {
        // Given
        doThrow(new RuntimeException()).when(moduleApi).registerCertificate(any(InternalModelHolder.class), anyString());

        // When
        certificateStoreProcessor.process(BODY, "fk7263", LOGICAL_ADDRESS1);

        // Then
        verify(moduleApi).registerCertificate(any(InternalModelHolder.class), eq(LOGICAL_ADDRESS1));
    }
}
