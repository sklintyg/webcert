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
package se.inera.intyg.webcert.infra.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/** Created by marced on 10/03/16. */
public class SessionTimeoutFilter extends OncePerRequestFilter {

  public static final String TIME_TO_INVALIDATE_ATTRIBUTE_NAME =
      SessionTimeoutFilter.class.getName() + ".SessionTimeToInvalidate";
  public static final String SECONDS_UNTIL_SESSIONEXPIRE_ATTRIBUTE_KEY =
      SessionTimeoutFilter.class.getName() + ".secondsToLive";

  private static final Logger LOG = LoggerFactory.getLogger(SessionTimeoutFilter.class);

  static final String LAST_ACCESS_TIME_ATTRIBUTE_NAME =
      SessionTimeoutFilter.class.getName() + ".SessionLastAccessTime";

  private static final long MILLISECONDS_PER_SECONDS = 1000;

  private String skipRenewSessionUrls;
  private List<String> skipRenewSessionUrlsList;

  @Override
  protected void initFilterBean() throws ServletException {
    super.initFilterBean();
    if (skipRenewSessionUrls == null) {
      LOG.warn("No skipRenewSessionUrls are configured!");
      skipRenewSessionUrlsList = new ArrayList<>();
    } else {
      skipRenewSessionUrlsList = Arrays.asList(skipRenewSessionUrls.split(","));
      LOG.info(
          "Configured skipRenewSessionUrls as:"
              + skipRenewSessionUrlsList.stream()
                  .map(Object::toString)
                  .collect(Collectors.joining(", ")));
    }
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    checkSessionValidity(request);

    filterChain.doFilter(request, response);
  }

  private void checkSessionValidity(HttpServletRequest request) {
    // Get existing session - if any
    HttpSession session = request.getSession(false);

    // Is it a request that should'nt prolong the expiration?
    String url = request.getRequestURI();
    boolean skipSessionUpdate = skipRenewSessionUrlsList.stream().anyMatch(url::contains);

    if (session != null) {
      Long lastAccess = (Long) session.getAttribute(LAST_ACCESS_TIME_ATTRIBUTE_NAME);

      if (invalidateSessionIfTimeToInvalidateHasPassed(session)) {
        return;
      }

      // Set an request attribute that other parties further down the request chaing can use.
      Long msUntilExpire = updateTimeLeft(request, session);

      if (msUntilExpire <= 0) {
        LOG.info("Session expired " + msUntilExpire + " ms ago. Invalidating it now!");
        session.invalidate();
      } else if (!skipSessionUpdate || lastAccess == null) {
        // Update lastaccessed for ALL requests except status requests
        session.setAttribute(LAST_ACCESS_TIME_ATTRIBUTE_NAME, System.currentTimeMillis());
        updateTimeLeft(request, session);
      }
    }
  }

  private boolean invalidateSessionIfTimeToInvalidateHasPassed(HttpSession session) {
    final var invalidTime = (Long) session.getAttribute(TIME_TO_INVALIDATE_ATTRIBUTE_NAME);
    if (invalidTime == null) {
      return false;
    }

    final var currentTime = Instant.now().toEpochMilli();
    if (currentTime < invalidTime) {
      LOG.info(
          "Current time {} is before invalid time {} - The session remains valid!",
          currentTime,
          invalidTime);
      return false;
    }

    LOG.info(
        "Current time {} is past invalid time {} - The session will be invalidated!",
        currentTime,
        invalidTime);
    session.invalidate();
    return true;
  }

  private Long updateTimeLeft(HttpServletRequest request, HttpSession session) {
    Long lastAccess = (Long) session.getAttribute(LAST_ACCESS_TIME_ATTRIBUTE_NAME);
    long inactiveTime = (lastAccess == null) ? 0 : (System.currentTimeMillis() - lastAccess);
    long maxInactiveTime = session.getMaxInactiveInterval() * MILLISECONDS_PER_SECONDS;

    long msUntilExpire = maxInactiveTime - inactiveTime;
    request.setAttribute(
        SECONDS_UNTIL_SESSIONEXPIRE_ATTRIBUTE_KEY, msUntilExpire / MILLISECONDS_PER_SECONDS);
    return msUntilExpire;
  }

  public String getSkipRenewSessionUrls() {
    return this.skipRenewSessionUrls;
  }

  public void setSkipRenewSessionUrls(String skipRenewSessionUrls) {
    this.skipRenewSessionUrls = skipRenewSessionUrls;
  }
}
