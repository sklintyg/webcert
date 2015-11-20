package se.inera.intyg.webcert.web.auth;

import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.inera.intyg.webcert.web.auth.eleg.FakeElegAuthenticationToken;
import se.inera.intyg.webcert.web.auth.eleg.FakeElegCredentials;

/**
 * @author andreaskaltenbach
 */
public class FakeAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger LOG = LoggerFactory.getLogger(FakeAuthenticationFilter.class);

    protected FakeAuthenticationFilter() {
        super("/fake");
        LOG.error("FakeAuthentication enabled. DO NOT USE IN PRODUCTION");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        if (request.getCharacterEncoding() == null) {
            request.setCharacterEncoding("UTF-8");
        }
        String parameter = request.getParameter("userJsonDisplay");
        // we manually encode the json parameter
        String json = URLDecoder.decode(parameter, "UTF-8");

        // Start of separate code paths for SakerhetsTjanst vs CGI (privatlakare) fakes
        JsonNode jsonNode = new ObjectMapper().readTree(json);
        if (jsonNode.has("privatLakare") && jsonNode.get("privatLakare").asBoolean()) {
            return performFakeElegAuthentication(json);
        } else {
            return performFakeSithsAuthentication(json);
        }
    }

    private Authentication performFakeElegAuthentication(String json) {
        try {
            FakeElegCredentials fakeElegCredentials = new ObjectMapper().readValue(json, FakeElegCredentials.class);
            LOG.info("Detected fake credentials " + fakeElegCredentials);
            return getAuthenticationManager().authenticate(new FakeElegAuthenticationToken(fakeElegCredentials));
        } catch (IOException e) {
            String message = "Failed to parse JSON for fake E-leg: " + json;
            LOG.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    private Authentication performFakeSithsAuthentication(String json) {
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
