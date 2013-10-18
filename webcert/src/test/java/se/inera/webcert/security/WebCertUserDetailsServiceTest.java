package se.inera.webcert.security;

import javax.xml.transform.stream.StreamSource;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.cxf.helpers.XMLUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.NameID;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.saml.SAMLCredential;
import org.w3c.dom.Document;
import se.inera.auth.WebCertUserDetailsService;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.hsa.services.HsaOrganizationsService;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class WebCertUserDetailsServiceTest {

    public static final String HSA_ID = "TST5565594230-106J";
    @InjectMocks
    private WebCertUserDetailsService userDetailsService = new WebCertUserDetailsService();

    @Mock
    private HsaOrganizationsService hsaOrganizationsService;

    @Test
    public void testPopulatingWebCertUser() throws Exception {

        List<Vardgivare> vardgivare = Collections.singletonList(new Vardgivare("vg", "Landstinget Ingenmansland"));
        when(hsaOrganizationsService.getAuthorizedEnheterForHosPerson(HSA_ID)).thenReturn(vardgivare);

        SAMLCredential samlCredential = createSamlCredential("saml-assertion-with-title-code-lakare.xml");

        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertEquals(HSA_ID, webCertUser.getHsaId());
        assertEquals("Markus Gran", webCertUser.getNamn());
        assertEquals(1, webCertUser.getVardgivare().size());
        assertEquals("vg", webCertUser.getVardgivare().get(0).getId());

        verify(hsaOrganizationsService).getAuthorizedEnheterForHosPerson(HSA_ID);
    }

    @Test
    public void testLakareTitle() throws Exception {
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-with-title-lakare.xml");
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);
        assertTrue(webCertUser.isLakare());
    }

    @Test
    public void testLakareTitleCode() throws Exception {
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-with-title-code-lakare.xml");
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);
        assertTrue(webCertUser.isLakare());
    }

    @Test
    public void testNoLakare() throws Exception {
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-no-lakare.xml");
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);
        assertFalse(webCertUser.isLakare());
    }

    @BeforeClass
    public static void bootstrapOpenSaml() throws Exception {
        DefaultBootstrap.bootstrap();
    }

    private SAMLCredential createSamlCredential(String filename) throws Exception {
        Document doc = (Document) XMLUtils.fromSource(new StreamSource(new ClassPathResource(
                "WebCertUserDetailsServiceTest/" + filename).getInputStream()));
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(Assertion.DEFAULT_ELEMENT_NAME);

        Assertion assertion = (Assertion) unmarshaller.unmarshall(doc.getDocumentElement());
        NameID nameId = assertion.getSubject().getNameID();
        return new SAMLCredential(nameId, assertion, "remoteId", "localId");
    }

}
