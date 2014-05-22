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
import se.inera.webcert.persistence.intyg.repository.OmsandningRepository;
import se.inera.webcert.service.IntygService;

@Component
public class OmsandningJob {
    private static final Logger LOG = LoggerFactory.getLogger(OmsandningJob.class);
    private static final int MAX_OMSANDNING_PER_CYCLE = 100;

    @Autowired
    private OmsandningRepository omsandningRepository;

    @Autowired
    private IntygService intygService;

//    @Scheduled(cron = "5 * * * * ?")
    @Scheduled(cron = "${scheduler.omsandningJob.cron}")
    public void sandOm() {
        LOG.info("<<<Schemalagd omsandning startar.");
        int failures = 0;
        for (Omsandning omsandning : omsandningRepository.findAll()) {
            if (omsandning.getGallringsdatum().isAfter(LocalDateTime.now())) {
                LOG.warn("Forsoker skicka om intyg: " + omsandning.getIntygId());
                if(!sandOm(omsandning)) {
                    if(++failures >= MAX_OMSANDNING_PER_CYCLE) {
                        break;
                    }
                }
            }
        }
        LOG.info(">>>Schemalagd omsandning klar.");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private boolean sandOm(Omsandning omsandning) {
        return intygService.storeIntyg(omsandning);
    }
}
