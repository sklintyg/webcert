/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.auth.oidc.jwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Key;
import java.util.ArrayList;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class JwtParserTest {

    @Test
    public void verifySignature() throws IOException {

        String jwtToken = readTokenFromDisk();

        SigningKeyResolverAdapter signingKeyResolverAdapter = buildResolverWithDiskBasedJwks();
        Jws<Claims> jws = Jwts.parser()
            .setSigningKeyResolver(signingKeyResolverAdapter)
            .setAllowedClockSkewSeconds(999999999999999L)
            .parseClaimsJws(jwtToken);

        assertNotNull(jws);
        ArrayList<String> employeeHsaIdList = (ArrayList<String>) jws.getBody().get("employeeHsaId");
        assertEquals("TST5565594230-10R3072", employeeHsaIdList.get(0));
    }

    @Test(expected = ExpiredJwtException.class)
    public void verifySignatureFailsDueToSkew() throws IOException {

        String jwtToken = readTokenFromDisk();
        SigningKeyResolverAdapter signingKeyResolverAdapter = buildResolverWithDiskBasedJwks();

        Jws<Claims> jws = Jwts.parser()
            .setSigningKeyResolver(signingKeyResolverAdapter)
            .setAllowedClockSkewSeconds(1)
            .parseClaimsJws(jwtToken);
    }

    private String readTokenFromDisk() throws IOException {
        String token = IOUtils.toString(new ClassPathResource("jwt/token.json").getInputStream(), Charset.forName("UTF-8"));
        JsonNode jsonNode = new ObjectMapper().readTree(token);
        return jsonNode.get("access_token").textValue();
    }


    private SigningKeyResolverAdapter buildResolverWithDiskBasedJwks() throws IOException {
        JwkProvider provider = new UrlJwkProvider(new ClassPathResource("jwt/jwks.json").getURL());
        return new SigningKeyResolverAdapter() {

            @Override
            public Key resolveSigningKey(JwsHeader jwsHeader, Claims claims) {
                // implement me
                try {
                    Jwk jwk = provider.get(jwsHeader.getKeyId());
                    return jwk.getPublicKey();
                } catch (JwkException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
