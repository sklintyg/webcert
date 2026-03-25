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
package se.inera.intyg.webcert.infra.security.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.infra.security.filter.SessionTimeoutFilter.TIME_TO_INVALIDATE_ATTRIBUTE_NAME;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Created by marced on 10/03/16. */
@ExtendWith(MockitoExtension.class)
class SessionTimeoutFilterTest {

  private static final String SKIP_RENEW_URL = "/test";
  private static final String OTHER_URL = "/any.html";
  private static final int FIVE_SECONDS_AGO = 5000;
  private static final int ONE_SECOND = 1;
  private static final int HALF_AN_HOUR = 1800;

  private SessionTimeoutFilter filter;

  @Mock HttpServletRequest request;

  @Mock HttpServletResponse response;

  @Mock FilterChain filterChain;

  @Mock HttpSession session;

  @BeforeEach
  void setupFilter() throws Exception {
    filter = new SessionTimeoutFilter();
    filter.setSkipRenewSessionUrls(SKIP_RENEW_URL);
    filter.initFilterBean();
  }

  @Test
  void testDoFilterInvalidSession() throws Exception {
    // Arrange
    setupMocks(ONE_SECOND, OTHER_URL);

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(session).invalidate();
    verify(session, never()).setAttribute(any(), any());
  }

  @Test
  void testDoFilterInvalidSessionWithSkipUrl() throws Exception {
    // Arrange
    setupMocks(ONE_SECOND, SKIP_RENEW_URL);

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(session).invalidate();
    verify(session, never()).setAttribute(any(), any());
  }

  @Test
  void testDoFilterValidSession() throws Exception {
    // Arrange
    setupMocks(HALF_AN_HOUR, OTHER_URL);

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(session, never()).invalidate();
    verify(session).setAttribute(eq(SessionTimeoutFilter.LAST_ACCESS_TIME_ATTRIBUTE_NAME), any());
  }

  @Test
  void testDoFilterValidSessionWithSkipUrl() throws Exception {
    // Arrange
    setupMocks(HALF_AN_HOUR, SKIP_RENEW_URL);

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(session, never()).invalidate();
    verify(session, never()).setAttribute(any(), any());
  }

  @Test
  void testInvalidateSessionIfTimeToInvalidateHasPassed() throws Exception {
    // Arrange
    setupMocks(HALF_AN_HOUR, SKIP_RENEW_URL, Instant.now().minusSeconds(1).toEpochMilli());

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(session, times(1)).invalidate();
    verify(session, never()).setAttribute(any(), any());
  }

  @Test
  void testDontInvalidateSessionIfTimeToInvalidateHasNotPassed() throws Exception {
    // Arrange
    setupMocks(HALF_AN_HOUR, SKIP_RENEW_URL, Instant.now().plusSeconds(2).toEpochMilli());

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(session, never()).invalidate();
    verify(session, never()).setAttribute(any(), any());
  }

  @Test
  void testDontInvalidateSessionIfTimeToInvalidateIsNull() throws Exception {
    // Arrange
    setupMocks(HALF_AN_HOUR, SKIP_RENEW_URL, null);

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(session, never()).invalidate();
    verify(session, never()).setAttribute(any(), any());
  }

  private void setupMocks(
      int sessionLengthInSeconds, String reportedRequestURI, Long timeToInvalidate) {
    setupMocks(sessionLengthInSeconds, reportedRequestURI);
    doReturn(timeToInvalidate).when(session).getAttribute(TIME_TO_INVALIDATE_ATTRIBUTE_NAME);
  }

  private void setupMocks(int sessionLengthInSeconds, String reportedRequestURI) {

    when(request.getSession(false)).thenReturn(session);
    when(request.getRequestURI()).thenReturn(reportedRequestURI);
    when(session.getAttribute(SessionTimeoutFilter.LAST_ACCESS_TIME_ATTRIBUTE_NAME))
        .thenReturn(System.currentTimeMillis() - FIVE_SECONDS_AGO);
    lenient().when(session.getMaxInactiveInterval()).thenReturn(sessionLengthInSeconds);
  }
}
