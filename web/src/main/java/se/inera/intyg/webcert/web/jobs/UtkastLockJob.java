package se.inera.intyg.webcert.web.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.javacrumbs.shedlock.core.SchedulerLock;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@Component
public class UtkastLockJob {
    private static final Logger LOG = LoggerFactory.getLogger(UtkastLockJob.class);
    private static final String JOB_NAME = "UtkastLockJob.run";


    @Autowired
    private UtkastService utkastService;

    @Value("${job.utkastlock.locked.after.day}")
    private int lockedAfterDay;

    @Scheduled(cron = "${job.utkastlock.cron}")
    @SchedulerLock(name = JOB_NAME)
    public void run() {
        LOG.info("Staring job to set utkast to locked");

        int numberOfLocked = utkastService.lockOldDrafts(lockedAfterDay);

        LOG.info("{} utkast set to locked", numberOfLocked);
    }
}
