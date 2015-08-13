package se.inera.auth;

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
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import se.inera.auth.common.BaseFakeAuthenticationProvider;
import se.inera.webcert.hsa.stub.Medarbetaruppdrag;

/**
 * @author andreaskaltenbach
 */
public class FakeAuthenticationProvider extends BaseFakeAuthenticationProvider {

    private SAMLUserDetailsService userDetails;

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

        attachAuthenticationContext(assertion, BaseFakeAuthenticationProvider.FAKE_AUTHENTICATION_SITHS_CONTEXT_REF);

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


    public void setUserDetails(SAMLUserDetailsService userDetails) {
        this.userDetails = userDetails;
    }
}
