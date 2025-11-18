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
import se.inera.intyg.webcert.web.csintegration.certificate.DeleteStaleDraftsService;
import se.inera.intyg.webcert.web.csintegration.util.DeleteStaleDraftsProfile;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteStaleDraftsJob {

    private final MdcHelper mdcHelper;
    private final DeleteStaleDraftsService deleteStaleDraftsService;
    private final DeleteStaleDraftsProfile deleteStaleDraftsProfile;

    private static final String JOB_NAME = "DeleteStaleDraftsJob.run";
    private static final String LOCK_AT_MOST = "PT10M";
    private static final String LOCK_AT_LEAST = "PT30S";
    
    @Scheduled(cron = "${delete.stale.drafts.cron}")
    @SchedulerLock(name = JOB_NAME, lockAtLeastFor = LOCK_AT_LEAST, lockAtMostFor = LOCK_AT_MOST)
    @PerformanceLogging(eventAction = "delete-stale-drafts", eventType = MdcLogConstants.EVENT_TYPE_CHANGE,
        eventCategory = MdcLogConstants.EVENT_CATEGORY_PROCESS)
    public void run() {
        if (!deleteStaleDraftsProfile.active()) {
            log.info("Not staring job to delete stale drafts since profile is not active");
            return;
        }

        try {
            MDC.put(MdcLogConstants.TRACE_ID_KEY, mdcHelper.traceId());
            MDC.put(MdcLogConstants.SPAN_ID_KEY, mdcHelper.spanId());

            log.info("Staring job to delete stale drafts");
            deleteStaleDraftsService.delete();
        } finally {
            MDC.clear();
        }
    }
}