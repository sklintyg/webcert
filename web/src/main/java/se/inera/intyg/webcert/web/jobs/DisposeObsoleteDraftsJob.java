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
package se.inera.intyg.webcert.web.jobs;

import java.time.LocalDateTime;
import java.time.Period;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.csintegration.certificate.DisposeObsoleteDraftsService;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisposeObsoleteDraftsJob {

  private static final String JOB_NAME = "DisposeObsoleteDraftsJob.run";
  private static final String LOCK_AT_MOST = "PT10M";
  private static final String LOCK_AT_LEAST = "PT30S";
  private final MdcHelper mdcHelper;
  private final DisposeObsoleteDraftsService disposeObsoleteDraftsService;

  @Value("${dispose.obsolete.drafts.period:P3M}")
  private String obsoleteDraftsPeriod;

  @Value("${dispose.obsolete.drafts.page.size:1000}")
  private Integer obsoleteDraftsPageSize;

  @Scheduled(cron = "${dispose.obsolete.drafts.cron:-}")
  @SchedulerLock(name = JOB_NAME, lockAtLeastFor = LOCK_AT_LEAST, lockAtMostFor = LOCK_AT_MOST)
  @PerformanceLogging(
      eventAction = "dispose-obsolete-drafts-job",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE,
      eventCategory = MdcLogConstants.EVENT_CATEGORY_PROCESS)
  public void run() {
    try {
      MDC.put(MdcLogConstants.TRACE_ID_KEY, mdcHelper.traceId());
      MDC.put(MdcLogConstants.SPAN_ID_KEY, mdcHelper.spanId());

      final var disposeObsoleteDraftsDate =
          LocalDateTime.now().minus(Period.parse(obsoleteDraftsPeriod));
      disposeObsoleteDraftsService.dispose(disposeObsoleteDraftsDate, obsoleteDraftsPageSize);
    } finally {
      MDC.clear();
    }
  }
}
