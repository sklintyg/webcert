package se.inera.auth.eleg;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.security.saml.SAMLCredential;
import se.inera.auth.SakerhetstjanstAssertion;
import se.inera.auth.common.BaseSAMLCredentialTest;

import static org.junit.Assert.assertEquals;

/**
 * Created by eriklupander on 2015-08-26.
 */
public class ElegAuthenticationAttributeHelperTest extends BaseSAMLCredentialTest {

    ElegAuthenticationAttributeHelperImpl testee;

    @BeforeClass
    public static void setupAsssertions() throws Exception {
        bootstrapSamlAssertions();
    }

    @Test
    public void testReadStringAttribute() {
        testee = new ElegAuthenticationAttributeHelperImpl();
        SAMLCredential cred = buildPrivatlakareSamlCredential();
        String personId = testee.getAttribute(cred, CgiElegAssertion.PERSON_ID_ATTRIBUTE);
        assertEquals("191212121212", personId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadUnknownAttribute() {
        testee = new ElegAuthenticationAttributeHelperImpl();
        SAMLCredential cred = buildLandstingslakareSamlCredential();
        testee.getAttribute(cred, CgiElegAssertion.PERSON_ID_ATTRIBUTE);
    }

    @Test
    public void testReadDOMTypeAttribute() {
        testee = new ElegAuthenticationAttributeHelperImpl();
        SAMLCredential cred = buildLandstingslakareSamlCredential();
        String fornamn = testee.getAttribute(cred, SakerhetstjanstAssertion.FORNAMN_ATTRIBUTE);
        assertEquals("Markus", fornamn);
    }
}
