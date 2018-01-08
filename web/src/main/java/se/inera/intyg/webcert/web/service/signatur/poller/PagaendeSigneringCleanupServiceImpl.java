/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.signatur.poller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.persistence.utkast.model.PagaendeSignering;
import se.inera.intyg.webcert.persistence.utkast.repository.PagaendeSigneringRepository;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional("jpaTransactionManager")
@EnableScheduling
@Profile({ "dev", "test", "webcertMainNode" })
public class PagaendeSigneringCleanupServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(PagaendeSigneringCleanupServiceImpl.class);

    private static final String UNKNOWN_TIMESTAMP = "UnknownTimestamp";
    private static final long EVICT_OLDER_THAN_MINUTES = 15L;

    @Autowired
    private PagaendeSigneringRepository pagaendeSigneringRepository;


    @Scheduled(cron = "${signature.cleanup.cron}")
    public void cleanup() {
        List<PagaendeSignering> all = pagaendeSigneringRepository.findAll();
        if (all == null || all.isEmpty()) {
            return;
        }
        LOG.info("Running stale signature cleanup. There are currently {} ongoing signatures.", all.size());

        LocalDateTime evictIfOlderThan = LocalDateTime.now().minusMinutes(EVICT_OLDER_THAN_MINUTES);

        Iterator<PagaendeSignering> iterator = all.stream().filter(ps -> ps.getSigneringsDatum().isBefore(evictIfOlderThan)).iterator();
        while (iterator.hasNext()) {
            PagaendeSignering pagaendeSignering = iterator.next();
            Long id = pagaendeSignering.getInternReferens();
            String intygsId = pagaendeSignering.getIntygsId();
            pagaendeSigneringRepository.delete(pagaendeSignering.getInternReferens());
            LOG.info("Removed stale PagaendeSignering with id '{}' for intygs-id '{}'. "
                    + "This is perfectly normal if the signing user didn't complete the signing.", id, intygsId);
        }
    }
}
