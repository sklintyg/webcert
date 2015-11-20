package se.inera.intyg.webcert.web.auth;

import static se.inera.intyg.webcert.web.auth.common.AuthConstants.FAKE_AUTHENTICATION_SITHS_CONTEXT_REF;
import static se.inera.intyg.webcert.web.security.SakerhetstjanstAssertion.ENHET_HSA_ID_ATTRIBUTE;
import static se.inera.intyg.webcert.web.security.SakerhetstjanstAssertion.FORNAMN_ATTRIBUTE;
import static se.inera.intyg.webcert.web.security.SakerhetstjanstAssertion.FORSKRIVARKOD_ATTRIBUTE;
import static se.inera.intyg.webcert.web.security.SakerhetstjanstAssertion.HSA_ID_ATTRIBUTE;
import static se.inera.intyg.webcert.web.security.SakerhetstjanstAssertion.MEDARBETARUPPDRAG_ID;
import static se.inera.intyg.webcert.web.security.SakerhetstjanstAssertion.MEDARBETARUPPDRAG_TYPE;
import static se.inera.intyg.webcert.web.security.SakerhetstjanstAssertion.MELLAN_OCH_EFTERNAMN_ATTRIBUTE;
import static se.inera.intyg.webcert.web.security.SakerhetstjanstAssertion.TITEL_ATTRIBUTE;
import static se.inera.intyg.webcert.web.security.SakerhetstjanstAssertion.TITEL_KOD_ATTRIBUTE;

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
import se.inera.intyg.webcert.web.auth.common.BaseFakeAuthenticationProvider;
import se.inera.webcert.hsa.stub.Medarbetaruppdrag;

import java.util.ArrayList;

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
    public boolean supports(Class<?> authentication) {
        return FakeAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public void setUserDetails(SAMLUserDetailsService userDetails) {
        this.userDetails = userDetails;
    }

    private SAMLCredential createSamlCredential(FakeAuthenticationToken token) {
        FakeCredentials fakeCredentials = (FakeCredentials) token.getCredentials();

        Assertion assertion = new AssertionBuilder().buildObject();

        attachAuthenticationContext(assertion, FAKE_AUTHENTICATION_SITHS_CONTEXT_REF);

        AttributeStatement attributeStatement = new AttributeStatementBuilder().buildObject();
        assertion.getAttributeStatements().add(attributeStatement);

        addAttribute(attributeStatement, HSA_ID_ATTRIBUTE, fakeCredentials.getHsaId());
        addAttribute(attributeStatement, FORNAMN_ATTRIBUTE, fakeCredentials.getFornamn());
        addAttribute(attributeStatement, MELLAN_OCH_EFTERNAMN_ATTRIBUTE, fakeCredentials.getEfternamn());
        addAttribute(attributeStatement, ENHET_HSA_ID_ATTRIBUTE, fakeCredentials.getEnhetId());
        addAttribute(attributeStatement, MEDARBETARUPPDRAG_TYPE, Medarbetaruppdrag.VARD_OCH_BEHANDLING);
        addAttribute(attributeStatement, MEDARBETARUPPDRAG_ID, fakeCredentials.getEnhetId());
        addAttribute(attributeStatement, FORSKRIVARKOD_ATTRIBUTE, fakeCredentials.getForskrivarKod());

        if (fakeCredentials.isLakare()) {
            addAttribute(attributeStatement, TITEL_ATTRIBUTE, "Läkare");
        }
        if (fakeCredentials.isTandlakare()) {
            addAttribute(attributeStatement, TITEL_ATTRIBUTE, "Tandläkare");
        }

        addAttribute(attributeStatement, TITEL_KOD_ATTRIBUTE, fakeCredentials.getBefattningsKod());

        NameID nameId = new NameIDBuilder().buildObject();
        nameId.setValue(token.getCredentials().toString());
        return new SAMLCredential(nameId, assertion, "fake-idp", "webcert");
    }

    private void addAttribute(AttributeStatement attributeStatement, String attributeName, String attributeValue) {
        if (attributeName == null || attributeValue == null) {
            return;
        }

        attributeStatement.getAttributes().add(createAttribute(attributeName, attributeValue));
    }

}
