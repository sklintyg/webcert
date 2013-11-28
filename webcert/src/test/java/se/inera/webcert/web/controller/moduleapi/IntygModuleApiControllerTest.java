package se.inera.webcert.web.controller.moduleapi;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_IMPLEMENTED;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.certificate.integration.exception.ExternalWebServiceCallFailedException;
import se.inera.certificate.integration.rest.ModuleRestApi;
import se.inera.certificate.integration.rest.ModuleRestApiFactory;
import se.inera.certificate.integration.rest.dto.CertificateContentHolder;
import se.inera.certificate.integration.rest.dto.CertificateContentMeta;
import se.inera.webcert.service.IntygService;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class IntygModuleApiControllerTest {

    private static final String CERTIFICATE_ID = "123456";
    private static final String CERTIFICATE_TYPE = "fk7263";

    private static CertificateContentHolder utlatandeHolder;

    @Mock
    private IntygService intygService = mock(IntygService.class);

    @Mock
    private ModuleRestApiFactory moduleRestApiFactory;

    @Mock
    private ModuleRestApi moduleRestApi;

    @InjectMocks
    private IntygModuleApiController moduleApiController = new IntygModuleApiController();

    @BeforeClass
    public static void setupCertificateData() throws IOException {

        utlatandeHolder = new CertificateContentHolder();

        CertificateContentMeta meta = new CertificateContentMeta();
        meta.setId(CERTIFICATE_ID);
        meta.setType(CERTIFICATE_TYPE);
        utlatandeHolder.setCertificateContentMeta(meta);
    }

    @Test
    public void testGetCertificatePdf() throws IOException {

        when(intygService.fetchExternalIntygData(CERTIFICATE_ID)).thenReturn(utlatandeHolder);
        when(moduleRestApiFactory.getModuleRestService(CERTIFICATE_TYPE)).thenReturn(moduleRestApi);

        // Mimic the module API to which the PDF generation is delegated to.
        // We return an HTTP 200 together with some mock PDF data.
        Response moduleCallResponse = mock(Response.class);
        when(moduleCallResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(moduleCallResponse.getEntity()).thenReturn("<pdf-file>");
        when(moduleRestApi.pdf(utlatandeHolder)).thenReturn(moduleCallResponse);

        Response response = moduleApiController.getCertificatePdf(CERTIFICATE_ID);

        verify(intygService).fetchExternalIntygData(CERTIFICATE_ID);
        verify(moduleRestApiFactory).getModuleRestService(CERTIFICATE_TYPE);
        verify(moduleRestApi).pdf(utlatandeHolder);

        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals("<pdf-file>", response.getEntity());
    }

    @Test
    public void testGetCertificatePdfWithFailingModule() throws IOException {
        when(intygService.fetchExternalIntygData(CERTIFICATE_ID)).thenReturn(utlatandeHolder);
        when(moduleRestApiFactory.getModuleRestService(CERTIFICATE_TYPE)).thenReturn(moduleRestApi);

        // Mimic the module API to which the PDF generation is delegated to.
        // We return an HTTP 501.
        Response moduleCallResponse = mock(Response.class);
        when(moduleCallResponse.getStatus()).thenReturn(Response.Status.NOT_IMPLEMENTED.getStatusCode());
        when(moduleRestApi.pdf(utlatandeHolder)).thenReturn(moduleCallResponse);

        Response response = moduleApiController.getCertificatePdf(CERTIFICATE_ID);

        verify(intygService).fetchExternalIntygData(CERTIFICATE_ID);
        verify(moduleRestApiFactory).getModuleRestService(CERTIFICATE_TYPE);
        verify(moduleRestApi).pdf(utlatandeHolder);

        assertEquals(NOT_IMPLEMENTED.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
    }

    @Test
    public void testGetCertificatePdfWithFailingIntygstjanst() {
        Response certificateResponse = mock(Response.class);
        when(certificateResponse.getStatus()).thenReturn(Response.Status.FORBIDDEN.getStatusCode());
        when(intygService.fetchExternalIntygData(CERTIFICATE_ID)).thenThrow(ExternalWebServiceCallFailedException.class);

        Response response = moduleApiController.getCertificatePdf(CERTIFICATE_ID);

        assertEquals(INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
    }
}
