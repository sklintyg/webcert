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
import se.inera.webcert.service.intyg.IntygService;

@Component
public class OmsandningJob {
    
    private static final Logger LOG = LoggerFactory.getLogger(OmsandningJob.class);
    
    public static final int MAX_RESENDS_PER_CYCLE = 100;

    @Autowired
    private OmsandningRepositoryCustom omsandningRepository;

    @Autowired
    private IntygService intygService;

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
        
        LOG.warn("Performing operation {} on intyg {}", omsandning.getOperation(), omsandning.getIntygId());
        
        switch (omsandning.getOperation()) {
        case STORE_INTYG:
            return intygService.storeIntyg(omsandning);
        case SEND_INTYG:
            return intygService.sendIntyg(omsandning);
        default:
            return false;
        }
    }
}
