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
package se.inera.intyg.webcert.infra.ia.jobs;

import java.util.List;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import se.inera.intyg.webcert.infra.driftbannerdto.Application;
import se.inera.intyg.webcert.infra.driftbannerdto.Banner;
import se.inera.intyg.webcert.infra.ia.services.IABannerService;
import se.inera.intyg.webcert.infra.monitoring.logging.LogMDCHelper;

public abstract class BannerJob {

  private static final Logger LOG = LoggerFactory.getLogger(BannerJob.class);
  private static final String JOB_NAME = "BannerJob.run";
  private static final String LOCK_AT_MOST = "PT10M"; // 10 * 60 * 1000
  private static final String LOCK_AT_LEAST = "PT30S"; // 30 * 1000;

  @Autowired private IABannerService iaBannerService;

  @Autowired private LogMDCHelper logMDCHelper;

  @Scheduled(cron = "${intygsadmin.cron}")
  @SchedulerLock(name = JOB_NAME, lockAtLeastFor = LOCK_AT_LEAST, lockAtMostFor = LOCK_AT_MOST)
  public void run() {
    logMDCHelper.run(
        () -> {
          List<Banner> banners = iaBannerService.loadBanners(getApplication());

          LOG.debug("Loaded banners from IA, found {} banners", banners.size());
        });
  }

  protected abstract Application getApplication();
}
