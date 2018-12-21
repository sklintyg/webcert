package se.inera.intyg.webcert.web.auth.oidc.jwt;

import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.MissingClaimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import se.inera.intyg.webcert.web.service.jwt.JwtIntrospectionService;
import se.inera.intyg.webcert.web.service.jwt.JwtValidationService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

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
        String jwsToken = extractAccessToken(request);
        return authenticate(jwsToken);
    }

    private Authentication authenticate(String jwsToken) {
        // Validate JWS token signature
        Jws<Claims> jwt = jwtValidationService.validateJwsToken(jwsToken);

        // If the JWT has a valid signature, call the introspection service to validate it.
        jwtIntrospectionService.validateToken(jwsToken);

        // If both signature and introspection is OK, extract the employeeHsaId and initiate authorization.
        Object hsaIdObj = jwt.getBody().get("employeeHsaId");
        String employeeHsaId = null;
        if (hsaIdObj instanceof String) {
            employeeHsaId = (String) hsaIdObj;
        } else if (hsaIdObj instanceof ArrayList) {
            ArrayList<String> parts = (ArrayList) hsaIdObj;
            if (parts != null && parts.size() > 0) {
                employeeHsaId = parts.get(0);
            }
        } else {
            throw new IncorrectClaimException(jwt.getHeader(), jwt.getBody(),
                    "Could not extract claim for employeeHsaId, claim was neither of class String nor ArrayList");
        }
        if (Strings.isNullOrEmpty(employeeHsaId)) {
            throw new MissingClaimException(jwt.getHeader(), jwt.getBody(), "Could extract claim for employeeHsaId");
        }

        // Build authentication token and proceed with authorization.
        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(employeeHsaId);
        return getAuthenticationManager().authenticate(jwtAuthenticationToken);
    }

    private String extractAccessToken(HttpServletRequest request) {

        // If POST, check body. If it contains something, assume it's an access token.
        if (request.getMethod().equalsIgnoreCase(HttpMethod.POST.name())) {

            String accessToken = request.getParameter("access_token");
            return accessToken;
            // Try extract access_token from POST body
            // try {
            // String accessToken = IOUtils.toString(request.getInputStream());
            // if (accessToken.startsWith("access_token=")) {
            // accessToken = accessToken.substring("access_token=".length());
            // } else {
            // throw new IllegalArgumentException("Unknown token prefix");
            // }
            // // String accessToken = IOUtils.toString(request.getReader());
            // if (!Strings.isNullOrEmpty(accessToken)) {
            // return accessToken;
            // }
            // } catch (IOException e) {
            // // Ignore silently for now...
            // }
        }

        // Otherwise, check for authorization bearer
        String authHeaderValue = request.getHeader("Authorization");

        // If there's a Authorization: Bearer: .... token, try JWT login.
        if (!Strings.isNullOrEmpty(authHeaderValue) && authHeaderValue.startsWith("Bearer: ")) {
            return authHeaderValue.substring("Bearer: ".length());
        }
        throw new AuthenticationServiceException("Request contained no 'Authorization: Bearer: <JWS token>' header or POST body");
    }
}
