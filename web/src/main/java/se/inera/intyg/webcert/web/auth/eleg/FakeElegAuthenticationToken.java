package se.inera.intyg.webcert.web.auth.eleg;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Simple auth token for fake logins for e-leg.
 *
 * Created by eriklupander on 2015-06-16.
 */
public class FakeElegAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = -2796850504529240890L;

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
