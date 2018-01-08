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
package se.inera.intyg.webcert.web.auth.eleg;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Requests picked up by this Filter will always break the filter chain and send a redirect
 * to the IdP configured for "e-legitimation".
 *
 * The intended usage is to map this filter to a simple URL such as /web/eleglogin that external systems
 * can use to initiate a federated authentication without having to know implementation details such as the exact
 * URL to the IdP.
 *
 * Created by eriklupander on 2015-09-28.
 */
@Component(value = "elegRedirectFilters")
public class ElegRedirectFilter extends OncePerRequestFilter {

    @Value("${cgi.funktionstjanster.saml.idp.metadata.url}")
    private String elegIdpUrl;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
            throws ServletException, IOException {
        if (elegIdpUrl == null || elegIdpUrl.trim().length() == 0) {
            throw new IllegalStateException("Cannot redirect to e-leg Identity Provider, "
                    + "no 'cgi.funktionstjanster.saml.idp.metadata.url' configured. Check your webcert.properties file.");
        }

        httpServletResponse.sendRedirect("/saml/login/alias/eleg?idp=" + elegIdpUrl);
    }

    // Package public for unit-testing.
    void setElegIdpUrl(String elegIdpUrl) {
        this.elegIdpUrl = elegIdpUrl;
    }
}
