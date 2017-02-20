/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.auth.fake.common;

import static se.inera.intyg.webcert.web.auth.common.AuthConstants.FAKE_AUTHENTICATION_SITHS_CONTEXT_REF;

import java.util.ArrayList;

import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;

import com.google.common.base.Strings;

import se.inera.intyg.infra.integration.hsa.model.Mottagning;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.infra.security.siths.BaseSakerhetstjanstAssertion;
import se.inera.intyg.webcert.web.auth.common.BaseFakeAuthenticationProvider;
import se.inera.intyg.webcert.web.auth.fake.FakeAuthenticationToken;
import se.inera.intyg.webcert.web.auth.fake.FakeCredentials;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;

/**
 * @author andreaskaltenbach
 */
public class CommonFakeAuthenticationProvider extends BaseFakeAuthenticationProvider {

    private SAMLUserDetailsService userDetails;

    @Override
    public Authentication authenticate(Authentication token) throws AuthenticationException {

        SAMLCredential credential = createSamlCredential(token);
        Object details = userDetails.loadUserBySAML(credential);

        addAbsentAttributesFromFakeCredentials(token, details);
        selectVardenhetFromFakeCredentials(token, details);
        applyUserOrigin(token, details);
        applyReference(token, details);
        ExpiringUsernameAuthenticationToken result = new ExpiringUsernameAuthenticationToken(null, details, credential,
                new ArrayList<>());
        result.setDetails(details);

        return result;
    }

    private void applyReference(Authentication token, Object details) {
        if (details instanceof IntygUser) {
            if (token.getCredentials() != null && ((FakeCredentials) token.getCredentials()).getReference() != null) {
                ((IntygUser) details).setReference(((FakeCredentials) token.getCredentials()).getReference());
            }
        }
    }

    private void applyUserOrigin(Authentication token, Object details) {
        if (details instanceof IntygUser) {
            if (token.getCredentials() != null && ((FakeCredentials) token.getCredentials()).getOrigin() != null) {
                String origin = ((FakeCredentials) token.getCredentials()).getOrigin();
                try {
                    WebCertUserOriginType.valueOf(origin); // Type check.
                    ((IntygUser) details).setOrigin(origin);
                } catch (IllegalArgumentException e) {
                    throw new AuthoritiesException(
                            "Could not set origin '" + origin + "'. Unknown, allowed types are NORMAL, DJUPINTEGRATION, UTHOPP");
                }
            }
        }
    }

    private void addAbsentAttributesFromFakeCredentials(Authentication token, Object details) {
        if (details instanceof IntygUser) {
            IntygUser user = (IntygUser) details;
            if (user.getNamn() == null || user.getNamn().isEmpty()) {
                user.setNamn(
                        ((FakeCredentials) token.getCredentials()).getFornamn() + " "
                                + ((FakeCredentials) token.getCredentials()).getEfternamn());
            }
        }
    }

    private void selectVardenhetFromFakeCredentials(Authentication token, Object details) {
        if (details instanceof IntygUser) {
            IntygUser user = (IntygUser) details;
            FakeCredentials fakeCredentials = (FakeCredentials) token.getCredentials();
            if (!Strings.isNullOrEmpty(fakeCredentials.getEnhetId())) {
                setVardenhetById(fakeCredentials.getEnhetId(), user);
                setVardgivareByVardenhetId(fakeCredentials.getEnhetId(), user);
            }
        }
    }

    private void setVardgivareByVardenhetId(String enhetId, IntygUser intygUser) {
        for (Vardgivare vg : intygUser.getVardgivare()) {
            for (Vardenhet ve : vg.getVardenheter()) {
                if (ve.getId().equals(enhetId)) {
                    intygUser.setValdVardgivare(vg);
                    return;
                } else if (ve.getMottagningar() != null) {
                    for (Mottagning m : ve.getMottagningar()) {
                        if (m.getId().equals(enhetId)) {
                            intygUser.setValdVardgivare(vg);
                            return;
                        }
                    }
                }
            }
        }
        throw new AuthoritiesException("Could not select a Vårdgivare given the fake credentials, not logging in.");
    }

    private void setVardenhetById(String enhetId, IntygUser intygUser) {
        for (Vardgivare vg : intygUser.getVardgivare()) {
            for (Vardenhet ve : vg.getVardenheter()) {
                if (ve.getId().equals(enhetId)) {
                    intygUser.setValdVardenhet(ve);
                    return;
                } else if (ve.getMottagningar() != null) {
                    for (Mottagning m : ve.getMottagningar()) {
                        if (m.getId().equals(enhetId)) {
                            intygUser.setValdVardenhet(m);
                            return;
                        }
                    }
                }
            }
        }
        throw new AuthoritiesException("Could not select a Vardenhet given the fake credentials, not logging in.");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return FakeAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public void setUserDetails(SAMLUserDetailsService userDetails) {
        this.userDetails = userDetails;
    }

    private SAMLCredential createSamlCredential(Authentication token) {
        FakeCredentials fakeCredentials = (FakeCredentials) token.getCredentials();

        Assertion assertion = new AssertionBuilder().buildObject();

        attachAuthenticationContext(assertion, FAKE_AUTHENTICATION_SITHS_CONTEXT_REF);

        AttributeStatement attributeStatement = new AttributeStatementBuilder().buildObject();
        assertion.getAttributeStatements().add(attributeStatement);

        addAttribute(attributeStatement, BaseSakerhetstjanstAssertion.HSA_ID_ATTRIBUTE, fakeCredentials.getHsaId());

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
