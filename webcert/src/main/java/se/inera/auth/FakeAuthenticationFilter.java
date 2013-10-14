package se.inera.auth;

import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import se.inera.webcert.security.WebCertUser;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author andreaskaltenbach
 */
public class FakeAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger LOG = LoggerFactory.getLogger(FakeAuthenticationFilter.class);

    @Value("${spring.profiles.active}")
    private String profiles;

    protected FakeAuthenticationFilter() {
        super("/fake");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        if (!"dev".equals(profiles) && !"test".equals(profiles)) {
            return null;
        }
        
        String json = request.getParameter("userjson");
        //we manually encode the json parameter
        json = URLDecoder.decode(json,"ISO-8859-1");
        if (json == null) {
            return null;
        }

        try {
            WebCertUser webCertUser = new ObjectMapper().readValue(json, WebCertUser.class);
            TestingAuthenticationToken token = new TestingAuthenticationToken(webCertUser, null);

            LOG.info("Fake authentication with user " + webCertUser);
            return getAuthenticationManager().authenticate(token);

        } catch (IOException e) {
            String message = "Failed to parse JSON: " + json;
            LOG.error(message, e);
            throw new RuntimeException(message, e);
        }
    }
}
