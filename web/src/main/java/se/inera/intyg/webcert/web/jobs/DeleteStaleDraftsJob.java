package se.inera.intyg.webcert.web.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteStaleDraftsJob {

    private static final String JOB_NAME = "DeleteStaleDraftsJob.run";
    private static final String LOCK_AT_MOST = "PT10M";
    private static final String LOCK_AT_LEAST = "PT30S";
    private final MdcHelper mdcHelper;

    @Scheduled(cron = "${delete.stale.drafts.cron}")
    @SchedulerLock(name = JOB_NAME, lockAtLeastFor = LOCK_AT_LEAST, lockAtMostFor = LOCK_AT_MOST)
    @PerformanceLogging(eventAction = "delete-stale-drafts", eventType = MdcLogConstants.EVENT_TYPE_CHANGE,
        eventCategory = MdcLogConstants.EVENT_CATEGORY_PROCESS)
    public void run() {
        //TODO: Check if feature is active - "delete-stale-drafts-activated"
        log.info("Staring job to delete stale drafts");

        try {
            MDC.put(MdcLogConstants.TRACE_ID_KEY, mdcHelper.traceId());
            MDC.put(MdcLogConstants.SPAN_ID_KEY, mdcHelper.spanId());

        } finally {
            MDC.clear();
        }
    }
}