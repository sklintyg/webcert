package se.inera.webcert.scheduler;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import se.inera.webcert.persistence.intyg.model.Omsandning;
import se.inera.webcert.persistence.intyg.repository.OmsandningRepositoryCustom;
import se.inera.webcert.service.intyg.IntygOmsandningService;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;

@Component
public class OmsandningJob {

    private static final Logger LOG = LoggerFactory.getLogger(OmsandningJob.class);

    public static final int MAX_RESENDS_PER_CYCLE = 100;

    @Autowired
    private OmsandningRepositoryCustom omsandningRepository;

    @Autowired
    private IntygOmsandningService intygService;

    @Scheduled(cron = "${scheduler.omsandningJob.cron}")
    public void sandOm() {
        LOG.info("<<<Scheduled resend starting.");
        int failures = 0;
        for (Omsandning omsandning : omsandningRepository.findByGallringsdatumGreaterThanAndNastaForsokLessThan(LocalDateTime.now(),
                LocalDateTime.now())) {

            if (!performOperation(omsandning)) {
                if (++failures >= MAX_RESENDS_PER_CYCLE) {
                    logTooManyFailures();
                    break;
                }
            }
        }
        LOG.info(">>>Scheduled resend done.");
    }

    protected void logTooManyFailures() {
        LOG.error("Cancelling resend cycle due to too many faults!");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private boolean performOperation(Omsandning omsandning) {

        IntygServiceResult res = null;
        
        switch (omsandning.getOperation()) {
        case STORE_INTYG:
            res = intygService.storeIntyg(omsandning);
            break;
        case SEND_INTYG:
            res = intygService.sendIntyg(omsandning);
            break;
        case REVOKE_INTYG:
            res = intygService.revokeIntyg(omsandning);
            break;
        default:
            res = IntygServiceResult.FAILED;
        }
        
        LOG.warn("Performed operation {} on intyg {} with result {}", new Object[]{omsandning.getOperation(), omsandning.getIntygId(), res});
        
        return (IntygServiceResult.OK.equals(res));
    }
}
