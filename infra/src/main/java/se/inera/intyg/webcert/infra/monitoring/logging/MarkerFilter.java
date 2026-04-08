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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class MarkerFilter extends AbstractMatcherFilter<ILoggingEvent> {

  /** The Monitoring marker. */
  public static final Marker MONITORING = MarkerFactory.getMarker("Monitoring");

  /** Validation marker. */
  public static final Marker VALIDATION = MarkerFactory.getMarker("Validation");

  List<Marker> markersToMatch = new ArrayList<>();

  @Override
  public void start() {
    if (!this.markersToMatch.isEmpty()) {
      super.start();
    } else {
      addError("!!! no marker yet !!!");
    }
  }

  @Override
  public FilterReply decide(final ILoggingEvent event) {
    if (!isStarted()) {
      return FilterReply.NEUTRAL;
    }

    final Marker marker = event.getMarker();
    return Objects.nonNull(marker) && this.markersToMatch.stream().anyMatch(m -> m.contains(marker))
        ? getOnMatch()
        : getOnMismatch();
  }

  /**
   * Allow multiple markers.
   *
   * @param name the marker name.
   */
  public void setMarker(final String name) {
    if (!Strings.isNullOrEmpty(name)) {
      this.markersToMatch.add(MarkerFactory.getMarker(name));
    }
  }

  /**
   * Allow multiple markers.
   *
   * @param names comma separated list of names.
   */
  public void setMarkers(final String names) {
    if (!Strings.isNullOrEmpty(names)) {
      Splitter.on(",")
          .split(names)
          .forEach(n -> this.markersToMatch.add(MarkerFactory.getMarker(n.trim())));
    }
  }
}
