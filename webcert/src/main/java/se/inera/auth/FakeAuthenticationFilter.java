package se.inera.auth;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

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

        if (!"dev".equals(profiles) && !"demo".equals(profiles) && !"test".equals(profiles) && !"qa".equals(profiles)) {
            return null;
        }

        String parameter = request.getParameter("userJsonDisplay");
        // we manually encode the json parameter
        String json = URLDecoder.decode(parameter, "ISO-8859-1");

        try {
            FakeCredentials fakeCredentials = new ObjectMapper().readValue(json, FakeCredentials.class);
            LOG.info("Detected fake credentials " + fakeCredentials);
            return getAuthenticationManager().authenticate(new FakeAuthenticationToken(fakeCredentials));
        } catch (IOException e) {
            String message = "Failed to parse JSON: " + json;
            LOG.error(message, e);
            throw new RuntimeException(message, e);
        }
    }
}
