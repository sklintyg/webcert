/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import se.inera.intyg.webcert.logging.MdcCloseableMap;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Setter
@Component
public class MdcUserServletFilter implements Filter {

    @Autowired
    private WebCertUserService webCertUserService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        if (webCertUserService.hasAuthenticationContext()) {
            final var user = webCertUserService.getUser();
            final var selectedUnit = user.getValdVardenhet();
            final var selectedCareProvider = user.getValdVardgivare();
            try (final var mdcLogConstants =
                MdcCloseableMap.builder()
                    .put(MdcLogConstants.USER_ID, user.getHsaId())
                    .put(MdcLogConstants.ORGANIZATION_ID, selectedUnit != null ? selectedUnit.getId() : "-")
                    .put(MdcLogConstants.ORGANIZATION_CARE_PROVIDER_ID, selectedCareProvider != null ? selectedCareProvider.getId() : "-")
                    .build()
            ) {
                chain.doFilter(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,
            filterConfig.getServletContext());
    }
}
