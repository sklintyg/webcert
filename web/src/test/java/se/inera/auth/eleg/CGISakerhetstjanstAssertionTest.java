package se.inera.auth.eleg;

import static org.junit.Assert.assertEquals;

import javax.xml.transform.stream.StreamSource;

import org.apache.cxf.helpers.XMLUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;

/**
 * @author eriklupander
 */
public class CGISakerhetstjanstAssertionTest {

    private static org.opensaml.saml2.core.Assertion assertionPrivatlakare;

    @BeforeClass
    public static void readSamlAssertions() throws Exception {
        DefaultBootstrap.bootstrap();

        Document doc = (Document) XMLUtils.fromSource(new StreamSource(new ClassPathResource(
                "CGIElegAssertiontest/sample-saml2-response.xml").getInputStream()));
        org.w3c.dom.Element documentElement = doc.getDocumentElement();

        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(documentElement);
        XMLObject responseXmlObj = unmarshaller.unmarshall(documentElement);
        Response response = (Response) responseXmlObj;
        assertionPrivatlakare = response.getAssertions().get(0);

    }

    @Test
    public void testAssertionWithEnhetAndVardgivare() {

        CgiElegAssertion assertion = new CgiElegAssertion(assertionPrivatlakare);

        assertEquals("191212121212", assertion.getPersonId());
        assertEquals("Tolvan", assertion.getFornamn());
        assertEquals("Tolvansson", assertion.getEfternamn());

        assertEquals("Testbank A e-Customer CA1 for BankID", assertion.getUtfardareCANamn());
        assertEquals("Testbank A AB (publ)", assertion.getUtfardareOrganisationsNamn());
        assertEquals("3", assertion.getSecurityLevel());

        assertEquals("urn:oasis:names:tc:SAML:2.0:ac:classes:SoftwarePKI", assertion.getAuthenticationScheme());
    }
}
