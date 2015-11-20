package se.inera.intyg.webcert.web.web.controller.moduleapi;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.joda.time.LocalDateTime;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.Status;
import se.inera.certificate.model.common.internal.Utlatande;
import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class IntygModuleApiControllerTest {

    private static final String CERTIFICATE_ID = "123456";
    private static final String CERTIFICATE_TYPE = "fk7263";

    private static final byte[] PDF_DATA = "<pdf-data>".getBytes();
    private static final String PDF_NAME = "the-file.pdf";

    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    @Mock
    private IntygService intygService;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private ModuleApi moduleApi;

    @Mock
    private LogService logService;

    @Mock
    private WebCertUserService webcertUserService;

    @InjectMocks
    private IntygModuleApiController moduleApiController = new IntygModuleApiController();

    @BeforeClass
    public static void setupCertificateData() throws IOException {

        Utlatande utlatande = new Utlatande();
        utlatande.setId(CERTIFICATE_ID);
        utlatande.setTyp(CERTIFICATE_TYPE);

        List<Status> status = new ArrayList<Status>();
        status.add(new Status(CertificateState.RECEIVED, "MI", LocalDateTime.now()));
        status.add(new Status(CertificateState.SENT, "FK", LocalDateTime.now()));
    }

    @Test
    public void testGetCertificatePdf() throws Exception {

        IntygPdf pdfResponse = new IntygPdf(PDF_DATA, PDF_NAME);

        when(intygService.fetchIntygAsPdf(CERTIFICATE_ID, "fk7263", false)).thenReturn(pdfResponse);

        Response response = moduleApiController.getIntygAsPdf("fk7263", CERTIFICATE_ID);

        verify(intygService).fetchIntygAsPdf(CERTIFICATE_ID, "fk7263", false);

        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(PDF_DATA, response.getEntity());
        assertNotNull(response.getHeaders().get(CONTENT_DISPOSITION));
    }

}
