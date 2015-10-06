package se.inera.auth.eleg;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Simple auth token for fake logins for e-leg.
 *
 * Created by eriklupander on 2015-06-16.
 */
public class FakeElegAuthenticationToken extends AbstractAuthenticationToken {

    private FakeElegCredentials fakeElegCredentials;

    public FakeElegAuthenticationToken(FakeElegCredentials fakeElegCredentials) {
        super(null);
        this.fakeElegCredentials = fakeElegCredentials;
    }

    @Override
    public Object getCredentials() {
        return fakeElegCredentials;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }
}
