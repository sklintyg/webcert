package se.inera.webcert.web.controller.moduleapi;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.ExternalModelHolder;
import se.inera.certificate.modules.support.api.dto.PdfResponse;
import se.inera.webcert.modules.IntygModuleRegistry;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.intyg.dto.IntygContentHolder;
import se.inera.webcert.service.intyg.dto.IntygMetadata;
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
        
        PdfResponse pdfResponse = new PdfResponse(PDF_DATA, PDF_NAME);
        
        when(intygService.fetchIntygAsPdf(CERTIFICATE_ID)).thenReturn(pdfResponse);

        Response response = moduleApiController.getSignedIntygAsPdf(CERTIFICATE_ID);

        verify(intygService).fetchIntygAsPdf(CERTIFICATE_ID);

        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(PDF_DATA, response.getEntity());
        assertNotNull(response.getHeaders().get(CONTENT_DISPOSITION));
    }

}
