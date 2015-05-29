package se.inera.webcert.scheduler;

import javax.persistence.OptimisticLockException;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import se.inera.webcert.persistence.utkast.model.Omsandning;
import se.inera.webcert.persistence.utkast.repository.OmsandningRepositoryCustom;
import se.inera.webcert.service.intyg.IntygOmsandningService;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;

@Component
public class OmsandningJob {

    private static final Logger LOG = LoggerFactory.getLogger(OmsandningJob.class);

    public static final int MAX_RESENDS_PER_CYCLE = 100;

    @Autowired
    private OmsandningRepositoryCustom omsandningRepository;

    @Autowired
    private IntygOmsandningService intygService;

    private TransactionTemplate transactionTemplate;

    @Autowired
    public void setTxManager(PlatformTransactionManager txManager) {
        this.transactionTemplate = new TransactionTemplate(txManager);
    }
    
    @Scheduled(cron = "${scheduler.omsandningJob.cron}")
    public void sandOm() {
        LOG.info("<<<Scheduled resend starting.");
        int failures = 0;
        for (Omsandning omsandning : omsandningRepository.findByGallringsdatumGreaterThanAndNastaForsokLessThanNotBearbetas(LocalDateTime.now(),
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

    protected void logLockOmsandning(Omsandning omsandning, boolean success) {
        if (success) {
        LOG.info("Locked omsandning {} for resend", omsandning.getId());
        } else {
            LOG.info("Omsandning {} already locked, skipping", omsandning.getId());
        }
    }

    private boolean performOperation(final Omsandning omsandning) {

        Omsandning bearbetadOmsandning;
        try {
            bearbetadOmsandning = transactionTemplate.execute(new TransactionCallback<Omsandning>() {
                public Omsandning doInTransaction(TransactionStatus status) {
                    omsandning.setBearbetas(true);
                    return omsandningRepository.save(omsandning);
                }
            });
        } catch (OptimisticLockException e) {
            // Denna omsändning bearbetas redan av någon annan, skippa den
            logLockOmsandning(omsandning, false);
            return true;
        }
        logLockOmsandning(omsandning, true);
        
        IntygServiceResult res = null;

        switch (omsandning.getOperation()) {
        case STORE_INTYG:
            res = intygService.storeIntyg(bearbetadOmsandning);
            break;
        case SEND_INTYG:
            res = intygService.sendIntyg(bearbetadOmsandning);
            break;
        default:
            res = IntygServiceResult.FAILED;
        }

        LOG.warn("Performed operation {} on intyg {} with result {}", new Object[] { bearbetadOmsandning.getOperation(), bearbetadOmsandning
                .getIntygId(), res });

        return (IntygServiceResult.OK.equals(res));
    }
}
