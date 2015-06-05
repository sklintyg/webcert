package se.inera.webcert.scheduler;

import javax.persistence.OptimisticLockException;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import se.inera.webcert.persistence.utkast.model.Omsandning;
import se.inera.webcert.persistence.utkast.model.ScheduleratJobb;
import se.inera.webcert.persistence.utkast.repository.OmsandningRepositoryCustom;
import se.inera.webcert.persistence.utkast.repository.ScheduleratJobbRepository;
import se.inera.webcert.service.intyg.IntygOmsandningService;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;

@Component
public class OmsandningJob {

    private static final Logger LOG = LoggerFactory.getLogger(OmsandningJob.class);

    public static final int MAX_RESENDS_PER_CYCLE = 100;

    public static final String JOBB_ID = "Omsandning";
    
    @Autowired
    private ScheduleratJobbRepository omsandningJobbRepository;

    @Autowired
    private OmsandningRepositoryCustom omsandningRepository;

    @Autowired
    private IntygOmsandningService intygService;

    private TransactionTemplate transactionTemplate;

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Autowired
    public void setTxManager(PlatformTransactionManager txManager) {
        this.transactionTemplate = new TransactionTemplate(txManager);
    }
    
    @Scheduled(cron = "${scheduler.omsandningJob.cron}")
    public void sandOm() {
        final ScheduleratJobb omsandningJobb = getScheduleratJobb();
        if (omsandningJobb.isBearbetas()) {
            LOG.info("<<<Resend already in progress - skipping.");
            return;
        }
        
        ScheduleratJobb bearbetatOmsandningJobb;
        try {
            bearbetatOmsandningJobb = transactionTemplate.execute(new TransactionCallback<ScheduleratJobb>() {
                public ScheduleratJobb doInTransaction(TransactionStatus status) {
                    omsandningJobb.setBearbetas(true);
                    return omsandningJobbRepository.save(omsandningJobb);
                }
            });
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            // Denna omsändning bearbetas redan av någon annan, skippa den
            LOG.info("<<<Resend already in progress - skipping.");
            return ;
        }
        try {
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
        } finally {
            bearbetatOmsandningJobb.setBearbetas(false);
            omsandningJobbRepository.save(bearbetatOmsandningJobb);
        }
    }

    private ScheduleratJobb getScheduleratJobb() {
        ScheduleratJobb omsandningJobb = omsandningJobbRepository.findOne(JOBB_ID);
        if (omsandningJobb == null) {
            omsandningJobb = new ScheduleratJobb(JOBB_ID);
        }
        return omsandningJobb;
    }

    protected void logTooManyFailures() {
        LOG.error("Cancelling resend cycle due to too many faults!");
    }

    private boolean performOperation(Omsandning omsandning) {

        IntygServiceResult res = null;

        switch (omsandning.getOperation()) {
        case STORE_INTYG:
            res = intygService.storeIntyg(omsandning);
            break;
        case SEND_INTYG:
            res = intygService.sendIntyg(omsandning);
            break;
        default:
            res = IntygServiceResult.FAILED;
        }

        LOG.warn("Performed operation {} on intyg {} with result {}", new Object[] { omsandning.getOperation(), omsandning.getIntygId(), res });

        return (IntygServiceResult.OK.equals(res));
    }
}
