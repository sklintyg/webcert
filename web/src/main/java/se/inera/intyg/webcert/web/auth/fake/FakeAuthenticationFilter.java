/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.auth.fake;

import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SPRING_SECURITY_SAVED_REQUEST_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import se.inera.intyg.webcert.web.auth.RedisSavedRequestCache;
import se.inera.intyg.webcert.web.auth.SavedRequestFactory;
import se.inera.intyg.webcert.web.auth.eleg.FakeElegAuthenticationToken;
import se.inera.intyg.webcert.web.auth.eleg.FakeElegCredentials;


/**
 * @author andreaskaltenbach
 */
public class FakeAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger LOG = LoggerFactory.getLogger(FakeAuthenticationFilter.class);

    @Autowired
    private SavedRequestFactory savedRequestFactory;

    @Autowired
    private RedisSavedRequestCache redisSavedRequestCache;

    protected FakeAuthenticationFilter() {
        super("/fake");
        LOG.error("FakeAuthentication enabled. DO NOT USE IN PRODUCTION");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException, IOException, ServletException {

        final var session = request.getSession(true);
        if (session != null) {
            final var savedRequest = savedRequestFactory.buildSavedRequest(request);
            session.setAttribute(SPRING_SECURITY_SAVED_REQUEST_KEY, savedRequest);
            redisSavedRequestCache.saveRequest(request, response);
        }

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
