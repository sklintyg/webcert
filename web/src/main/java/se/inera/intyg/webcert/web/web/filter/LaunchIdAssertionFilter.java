/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Component(value = "launchIdAssertionFilter")
public class LaunchIdAssertionFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(UnitSelectedAssuranceFilter.class);
    @Autowired
    WebCertUserService webCertUserService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (continueFilterChain(request)) {
            filterChain.doFilter(request, response);
        } else {
            WebCertUser user = webCertUserService.getUser();
            String launchId = request.getHeader("launchId");
            if (!user.getParameters().getLaunchId().equals(launchId)) {
                LOG.error("launchId does not match with current session - session will be invalidated");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } else {
                filterChain.doFilter(request, response);
            }
        }
    }

    private boolean continueFilterChain(HttpServletRequest request) {
        return request.getHeader("launchId") == null || webCertUserService.getUser() == null
            || webCertUserService.getUser().getParameters() == null;
    }
}
