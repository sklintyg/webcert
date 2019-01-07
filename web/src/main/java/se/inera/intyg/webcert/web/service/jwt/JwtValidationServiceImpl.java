/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

/**
 * Class responsible for validating a JWT token given the configured JWKS URI.
 */
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
