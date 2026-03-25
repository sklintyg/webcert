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
package se.inera.intyg.webcert.infra.monitoring.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import se.inera.intyg.infra.monitoring.MonitoringConfiguration;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MonitoringConfiguration.class})
class MarkerFilterTest {

  private MarkerFilter markerFilter;
  private final FilterReply accept = FilterReply.ACCEPT;
  private final FilterReply deny = FilterReply.DENY;
  private static final String TEST_MARKER_MONITORING = "Monitoring";
  private static final String TEST_MARKER_VALIDATION = "Validation";

  @BeforeEach
  public void setUp() {
    markerFilter = new MarkerFilter();
    markerFilter.setName("testfilter");
    markerFilter.setOnMatch(accept);
    markerFilter.setOnMismatch(deny);
  }

  @Test
  void testSingleMarkerAcceptsMatchingEvent() {
    markerFilter.setMarker(TEST_MARKER_MONITORING);
    markerFilter.start();
    Marker marker = MarkerFactory.getMarker(TEST_MARKER_MONITORING);
    ILoggingEvent matchingEvent = mock(ILoggingEvent.class);
    when(matchingEvent.getMarker()).thenReturn(marker);
    assertEquals(accept, markerFilter.decide(matchingEvent));
  }

  @Test
  void testSingleMarkerRejectsNonMatchingEvent() {
    markerFilter.setMarker(TEST_MARKER_MONITORING);
    markerFilter.start();
    Marker anotherMarker = MarkerFactory.getMarker(TEST_MARKER_VALIDATION);
    ILoggingEvent nonMatchingEvent = mock(ILoggingEvent.class);
    when(nonMatchingEvent.getMarker()).thenReturn(anotherMarker);
    assertEquals(deny, markerFilter.decide(nonMatchingEvent));
  }

  @Test
  void testMultipleMarkerWithOnlyOneMarkerAcceptsMatchingEvent() {
    markerFilter.setMarkers(TEST_MARKER_MONITORING);
    markerFilter.start();
    Marker marker = MarkerFactory.getMarker(TEST_MARKER_MONITORING);
    ILoggingEvent matchingEvent = mock(ILoggingEvent.class);
    when(matchingEvent.getMarker()).thenReturn(marker);
    assertEquals(accept, markerFilter.decide(matchingEvent));
  }

  @Test
  void testMultipleMarkerAcceptsMatchingEvents() {
    markerFilter.setMarkers(" Monitoring, Validation ");
    markerFilter.start();
    Marker marker1 = MarkerFactory.getMarker(TEST_MARKER_MONITORING);
    ILoggingEvent matchingEvent1 = mock(ILoggingEvent.class);
    when(matchingEvent1.getMarker()).thenReturn(marker1);
    assertEquals(accept, markerFilter.decide(matchingEvent1));
    Marker marker2 = MarkerFactory.getMarker(TEST_MARKER_VALIDATION);
    ILoggingEvent matchingEvent2 = mock(ILoggingEvent.class);
    when(matchingEvent2.getMarker()).thenReturn(marker2);
    assertEquals(accept, markerFilter.decide(matchingEvent2));
  }

  @Test
  void testMultipleMarkerRejectsNonMatchingEvent() {
    markerFilter.setMarkers(" Monitoring, Validation ");
    markerFilter.start();
    Marker anotherMarker = MarkerFactory.getMarker("anotherMarkerName");
    ILoggingEvent nonMatchingEvent = mock(ILoggingEvent.class);
    when(nonMatchingEvent.getMarker()).thenReturn(anotherMarker);
    assertEquals(deny, markerFilter.decide(nonMatchingEvent));
  }

  @Test
  void testStartWithoutMarker() {
    markerFilter.start();
    assertFalse(markerFilter.isStarted());
  }

  @Test
  void testStartWithMarker() {
    markerFilter.setMarker(TEST_MARKER_MONITORING);
    markerFilter.start();
    assertTrue(markerFilter.isStarted());
  }

  @Test
  void testDecide() {
    MarkerFilter markerFilter = new MarkerFilter();
    markerFilter.setMarker(TEST_MARKER_MONITORING);
    markerFilter.start();
    Logger logbackLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    LoggingEvent event =
        new LoggingEvent("fqcn", logbackLogger, Level.DEBUG, "a message", null, null);

    final FilterReply reply = markerFilter.decide(event);
    assertEquals(FilterReply.NEUTRAL, reply);
  }

  @Test
  void testDecideWithoutStarted() {
    markerFilter.setMarkers("Monitoring,Validation");
    Logger logbackLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    LoggingEvent event =
        new LoggingEvent("fqcn", logbackLogger, Level.DEBUG, "a message", null, null);

    final FilterReply reply = markerFilter.decide(event);
    assertEquals(FilterReply.NEUTRAL, reply);
  }

  @Test
  void testDecideMismatchWithNonMatchingMarker() {
    markerFilter.setMarker(TEST_MARKER_MONITORING);
    markerFilter.start();
    Logger logbackLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    LoggingEvent event =
        new LoggingEvent("fqcn", logbackLogger, Level.DEBUG, "a message", null, null);
    event.addMarker(MarkerFactory.getMarker("unknownmarker"));

    final FilterReply reply = markerFilter.decide(event);
    assertEquals(FilterReply.DENY, reply);
  }

  @Test
  void testDecideWithMatchingMarker() {
    markerFilter.setMarker(TEST_MARKER_MONITORING);
    markerFilter.start();
    Logger logbackLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    LoggingEvent event =
        new LoggingEvent("fqcn", logbackLogger, Level.DEBUG, "a message", null, null);
    event.addMarker(MarkerFactory.getMarker(TEST_MARKER_MONITORING));

    final FilterReply reply = markerFilter.decide(event);
    assertEquals(FilterReply.ACCEPT, reply);
  }

  @Test
  void single_marker() {
    markerFilter.setMarker(TEST_MARKER_MONITORING);

    assertTrue(markerFilter.markersToMatch.contains(MarkerFilter.MONITORING));
  }

  @Test
  void multiple_markers() {
    markerFilter.setMarkers("Monitoring, Validation");

    assertEquals(2, markerFilter.markersToMatch.size());
    assertTrue(markerFilter.markersToMatch.contains(MarkerFilter.MONITORING));
    assertTrue(markerFilter.markersToMatch.contains(MarkerFilter.VALIDATION));
  }

  @Test
  void event_accept() {
    ILoggingEvent event = mock(ILoggingEvent.class);
    when(event.getMarker()).thenReturn(MarkerFilter.MONITORING);
    markerFilter.setMarkers("Monitoring, Validation");
    markerFilter.start();

    assertEquals(FilterReply.ACCEPT, markerFilter.decide(event));
  }

  @Test
  void event_deny() {
    ILoggingEvent event = mock(ILoggingEvent.class);
    when(event.getMarker()).thenReturn(MarkerFilter.VALIDATION);
    markerFilter.setMarkers(TEST_MARKER_MONITORING);
    markerFilter.start();

    assertEquals(FilterReply.DENY, markerFilter.decide(event));
  }
}
