package se.inera.webcert.security;

import javax.xml.transform.stream.StreamSource;

import static org.junit.Assert.assertEquals;

import org.apache.cxf.helpers.XMLUtils;
import org.junit.Test;
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
import se.inera.webcert.hsa.model.WebCertUser;

/**
 * @author andreaskaltenbach
 */
public class WebCertUserDetailsServiceTest {

    @Test
    public void test() throws Exception {
        WebCertUserDetailsService userDetailsService = new WebCertUserDetailsService();

        SAMLCredential samlCredential = createSamlCredential();

        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertEquals("TST5565594230-106J", webCertUser.getHsaId());
    }

    private SAMLCredential createSamlCredential() throws Exception {
        DefaultBootstrap.bootstrap();
        Document doc = (Document) XMLUtils.fromSource(new StreamSource(new ClassPathResource("SakerhetstjanstAssertionTest/saml-assertion.xml").getInputStream()));
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(Assertion.DEFAULT_ELEMENT_NAME);

        Assertion assertion = (Assertion) unmarshaller.unmarshall(doc.getDocumentElement());
        NameID nameId = assertion.getSubject().getNameID();
        return new SAMLCredential(nameId, assertion, "remoteId", "localId");
    }

}
