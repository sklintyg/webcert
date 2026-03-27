/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.infra.monitoring.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/** Tags log records with trace id and session id. */
public class LogMDCServletFilter implements Filter {

  @Autowired private LogMDCHelper mdcHelper;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    final Closeable trace = open(request);
    try {
      chain.doFilter(request, response);
    } finally {
      IOUtils.closeQuietly(trace);
    }
  }

  @Override
  public void init(FilterConfig filterConfig) {
    SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(
        this, filterConfig.getServletContext());
  }

  @Override
  public void destroy() {}

  Closeable open(final ServletRequest request) {
    if (request instanceof HttpServletRequest) {
      final HttpServletRequest http = ((HttpServletRequest) request);
      mdcHelper
          .withTraceId(http.getHeader(mdcHelper.traceHeader()))
          .withSessionInfo(sessionId(http));
    }
    return mdcHelper.openTrace();
  }

  // check cookie instead of http session, since this filer shall not create or use
  // sessions
  String sessionId(HttpServletRequest http) {
    final Cookie[] cookies = http.getCookies();

    return Objects.isNull(cookies)
        ? null
        : Stream.of(cookies)
            .filter(c -> "SESSION".equals(c.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
  }
}
