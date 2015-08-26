package se.inera.auth.eleg;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.auth.common.BaseSAMLCredentialTest;
import se.inera.webcert.hsa.model.AuthenticationMethod;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2015-08-26.
 */
@RunWith(MockitoJUnitRunner.class)
public class ElegAuthenticationMethodResolverTest extends BaseSAMLCredentialTest {

    private static final java.lang.String BANK_ID_ISSUER = "Testbank A e-Customer CA1 for BankID";
    private static final java.lang.String NET_ID_ISSUER = "Telia e-legitimation Test PP CA v3";
    private static final java.lang.String INDETERMINATE_ISSUER = "Telia fake string with BankID";
    private static final java.lang.String UNKNOWN_ISSUER = "Not a known issuer";

    String netIdIssuerCommonNames = "telia";

    String bankIdIssuerCommonNames = "bankid";

    @Mock
    ElegAuthenticationAttributeHelper elegAuthenticationAttributeHelper;

    @InjectMocks
    ElegAuthenticationMethodResolverImpl testee;

    @BeforeClass
    public static void setup() throws Exception {
        bootstrapSamlAssertions();

    }

    @Before
    public void initIssuerTokens() {
        ReflectionTestUtils.setField(testee, "netIdIssuerCommonNames", netIdIssuerCommonNames);
        ReflectionTestUtils.setField(testee, "bankIdIssuerCommonNames", bankIdIssuerCommonNames);
    }


    @Test
    public void testBankID() {
        when(elegAuthenticationAttributeHelper.getAttribute(any(SAMLCredential.class), anyString())).thenReturn(BANK_ID_ISSUER);
        AuthenticationMethod authMetod = testee.resolveAuthenticationMethod(buildPrivatlakareSamlCredential());
        assertEquals(AuthenticationMethod.BANK_ID, authMetod);
    }

    @Test
    public void testNetID() {
        when(elegAuthenticationAttributeHelper.getAttribute(any(SAMLCredential.class), anyString())).thenReturn(NET_ID_ISSUER);
        AuthenticationMethod authMetod = testee.resolveAuthenticationMethod(buildPrivatlakareSamlCredential());
        assertEquals(AuthenticationMethod.NET_ID, authMetod);
    }

    @Test(expected = IllegalStateException.class)
    public void testNullNetIdIdentifierValueThrowsException() {
        ReflectionTestUtils.setField(testee, "netIdIssuerCommonNames", null);
        when(elegAuthenticationAttributeHelper.getAttribute(any(SAMLCredential.class), anyString())).thenReturn(BANK_ID_ISSUER);
        testee.resolveAuthenticationMethod(buildPrivatlakareSamlCredential());
    }

    @Test(expected = IllegalStateException.class)
    public void testBlankNetIdIdentifierValueThrowsException() {
        ReflectionTestUtils.setField(testee, "netIdIssuerCommonNames", " ");
        when(elegAuthenticationAttributeHelper.getAttribute(any(SAMLCredential.class), anyString())).thenReturn(BANK_ID_ISSUER);
        testee.resolveAuthenticationMethod(buildPrivatlakareSamlCredential());
    }

    @Test(expected = IllegalStateException.class)
    public void testNullBankIdIdentifierValueThrowsException() {
        ReflectionTestUtils.setField(testee, "bankIdIssuerCommonNames", null);
        when(elegAuthenticationAttributeHelper.getAttribute(any(SAMLCredential.class), anyString())).thenReturn(BANK_ID_ISSUER);
        testee.resolveAuthenticationMethod(buildPrivatlakareSamlCredential());
    }

    @Test(expected = IllegalStateException.class)
    public void testBlankBankIdIdentifierValueThrowsException() {
        ReflectionTestUtils.setField(testee, "bankIdIssuerCommonNames", " ");
        when(elegAuthenticationAttributeHelper.getAttribute(any(SAMLCredential.class), anyString())).thenReturn(BANK_ID_ISSUER);
        testee.resolveAuthenticationMethod(buildPrivatlakareSamlCredential());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoIssuerThrowsException() {
        when(elegAuthenticationAttributeHelper.getAttribute(any(SAMLCredential.class), anyString())).thenReturn(null);
        testee.resolveAuthenticationMethod(buildPrivatlakareSamlCredential());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testIndeterminateIssuerThrowsException() {
        when(elegAuthenticationAttributeHelper.getAttribute(any(SAMLCredential.class), anyString())).thenReturn(INDETERMINATE_ISSUER);
        testee.resolveAuthenticationMethod(buildPrivatlakareSamlCredential());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknwonIssuerThrowsException() {
        when(elegAuthenticationAttributeHelper.getAttribute(any(SAMLCredential.class), anyString())).thenReturn(UNKNOWN_ISSUER);
        testee.resolveAuthenticationMethod(buildPrivatlakareSamlCredential());
    }
}