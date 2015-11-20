package se.inera.intyg.webcert.web.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * @author andreaskaltenbach
 */
public class FakeAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 6816599869136456844L;

    private FakeCredentials fakeCredentials;

    public FakeAuthenticationToken(FakeCredentials fakeCredentials) {
        super(null);
        this.fakeCredentials = fakeCredentials;
    }

    @Override
    public Object getCredentials() {
        return fakeCredentials;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }
}
