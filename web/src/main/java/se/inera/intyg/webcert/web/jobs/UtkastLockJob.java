/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import java.time.LocalDate;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@Component
public class UtkastLockJob {

    private static final Logger LOG = LoggerFactory.getLogger(UtkastLockJob.class);
    private static final String JOB_NAME = "UtkastLockJob.run";
    private static final String LOCK_AT_MOST = "PT10M"; //10 * 60 * 1000
    private static final String LOCK_AT_LEAST = "PT30S"; //30 * 1000
    
    private final UtkastService utkastService;
    private final LockDraftsFromCertificateService lockDraftsFromCertificateService;

    @Value("${job.utkastlock.locked.after.day}")
    private int lockedAfterDay;

    public UtkastLockJob(UtkastService utkastService, LockDraftsFromCertificateService lockDraftsFromCertificateService) {
        this.utkastService = utkastService;
        this.lockDraftsFromCertificateService = lockDraftsFromCertificateService;
    }

    @Scheduled(cron = "${job.utkastlock.cron}")
    @SchedulerLock(name = JOB_NAME, lockAtLeastFor = LOCK_AT_LEAST, lockAtMostFor = LOCK_AT_MOST)
    @PerformanceLogging(eventAction = "job-lock-drafts", eventType = MdcLogConstants.EVENT_TYPE_CHANGE,
        eventCategory = MdcLogConstants.EVENT_CATEGORY_PROCESS)
    public void run() {
        LOG.info("Staring job to set utkast to locked");

        final var today = LocalDate.now();
        final var lockedInWC = utkastService.lockOldDrafts(lockedAfterDay, today);
        final var lockedInCS = lockDraftsFromCertificateService.lock(lockedAfterDay);

        LOG.info("{} utkast set to locked - {} in Webcert - {} in CertificateService", lockedInWC + lockedInCS, lockedInWC, lockedInCS);
    }
}
