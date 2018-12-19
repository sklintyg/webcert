package se.inera.intyg.webcert.web.auth.oidc;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.Key;

import static org.junit.Assert.assertNotNull;

public class JwtParserTest {

    @Test
    public void verifySignature() throws IOException {

        String token = IOUtils.toString(new ClassPathResource("jwt/token.json").getInputStream(), Charset.forName("UTF-8"));
        JsonNode jsonNode = new ObjectMapper().readTree(token);

        String jwtToken = jsonNode.get("access_token").textValue();

        JwkProvider provider = new UrlJwkProvider(new URL("https://idp.ineradev.org/oidc/jwks.json"));
        SigningKeyResolverAdapter signingKeyResolverAdapter = new SigningKeyResolverAdapter() {

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
        Jws<Claims> jws = Jwts.parser()
                .setSigningKeyResolver(signingKeyResolverAdapter)
                .setAllowedClockSkewSeconds(999999999)
                .parseClaimsJws(jwtToken);

        assertNotNull(jws);
    }
}
