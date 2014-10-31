package se.inera.auth;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;

import static se.inera.auth.SakerhetstjanstAssertion.ENHET_HSA_ID_ATTRIBUTE;
import static se.inera.auth.SakerhetstjanstAssertion.FORNAMN_ATTRIBUTE;
import static se.inera.auth.SakerhetstjanstAssertion.FORSKRIVARKOD_ATTRIBUTE;
import static se.inera.auth.SakerhetstjanstAssertion.HSA_ID_ATTRIBUTE;
import static se.inera.auth.SakerhetstjanstAssertion.MEDARBETARUPPDRAG_TYPE;
import static se.inera.auth.SakerhetstjanstAssertion.MEDARBETARUPPDRAG_ID;
import static se.inera.auth.SakerhetstjanstAssertion.MELLAN_OCH_EFTERNAMN_ATTRIBUTE;
import static se.inera.auth.SakerhetstjanstAssertion.TITEL_ATTRIBUTE;

import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.saml2.core.impl.AuthnContextBuilder;
import org.opensaml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml2.core.impl.AuthnStatementBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import se.inera.webcert.hsa.stub.Medarbetaruppdrag;

/**
 * @author andreaskaltenbach
 */
public class FakeAuthenticationProvider implements AuthenticationProvider {

    public static final String FAKE_AUTHENTICATION_CONTEXT_REF = "urn:inera:webcert:fake";

    private static DocumentBuilder documentBuilder;

    private SAMLUserDetailsService userDetails;

    static {
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Failed to instantiate DocumentBuilder", e);
        }
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        FakeAuthenticationToken token = (FakeAuthenticationToken) authentication;

        SAMLCredential credential = createSamlCredential(token);
        Object details = userDetails.loadUserBySAML(credential);

        ExpiringUsernameAuthenticationToken result = new ExpiringUsernameAuthenticationToken(null, details, credential,
                new ArrayList<GrantedAuthority>());
        result.setDetails(details);

        return result;
    }

    @Override
    public boolean supports(Class authentication) {
        return FakeAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private SAMLCredential createSamlCredential(FakeAuthenticationToken token) {
        FakeCredentials fakeCredentials = (FakeCredentials) token.getCredentials();

        Assertion assertion = new AssertionBuilder().buildObject();

        attachAuthenticationContext(assertion);

        AttributeStatement attributeStatement = new AttributeStatementBuilder().buildObject();
        assertion.getAttributeStatements().add(attributeStatement);

        attributeStatement.getAttributes().add(createAttribute(HSA_ID_ATTRIBUTE, fakeCredentials.getHsaId()));
        attributeStatement.getAttributes().add(createAttribute(FORNAMN_ATTRIBUTE, fakeCredentials.getFornamn()));
        attributeStatement.getAttributes().add(
                createAttribute(MELLAN_OCH_EFTERNAMN_ATTRIBUTE, fakeCredentials.getEfternamn()));
        attributeStatement.getAttributes().add(createAttribute(ENHET_HSA_ID_ATTRIBUTE, fakeCredentials.getEnhetId()));
        attributeStatement.getAttributes().add(createAttribute(MEDARBETARUPPDRAG_TYPE, Medarbetaruppdrag.VARD_OCH_BEHANDLING));
        attributeStatement.getAttributes().add(createAttribute(MEDARBETARUPPDRAG_ID, fakeCredentials.getEnhetId()));
        attributeStatement.getAttributes().add(createAttribute(FORSKRIVARKOD_ATTRIBUTE, fakeCredentials.getForskrivarKod()));

        if (fakeCredentials.isLakare()) {
            attributeStatement.getAttributes().add(createAttribute(TITEL_ATTRIBUTE, "Läkare"));
        }

        NameID nameId = new NameIDBuilder().buildObject();
        nameId.setValue(token.getCredentials().toString());
        return new SAMLCredential(nameId, assertion, "fake-idp", "webcert");
    }

    private void attachAuthenticationContext(Assertion assertion) {
        AuthnStatement authnStatement = new AuthnStatementBuilder().buildObject();
        AuthnContext authnContext = new AuthnContextBuilder().buildObject();
        AuthnContextClassRef authnContextClassRef = new AuthnContextClassRefBuilder().buildObject();

        authnContextClassRef.setAuthnContextClassRef(FAKE_AUTHENTICATION_CONTEXT_REF);
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnStatement.setAuthnContext(authnContext);
        assertion.getAuthnStatements().add(authnStatement);
    }

    private Attribute createAttribute(String name, String value) {

        Attribute attribute = new AttributeBuilder().buildObject();
        attribute.setName(name);

        Document doc = documentBuilder.newDocument();
        Element element = doc.createElement("element");
        element.setTextContent(value);

        XMLObject xmlObject = new XSStringBuilder().buildObject(new QName("ns", "local"));
        xmlObject.setDOM(element);
        attribute.getAttributeValues().add(xmlObject);

        return attribute;
    }

    public void setUserDetails(SAMLUserDetailsService userDetails) {
        this.userDetails = userDetails;
    }
}
