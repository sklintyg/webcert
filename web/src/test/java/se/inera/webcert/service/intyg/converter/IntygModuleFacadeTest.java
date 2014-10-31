package se.inera.webcert.service.intyg.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.common.MinimalUtlatande;
import se.inera.certificate.modules.support.ApplicationOrigin;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.ExternalModelHolder;
import se.inera.certificate.modules.support.api.dto.ExternalModelResponse;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelResponse;
import se.inera.certificate.modules.support.api.dto.PdfResponse;
import se.inera.certificate.modules.support.api.dto.TransportModelHolder;
import se.inera.certificate.modules.support.api.dto.TransportModelResponse;
import se.inera.certificate.modules.support.api.dto.TransportModelVersion;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.modules.IntygModuleRegistry;
import se.inera.webcert.service.intyg.dto.IntygPdf;

@RunWith(MockitoJUnitRunner.class)
public class IntygModuleFacadeTest {

    private static final String CERTIFICATE_TYPE = "fk7263";

    private static final String EXT_JSON = "<ext-json>";

    private static final String INT_JSON = "<ext-json>";

    private static final String TRANSPORT_XML = "<xml/>";

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private ModuleApi moduleApi;

    @Mock
    private IntygModuleModelJaxbUtil jaxbUtil;

    @InjectMocks
    private IntygModuleFacadeImpl moduleFacade = new IntygModuleFacadeImpl();

    @Before
    public void setupCommonExpectations() throws Exception {
        // setup to return a mocked module API
        when(moduleRegistry.getModuleApi(CERTIFICATE_TYPE)).thenReturn(moduleApi);
    }

    @Test
    public void testConvertFromTransportToExternal() throws IntygModuleFacadeException, ModuleException, JAXBException, IOException {
        
        when(jaxbUtil.marshallFromTransportToXml(any(UtlatandeType.class))).thenReturn(TRANSPORT_XML);
        
        Utlatande utlatande = new MinimalUtlatande();
        ExternalModelResponse emr = new ExternalModelResponse(EXT_JSON, utlatande);
        when(moduleApi.unmarshall(any(TransportModelHolder.class))).thenReturn(emr);
        
        UtlatandeType utlatandeType = new UtlatandeType();
        ExternalModelResponse res = moduleFacade.convertFromTransportToExternal(CERTIFICATE_TYPE, utlatandeType);
        assertNotNull(res);
        
        verify(moduleRegistry).getModuleApi(CERTIFICATE_TYPE);
        verify(moduleApi).unmarshall(any(TransportModelHolder.class));
        verify(jaxbUtil).marshallFromTransportToXml(any(UtlatandeType.class));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testConvertFromInternalToTransport() throws ModuleException, IntygModuleFacadeException, JAXBException {
        
        Utlatande utlatande = new MinimalUtlatande();
        ExternalModelResponse emr = new ExternalModelResponse(EXT_JSON, utlatande);
        when(moduleApi.convertInternalToExternal(any(InternalModelHolder.class))).thenReturn(emr);

        TransportModelResponse transModelResponse = new TransportModelResponse(TRANSPORT_XML);
        when(moduleApi.marshall(any(ExternalModelHolder.class), any(TransportModelVersion.class))).thenReturn(transModelResponse);

        UtlatandeType utlatandeType = new UtlatandeType();
        when(jaxbUtil.unmarshallFromXmlToTransport(TRANSPORT_XML)).thenReturn(utlatandeType);

        UtlatandeType res = moduleFacade.convertFromInternalToTransport(CERTIFICATE_TYPE, INT_JSON);
        assertNotNull(res);

        // ensure correct module lookup is done with module registry
        verify(moduleRegistry).getModuleApi(CERTIFICATE_TYPE);
        verify(moduleApi).convertInternalToExternal(any(InternalModelHolder.class));
        verify(moduleApi).marshall(any(ExternalModelHolder.class), any(TransportModelVersion.class));
        verify(jaxbUtil).unmarshallFromXmlToTransport(anyString());

    }

    @Test
    public void testConvertFromExternalToPdfDocument() throws IntygModuleFacadeException, ModuleException {
        
        byte[] pdfData = "PDFDATA".getBytes();
        PdfResponse pdfResp = new PdfResponse(pdfData , "file.pdf");
        when(moduleApi.pdf(any(ExternalModelHolder.class), any(ApplicationOrigin.class))).thenReturn(pdfResp);
        
        IntygPdf intygPdf = moduleFacade.convertFromExternalToPdfDocument(CERTIFICATE_TYPE, EXT_JSON);
        assertNotNull(intygPdf.getPdfData());
        assertEquals("file.pdf", intygPdf.getFilename());
        
        verify(moduleApi).pdf(any(ExternalModelHolder.class), eq(ApplicationOrigin.WEBCERT));
        
    }
    
    @Test
    public void testConvertFromExternalToInternal() throws IntygModuleFacadeException, ModuleException {

        InternalModelResponse imr = new InternalModelResponse(INT_JSON);
        when(moduleApi.convertExternalToInternal(any(ExternalModelHolder.class))).thenReturn(imr);

        String res = moduleFacade.convertFromExternalToInternal(CERTIFICATE_TYPE, EXT_JSON);
        assertEquals(INT_JSON, res);

        verify(moduleRegistry).getModuleApi(CERTIFICATE_TYPE);
        verify(moduleApi).convertExternalToInternal(any(ExternalModelHolder.class));
    }

    @Test
    public void testConvertFromInternalToExternal() throws IntygModuleFacadeException, ModuleException {

        ExternalModelResponse emr = new ExternalModelResponse(EXT_JSON, new MinimalUtlatande());
        when(moduleApi.convertInternalToExternal(any(InternalModelHolder.class))).thenReturn(emr);

        Utlatande res = moduleFacade.convertFromInternalToExternal(CERTIFICATE_TYPE, INT_JSON);
        assertNotNull(res);
        
        verify(moduleRegistry).getModuleApi(CERTIFICATE_TYPE);
        verify(moduleApi).convertInternalToExternal(any(InternalModelHolder.class));
        
    }
        
}
