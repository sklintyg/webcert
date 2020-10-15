/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;


@Component(value = "unitSelectedAssuranceFilter")
public class UnitSelectedAssuranceFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(UnitSelectedAssuranceFilter.class);

    @Autowired
    private WebCertUserService userService;

    private String ignoredUrls;
    private List<String> ignoredUrlsList;

    @Override
    protected void initFilterBean() throws ServletException {
        if (ignoredUrls == null) {
            LOG.warn("No ignored Urls are configured!");
        } else {
            ignoredUrlsList = Arrays.asList(ignoredUrls.split(","));
            LOG.info("Configured ignored urls as:" + ignoredUrlsList.stream().map(Object::toString).collect(Collectors.joining(", ")));
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        LOG.error("User accessed " + request.getRequestURI() + " but has not selected a vardEnhet");
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        WebCertUser user = getUser();
        boolean shouldNotFilterIf = user == null || user.getValdVardenhet() != null || isIgnoredUrl(request);
        LOG.debug("shouldNotFilter " + request.getRequestURI() + " = " + shouldNotFilterIf);
        return shouldNotFilterIf;
    }

    private boolean isIgnoredUrl(HttpServletRequest request) {
        String url = request.getRequestURI();
        boolean shouldIgnore = ignoredUrlsList.stream().filter(s -> url.contains(s)).count() > 0;
        LOG.debug("shouldIgnore " + url + " = " + shouldIgnore);
        return shouldIgnore;
    }

    public String getIgnoredUrls() {
        return ignoredUrls;
    }

    public void setIgnoredUrls(String ignoredUrls) {
        this.ignoredUrls = ignoredUrls;
    }

    private WebCertUser getUser() {
        if (userService.hasAuthenticationContext()) {
            return userService.getUser();
        } else {
            return null;
        }
    }
}
