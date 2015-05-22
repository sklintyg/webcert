package se.inera.webcert.security;

import javax.xml.transform.stream.StreamSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.cxf.helpers.XMLUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;

import se.inera.auth.SakerhetstjanstAssertion;

/**
 * @author andreaskaltenbach
 */
public class SakerhetstjanstAssertionTest {

    private static Assertion assertionWithEnhet;
    private static Assertion assertionWithoutEnhet;
    private static Assertion assertionWithMultipleTitles;

    @BeforeClass
    public static void readSamlAssertions() throws Exception {
        DefaultBootstrap.bootstrap();

        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(Assertion.DEFAULT_ELEMENT_NAME);

        Document doc = (Document) XMLUtils.fromSource(new StreamSource(new ClassPathResource(
                "SakerhetstjanstAssertionTest/saml-assertion-with-enhet.xml").getInputStream()));
        assertionWithEnhet = (Assertion) unmarshaller.unmarshall(doc.getDocumentElement());

        doc = (Document) XMLUtils.fromSource(new StreamSource(new ClassPathResource(
                "SakerhetstjanstAssertionTest/saml-assertion-without-enhet.xml").getInputStream()));
        assertionWithoutEnhet = (Assertion) unmarshaller.unmarshall(doc.getDocumentElement());

        doc = (Document) XMLUtils.fromSource(new StreamSource(new ClassPathResource(
                "SakerhetstjanstAssertionTest/saml-assertion-with-multiple-titles.xml").getInputStream()));
        assertionWithMultipleTitles = (Assertion) unmarshaller.unmarshall(doc.getDocumentElement());
    }

    @Test
    public void testAssertionWithEnhetAndVardgivare() {

        SakerhetstjanstAssertion assertion = new SakerhetstjanstAssertion(assertionWithEnhet);

        assertTrue(assertion.getTitelKod().contains("204010"));
        assertEquals("8787878", assertion.getForskrivarkod());
        assertEquals("TST5565594230-106J", assertion.getHsaId());
        assertEquals("Markus", assertion.getFornamn());
        assertEquals("Gran", assertion.getMellanOchEfternamn());
        assertEquals("IFV1239877878-103H", assertion.getEnhetHsaId());
        assertEquals("VårdEnhet2A", assertion.getEnhetNamn());
        assertEquals("IFV1239877878-0001", assertion.getVardgivareHsaId());
        assertEquals("IFV Testdata", assertion.getVardgivareNamn());
    }

    @Test
    public void testAssertionWithoutEnhetAndVardgivare() {

        SakerhetstjanstAssertion assertion = new SakerhetstjanstAssertion(assertionWithoutEnhet);

        assertEquals("T_SERVICES_SE165565594230-106X", assertion.getHsaId());
    }

    @Test
    public void testAssertionWithMultipleTitles() {
        SakerhetstjanstAssertion assertion = new SakerhetstjanstAssertion(assertionWithMultipleTitles);
        assertTrue(assertion.getTitelKod().contains("204010"));
    }
}
