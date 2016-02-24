/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.arende;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygMetaData;

@Service
@Transactional("jpaTransactionManager")
public class ArendeServiceImpl implements ArendeService {

    @Autowired
    private ArendeRepository repo;

    @Autowired
    private IntygService intygService;

    @Override
    public Arende processIncomingMessage(Arende arende) throws WebCertServiceException {
        IntygMetaData intygMetaData = intygService.fetchIntygMetaData(arende.getIntygsId());
        decorateArende(arende, intygMetaData);
        arende.setStatus(Status.PENDING_INTERNAL_ACTION);
        arende.setTimestamp(LocalDateTime.now());

        // TODO Validate! Katarina will create validation for intygstjansten in common which we can reuse in webcert
        // TODO LOG THIS
        return repo.save(arende);
    }

    private void decorateArende(Arende arende, IntygMetaData intygMetaData) {
        arende.setIntygTyp(intygMetaData.getIntygTyp());
        arende.setSigneratAv(intygMetaData.getSigneratAv());
        arende.setEnhet(intygMetaData.getEnhet());
    }
}
