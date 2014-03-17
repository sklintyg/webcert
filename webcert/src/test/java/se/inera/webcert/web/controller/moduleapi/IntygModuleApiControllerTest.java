package se.inera.webcert.web.controller.moduleapi;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_IMPLEMENTED;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sun.xml.bind.v2.schemagen.xmlschema.Any;

import se.inera.certificate.integration.exception.ExternalWebServiceCallFailedException;
import se.inera.certificate.integration.rest.ModuleRestApi;
import se.inera.certificate.integration.rest.ModuleRestApiFactory;
import se.inera.certificate.integration.rest.dto.CertificateContentHolder;
import se.inera.certificate.integration.rest.dto.CertificateContentMeta;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.ExternalModelHolder;
import se.inera.certificate.modules.support.api.dto.PdfResponse;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.modules.registry.IntygModuleRegistry;
import se.inera.webcert.service.IntygService;
import se.inera.webcert.service.dto.IntygContentHolder;
import se.inera.webcert.service.dto.IntygMetadata;
import se.inera.webcert.service.log.LogService;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class IntygModuleApiControllerTest {

    private static final String CERTIFICATE_ID = "123456";
    private static final String CERTIFICATE_TYPE = "fk7263";
    private static final String PATIENT_ID = "19121212-1212";
    
    private static final byte[] PDF_DATA = "<pdf-data>".getBytes();
    private static final String PDF_NAME = "the-file.pdf";
    
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    
    private static IntygContentHolder utlatandeHolder;

    @Mock
    private IntygService intygService;

    @Mock
    private IntygModuleRegistry moduleRegistry;
    
    @Mock
    private ModuleApi moduleApi;
    
    @Mock
    private LogService logService;

    @InjectMocks
    private IntygModuleApiController moduleApiController = new IntygModuleApiController();

    @BeforeClass
    public static void setupCertificateData() throws IOException {

        IntygMetadata meta = new IntygMetadata();
        meta.setId(CERTIFICATE_ID);
        meta.setType(CERTIFICATE_TYPE);
        meta.setPatientId(PATIENT_ID);
        
        utlatandeHolder = new IntygContentHolder("<json>", meta);
    }

    @Test
    public void testGetCertificatePdf() throws Exception {

        when(intygService.fetchExternalIntygData(CERTIFICATE_ID)).thenReturn(utlatandeHolder);
        
        when(moduleRegistry.getModule(CERTIFICATE_TYPE)).thenReturn(moduleApi);
        
        PdfResponse pdfResponse = new PdfResponse(PDF_DATA, PDF_NAME);
        when(moduleApi.pdf(any(ExternalModelHolder.class))).thenReturn(pdfResponse);

        Response response = moduleApiController.getSignedIntygAsPdf(CERTIFICATE_ID);

        verify(intygService).fetchExternalIntygData(CERTIFICATE_ID);
        verify(moduleRegistry).getModule(CERTIFICATE_TYPE);
        verify(moduleApi).pdf(any(ExternalModelHolder.class));
        verify(logService).logPrintOfIntyg(CERTIFICATE_ID, PATIENT_ID);

        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(PDF_DATA, response.getEntity());
        assertNotNull(response.getHeaders().get(CONTENT_DISPOSITION));
    }

}
