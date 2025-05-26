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
import java.util.Map;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.logging.MdcCloseableMap;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

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
            final var unitId = selectedUnitId(user.getValdVardenhet());
            final var careUnitId = selectedCareUnitId(user.getValdVardenhet());
            final var careProviderId = selectedUnitId(user.getValdVardgivare());
            final var userRole = userRole(user.getRoles());
            final var userOrigin = userOrigin(user);
            try (final var mdcLogConstants =
                MdcCloseableMap.builder()
                    .put(MdcLogConstants.USER_ID, user.getHsaId())
                    .put(MdcLogConstants.ORGANIZATION_ID, unitId)
                    .put(MdcLogConstants.ORGANIZATION_CARE_UNIT_ID, careUnitId)
                    .put(MdcLogConstants.ORGANIZATION_CARE_PROVIDER_ID, careProviderId)
                    .put(MdcLogConstants.USER_ORIGIN, userOrigin)
                    .put(MdcLogConstants.USER_ROLE, userRole)
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

    private String selectedUnitId(SelectableVardenhet selectableVardenhet) {
        if (selectableVardenhet == null) {
            return "-";
        }
        return selectableVardenhet.getId();
    }

    private String selectedCareUnitId(SelectableVardenhet selectedVardenhet) {
        if (selectedVardenhet instanceof Mottagning mottagning) {
            return mottagning.getParentHsaId();
        }
        return selectedUnitId(selectedVardenhet);
    }

    private static String userRole(Map<String, Role> roles) {
        return roles != null && roles.size() == 1 ? roles.keySet().iterator().next() : MdcLogConstants.NO_ROLE;
    }

    private static String userOrigin(WebCertUser user) {
        return user.getOrigin() != null ? user.getOrigin() : MdcLogConstants.NO_ORIGIN;
    }
}
