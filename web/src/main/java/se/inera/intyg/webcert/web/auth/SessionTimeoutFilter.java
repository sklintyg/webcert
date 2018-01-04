/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.springframework.web.filter.OncePerRequestFilter;

public class SessionTimeoutFilter extends OncePerRequestFilter {

    private static final String SESSION_LAST_ACCESS_TIME = "SessionLastAccessTime";
    private static final long MILLISECONDS_PER_SECONDS = 1000;

    private String ignoredUrl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            Long lastAccess = (Long) session.getAttribute(SESSION_LAST_ACCESS_TIME);

            long inactiveTime = lastAccess == null ? 0 : System.currentTimeMillis() - lastAccess;
            long maxInactiveTime =  MILLISECONDS_PER_SECONDS * session.getMaxInactiveInterval();

            if (inactiveTime > maxInactiveTime) {
                session.invalidate();

            } else if (!request.getRequestURI().contains(ignoredUrl)) {
                session.setAttribute(SESSION_LAST_ACCESS_TIME, System.currentTimeMillis());
            }
        }
        filterChain.doFilter(request, response);
    }

    public void setIgnoredUrl(String ignoredUrl) {
        this.ignoredUrl = ignoredUrl;
    }
}
