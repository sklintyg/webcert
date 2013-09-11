package se.inera.auth;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import se.inera.webcert.security.WebCertUser;

/**
 * @author andreaskaltenbach
 */
public class FakeAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    protected FakeAuthenticationFilter() {
        super("/fake");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        String json = request.getParameter("userjson");

        if (json != null) {

            try {
                WebCertUser webCertUser = new ObjectMapper().readValue(json, WebCertUser.class);

                TestingAuthenticationToken token = new TestingAuthenticationToken(webCertUser, null);
                return getAuthenticationManager().authenticate(token);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        }

        return null; // T
    }
}
