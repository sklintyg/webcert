package se.inera.intyg.webcert.web.auth.oidc.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final String userHsaId;

    public JwtAuthenticationToken(String userHsaId) {
        super(null);
        this.userHsaId = userHsaId;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    public String getUserHsaId() {
        return userHsaId;
    }
}
