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

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.notification.NotificationRedeliveryJobService;

@Component
public class NotificationRedeliveryJob {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryJob.class);

    @Autowired
    private NotificationRedeliveryJobService notificationRedeliveryJobService;

    @Autowired
    private MdcHelper mdcHelper;

    private static final String JOB_NAME = "NotificationRedeliveryJob.run";
    private static final String LOCK_AT_MOST = "PT29S";
    private static final String LOCK_AT_LEAST = "PT20S";

    @Value("${job.notification.redelivery.batchsize:1000}")
    private int batchSize;

    @Scheduled(cron = "${job.notification.redelivery.cron:-}")
    @SchedulerLock(name = JOB_NAME, lockAtLeastFor = LOCK_AT_LEAST, lockAtMostFor = LOCK_AT_MOST)
    @PerformanceLogging(eventAction = "job-lock-drafts-from-certificate-service", eventType = MdcLogConstants.EVENT_TYPE_CHANGE,
        eventCategory = MdcLogConstants.EVENT_CATEGORY_PROCESS)
    public void run() {
        LOG.debug("Running notification redelivery job...");

        try {
            MDC.put(MdcLogConstants.TRACE_ID_KEY, mdcHelper.traceId());
            MDC.put(MdcLogConstants.SPAN_ID_KEY, mdcHelper.spanId());

            notificationRedeliveryJobService.resendScheduledNotifications(batchSize);
        } catch (Exception ex) {
            LOG.error("Redelivery job failed due to unexpected error: ", ex);
        } finally {
            MDC.clear();
        }
    }
}
