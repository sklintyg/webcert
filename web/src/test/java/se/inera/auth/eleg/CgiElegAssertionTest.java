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
import se.inera.auth.common.BaseSAMLCredentialTest;

/**
 * @author eriklupander
 */
public class CgiElegAssertionTest extends BaseSAMLCredentialTest {

    @BeforeClass
    public static void readSamlAssertions() throws Exception {
        bootstrapSamlAssertions();
    }

    @Test
    public void testAssertionWithEnhetAndVardgivare() {

        CgiElegAssertion assertion = new CgiElegAssertion(assertionPrivatlakare);

        assertEquals("197705232382", assertion.getPersonId());
        assertEquals("Frida", assertion.getFornamn());
        assertEquals("Kranstege", assertion.getEfternamn());

        assertEquals("Testbank A e-Customer CA2 for BankID", assertion.getUtfardareCANamn());
        assertEquals("Testbank A AB (publ)", assertion.getUtfardareOrganisationsNamn());
        assertEquals("3", assertion.getSecurityLevel());

        assertEquals("urn:oasis:names:tc:SAML:2.0:ac:classes:SoftwarePKI", assertion.getAuthenticationScheme());
        assertEquals("ccp10", assertion.getLoginMethod());
    }
}
