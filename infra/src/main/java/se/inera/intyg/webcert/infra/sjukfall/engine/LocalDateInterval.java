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
package se.inera.intyg.webcert.infra.sjukfall.engine;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;

/**
 * This class has the important getDurationInDays which includes the to-date in duration length.
 *
 * <p>Created by marced on 19/02/16.
 */
public class LocalDateInterval {

  private LocalDate startDate;
  private LocalDate endDate;

  public LocalDateInterval(LocalDate startDate, LocalDate endDate) {
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public int getDurationInDays() {
    // ChronoUnit.DAYS does not include the endDate (just between), so to get "duration", we need to
    // add 1.
    return Long.valueOf(DAYS.between(startDate, endDate)).intValue() + 1;
  }
}
