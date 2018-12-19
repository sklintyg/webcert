package se.inera.intyg.webcert.web.service.jwt;

import com.auth0.jwk.GuavaCachedJwkProvider;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;

@Service
public class JwtValidationServiceImpl implements JwtValidationService {

    @Value("${idp.oidc.jwks.url}")
    private String idpOidcJwksUrl;

    @Value("${idp.oidc.jwks.skew}")
    private Long idpOidcJwksSkewSeconds;

    private JwkProvider provider;

    @PostConstruct
    public void init() {
        // Initialize provider
        provider = new GuavaCachedJwkProvider(new UrlJwkProvider(buildUrl()));
    }

    @Override
    public Jws<Claims> validateJwsToken(String jwtToken) {

        return Jwts.parser()
                .setSigningKeyResolver(new WebcertSigningKeyResolverAdapter(provider))
                .setAllowedClockSkewSeconds(idpOidcJwksSkewSeconds)
                .parseClaimsJws(jwtToken);
    }

    private URL buildUrl() {
        try {
            return new URL(idpOidcJwksUrl);
        } catch (MalformedURLException e) {
            throw new AuthenticationServiceException(
                    "Unable to construct URL for jwks from " + idpOidcJwksUrl + ". Message: " + e.getMessage());
        }
    }

    private class WebcertSigningKeyResolverAdapter extends SigningKeyResolverAdapter {

        private JwkProvider provider;

        WebcertSigningKeyResolverAdapter(JwkProvider provider) {
            this.provider = provider;
        }

        @Override
        public Key resolveSigningKey(JwsHeader jwsHeader, Claims claims) {
            try {
                Jwk jwk = provider.get(jwsHeader.getKeyId());
                return jwk.getPublicKey();
            } catch (JwkException e) {
                throw new AuthenticationServiceException("Unable to resolve public key for JWS signature validation from " + idpOidcJwksUrl
                        + ". Message: " + e.getMessage());
            }
        }
    };
}
