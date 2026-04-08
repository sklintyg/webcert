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
package se.inera.intyg.webcert.infra.intyginfo.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IntygInfoEvent {

  private Source source;
  private LocalDateTime date;
  private IntygInfoEventType type;
  private Map<String, Object> data;

  // For deserialize
  IntygInfoEvent() {}

  public IntygInfoEvent(Source source) {
    this.source = source;
  }

  public IntygInfoEvent(Source source, LocalDateTime date, IntygInfoEventType type) {
    this.source = source;
    this.date = date;
    this.type = type;
  }

  public Source getSource() {
    return source;
  }

  public void setSource(Source source) {
    this.source = source;
  }

  public LocalDateTime getDate() {
    return date;
  }

  public void setDate(LocalDateTime date) {
    this.date = date;
  }

  public IntygInfoEventType getType() {
    return type;
  }

  public void setType(IntygInfoEventType type) {
    this.type = type;
  }

  public void addData(String key, Object value) {
    if (data == null) {
      data = new HashMap<>();
    }

    data.put(key, value);
  }

  public Map<String, Object> getData() {
    return data;
  }

  public void setData(Map<String, Object> data) {
    this.data = data;
  }

  public enum Source {
    WEBCERT,
    INTYGSTJANSTEN
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IntygInfoEvent)) {
      return false;
    }
    IntygInfoEvent that = (IntygInfoEvent) o;
    return source == that.source
        && Objects.equals(date, that.date)
        && type == that.type
        && Objects.equals(data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, date, type, data);
  }

  @Override
  public String toString() {
    return "IntygInfoEvent{"
        + "source="
        + source
        + ", date="
        + date
        + ", type="
        + type
        + ", data="
        + data
        + '}';
  }
}
