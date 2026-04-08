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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.webcert.infra.monitoring.MonitoringConfiguration;
import se.inera.intyg.webcert.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.infra.security.common.model.Role;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MonitoringConfiguration.class})
class LogbackTest {

  static Logger LOG = LoggerFactory.getLogger(LogbackTest.class);

  @Autowired LogMDCHelper logMDCHelper;

  @Autowired LogMDCServletFilter logMDCServletFilter;

  private final Appender<ILoggingEvent> appender = Mockito.mock(Appender.class);

  public LogbackTest() {
    ((ch.qos.logback.classic.Logger) LOG).addAppender(appender);
  }

  @Test
  void logEventTest() {
    final var captureLogMessage = ArgumentCaptor.forClass(ILoggingEvent.class);
    LOG.error("Hello");
    verify(appender).doAppend(captureLogMessage.capture());

    final var loggingEvent = (LoggingEvent) captureLogMessage.getAllValues().get(0);
    assertEquals(1, captureLogMessage.getAllValues().size());
    assertEquals("Hello", loggingEvent.getMessage());
    assertEquals("ERROR", loggingEvent.getLevel().levelStr);
  }

  @Test
  void logContentContextTest() {
    final var captureLogMessage = ArgumentCaptor.forClass(ILoggingEvent.class);
    final var out = captureStdout(() -> LOG.info("Hello"));
    verify(appender).doAppend(captureLogMessage.capture());

    LoggingEvent le = (LoggingEvent) captureLogMessage.getAllValues().get(0);
    assertEquals(
        2,
        out.split(System.lineSeparator()).length,
        "Process and console appender should be triggered (2 records)");
    assertTrue(out.contains("[process,-,"));
    assertTrue(out.contains("[console,-,"));
    assertTrue(out.endsWith(": Hello" + System.lineSeparator()));
  }

  @Test
  void logExplicitTraceIdTest() {
    final String traceId = logMDCHelper.traceHeader();
    final String sessionInfo = "NO SESSION";
    Closeable trace = logMDCHelper.withSessionInfo(sessionInfo).withTraceId(traceId).openTrace();
    try {
      String out = captureStdout(() -> LOG.info("Hello"));
      assertTrue(out.contains("[process," + sessionInfo + "," + traceId));
    } finally {
      IOUtils.closeQuietly(trace);
    }
  }

  @Test
  void logImplicitTraceIdTest() {
    logMDCHelper.run(
        () -> {
          String out = captureStdout(() -> LOG.info(MarkerFilter.MONITORING, "Marker test"));

          String regex =
              String.format(
                  "^.* \\[monitoring,-,([%s)]+)\\,noUser].*$",
                  String.valueOf(LogMDCHelper.BASE62CHARS));
          Matcher m = Pattern.compile(regex).matcher(out);

          assertTrue(m.find());
          assertEquals(LogMDCHelper.IDLEN, m.group(1).length());
        });
  }

  @Test
  void logMarkerTest() {
    String out = captureStdout(() -> LOG.info(MarkerFilter.MONITORING, "Marker test"));
    assertTrue(out.contains("[monitoring,-,-,noUser]"));
    assertEquals(
        1,
        out.split(System.lineSeparator()).length,
        "Monitor appender only should be triggered (1 record)");
  }

  @Test
  void logAuthenticatedPrincipalTest() throws IOException {
    Authentication authentication = Mockito.mock(Authentication.class);
    IntygUser intygUser = Mockito.mock(IntygUser.class);
    when(intygUser.getHsaId()).thenReturn("hsaId");
    when(intygUser.getOrigin()).thenReturn("origin");
    when(intygUser.getRoles())
        .thenReturn(Collections.singletonMap("role", Mockito.mock(Role.class)));

    SelectableVardenhet vg = Mockito.mock(SelectableVardenhet.class);
    when(vg.getId()).thenReturn("vgId");
    when(intygUser.getValdVardgivare()).thenReturn(vg);

    SelectableVardenhet ve = Mockito.mock(SelectableVardenhet.class);
    when(ve.getId()).thenReturn("sevId");
    when(intygUser.getValdVardenhet()).thenReturn(ve);

    when(authentication.getPrincipal()).thenReturn(intygUser);

    SecurityContextHolder.getContext().setAuthentication(authentication);

    HttpServletRequest mockedRequest = Mockito.mock(HttpServletRequest.class);
    Cookie sessionCookie = new Cookie("SESSION", "sessionCookieValue");
    when(mockedRequest.getCookies())
        .thenReturn(new Cookie[] {new Cookie("test", "test"), sessionCookie});

    Closeable c = logMDCServletFilter.open(mockedRequest);
    String out = captureStdout(() -> LOG.info(MarkerFilter.MONITORING, "Auth Test"));
    assertTrue(out.contains("hsaId,sevId,origin,role,vgId]"));
    assertTrue(out.contains(sessionCookie.getValue()));
    c.close();
  }

  // returns stdout as a string (default encoding)
  private String captureStdout(final Runnable runnable) {
    final var out = new ByteArrayOutputStream();
    final var old = System.out;
    System.setOut(new PrintStream(out));
    try {
      runnable.run();
    } finally {
      System.out.flush();
      System.setOut(old);
    }
    return out.toString();
  }
}
