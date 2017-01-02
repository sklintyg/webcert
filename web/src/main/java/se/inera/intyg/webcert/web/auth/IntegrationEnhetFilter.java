/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.auth;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SPRING_SECURITY_CONTEXT;

@Component(value = "integrationEnhetFilter")
public class IntegrationEnhetFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationEnhetFilter.class);

    private static final String ENHET = "enhet";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestUrl = buildFullUrl(request);
        LOG.info("Intercepted djupintegrationslänk: " + requestUrl + " (" + request.getQueryString() + ")");

        HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        WebCertUser webCertUser = (WebCertUser) ((SecurityContextImpl) session.getAttribute(SPRING_SECURITY_CONTEXT)).getAuthentication().getPrincipal();
        Map<String, List<String>> queryMap = splitQuery(request.getQueryString());
        if (!queryMap.containsKey(ENHET)) {

            // If ENHET isn't set but the user only has one possible enhet that can be selected, we auto-select that one
            // explicitly and proceed down the filter chain. Typically, that unit should already have been selected by
            // the UserDetailsService that built the Principal, but better safe than sorry...
            if (userHasExactlyOneSelectableVardenhet(webCertUser)) {
                webCertUser.changeValdVardenhet(webCertUser.getVardgivare().get(0).getVardenheter().get(0).getId());
                filterChain.doFilter(request, response);
            } else {
                LOG.warn("Deep integration request does not contain an 'enhet', redirecting to enhet selection page!");
                response.sendRedirect("/web/dashboard?destination=" + URLEncoder.encode(requestUrl, "UTF-8") + "#/integration-enhetsval");
            }

        } else {
            List<String> enhet = queryMap.get(ENHET);
            if (webCertUser.changeValdVardenhet(enhet.get(0))) {
                filterChain.doFilter(request, response);
            } else {
                LOG.warn("Authorization Validation failed for deep-integration request for intyg because user {} is not authorized for enhet {}", webCertUser.getHsaId(), enhet.get(0));
                response.sendRedirect("/error.jsp?reason=login.medarbetaruppdrag");
            }
        }
    }

    private boolean userHasExactlyOneSelectableVardenhet(WebCertUser webCertUser) {
        return webCertUser.getVardgivare().stream()
                .distinct()
                .flatMap(vg -> vg.getVardenheter().stream().distinct())
                .count() == 1L;
    }

    private String buildFullUrl(HttpServletRequest request) {
        StringBuilder buf = new StringBuilder();
        buf.append(request.getRequestURL().toString());
        if (request.getQueryString() != null && request.getQueryString().trim().length() > 0) {
            buf.append("?" + request.getQueryString());
        }
        return buf.toString();
    }

    private Map<String, List<String>> splitQuery(String query) {
        if (Strings.isNullOrEmpty(query)) {
            return Collections.emptyMap();
        }
        return Arrays.stream(query.split("&"))
                .map(this::splitQueryParameter)
                .collect(Collectors.groupingBy(AbstractMap.SimpleImmutableEntry::getKey, LinkedHashMap::new, mapping(Map.Entry::getValue, toList())));
    }

    private AbstractMap.SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
        final int idx = it.indexOf("=");
        final String key = idx > 0 ? it.substring(0, idx) : it;
        final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

}
