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

import static se.inera.intyg.webcert.logging.MdcLogConstants.ORGANIZATION_CARE_PROVIDER_ID;
import static se.inera.intyg.webcert.logging.MdcLogConstants.ORGANIZATION_ID;
import static se.inera.intyg.webcert.logging.MdcLogConstants.USER_ID;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import lombok.Setter;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Setter
@Component
public class MdcUserServletFilter implements Filter {

  @Autowired
  private  WebCertUserService webCertUserService;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      if (webCertUserService.hasAuthenticationContext()) {
        addUserContextToMdc();
      }
      chain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }

  private void addUserContextToMdc() {
    final var user = webCertUserService.getUser();
    MDC.put(USER_ID, user.getHsaId());
    MDC.put(ORGANIZATION_ID,
        user.getValdVardenhet() != null ? user.getValdVardenhet().getId() : "-");
    MDC.put(ORGANIZATION_CARE_PROVIDER_ID,
        user.getValdVardgivare() != null ? user.getValdVardgivare().getId() : "-");
  }

  @Override
  public void init(FilterConfig filterConfig) {
    SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,
        filterConfig.getServletContext());
  }
}
