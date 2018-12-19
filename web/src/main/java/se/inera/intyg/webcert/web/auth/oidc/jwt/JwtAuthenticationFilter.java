package se.inera.intyg.webcert.web.auth.oidc.jwt;

import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.MissingClaimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import se.inera.intyg.webcert.web.service.jwt.JwtIntrospectionService;
import se.inera.intyg.webcert.web.service.jwt.JwtValidationService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtValidationService jwtValidationService;

    @Autowired
    private JwtIntrospectionService jwtIntrospectionService;

    protected JwtAuthenticationFilter(RequestMatcher requestMatcher) {
        super(requestMatcher);
        LOG.error("JWT Authentication enabled. DO NOT USE IN PRODUCTION UNLESS YOU KNOW WHAT YOU ARE DOING!!!");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        // Check for Authorization: Bearer
        String authHeaderValue = request.getHeader("Authorization");

        // If there's a Authorization: Bearer: .... token, try JWT login.
        if (authHeaderValue != null && authHeaderValue.startsWith("Bearer: ")) {

            // Extract jwsToken from header
            String jwsToken = authHeaderValue.substring("Bearer: ".length());

            // Validate JWS token signature
            Jws<Claims> jwt = jwtValidationService.validateJwsToken(jwsToken);

            // If the JWT has a valid signature, call the introspection service to validate it.
            jwtIntrospectionService.validateToken(jwsToken);

            // If both signature and introspection is OK, extract the employeeHsaId and initiate authorization.
            String employeeHsaId = jwt.getBody().get("employeeHsaId", String.class);
            if (Strings.isNullOrEmpty(employeeHsaId)) {
                throw new MissingClaimException(jwt.getHeader(), jwt.getBody(), "Could extract claim for employeeHsaId");
            }

            // Build authentication token and proceed with authorization.
            JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(employeeHsaId);
            return getAuthenticationManager().authenticate(jwtAuthenticationToken);

        }
        throw new AuthenticationServiceException("Request contained no 'Authorization: Bearer: <JWS token>' header");
    }
}
